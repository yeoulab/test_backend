package com.load.pgm.jms;

import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class QueueReceive implements MessageListener{
    public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
    public final static String JMS_FACTORY = "com.wlsexpert.ConnectionFactory";
    public final static String QUEUE = "com.wlsexpert.Queue";

    private QueueConnectionFactory qconFactory;
    private QueueConnection qcon;
    private QueueSession qsession;
    private QueueReceiver qreceiver;
    private Queue queue;
    private TextMessage msg;
    private boolean quit = false;
    
    public static void main(String[] args) throws Exception{
    	
    	// JMS 서버/모듈 이 올라가 있는 서버url 미기재 시, 종료
    	//if( args.length != 1 ) {
        //    System.out.println("Usage: java QueueReceive WebLogicURL");
        //    return;
    	//}

	    //InitialContext ic = getInitialConext(args[0]);
	    InitialContext ic = getInitialConext("192.168.56.111:8001");
	    
	    QueueReceive qr = new QueueReceive();
	    qr.init(ic, QUEUE);
	    System.out.println("JMS Ready to recieve Messages ( To quit, send a \"quit\" message from QueuSender class)");
	    
	    // MessageListener 를 수신하는 Thread 들의 경합을 방지하기 위해
	    // 메세지가 들어오면 onMessage 메소드가 호출되고, quit=true 를 받지 않으면 계속 wait 상태로 있는다
	    synchronized(qr) {
            while( !qr.quit ) {
                try {
                	qr.wait();
                }
                catch( InterruptedException e ) {

                }
            }
	    }
	    // while 문을 빠져나오면 qr 
	    qr.close();
    }
    
    private static InitialContext getInitialConext(String url) throws NamingException{
    	Hashtable env = new Hashtable();
    	// wls-api.jar 안에 weblogic.jndi 패키지 안에 있음
    	env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
    	
    	// java.naming.provider.url
    	// Environment class 에서 Set 한다
    	env.put(Context.PROVIDER_URL, url);
    	
    	return new InitialContext(env);
    }    
    
    public void init(Context ctx, String queueName) throws NamingException, JMSException {
        qconFactory = (QueueConnectionFactory)ctx.lookup(JMS_FACTORY); // JMS 모듈의 커넥션팩토리 JNDI
        qcon = qconFactory.createQueueConnection();
        qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE); // 세션 하나에 리시브 하나 연결
        queue = (Queue)ctx.lookup(queueName);
        qreceiver = qsession.createReceiver(queue);
        qreceiver.setMessageListener(this);
        qcon.start();
    }    

    // onMessage 함수 구현체
    public void onMessage(Message msg) {
        try {
            String msgText;
            
            if(msg instanceof TextMessage) {
                msgText = ((TextMessage)msg).getText();
            }
            else {
                msgText = msg.toString();
            }
            // 메세지 출력
            System.out.println("Msg_Receiver : " +msgText);
            
            // quit 가 나오면, 메세지 받는 쓰레드들을 모두 대기(synchronized 구문에서 다른 쓰레드가 잡고 있으므로)
            // quit 값은 true 로 하고 wait 상태를 모두 깨운다
            // 그 다음 main method 수행 시, quit=true 값을 받고, 프로세스 종료
            if( msgText.equalsIgnoreCase("quit")) {
                synchronized(this) {
                    quit = true;
                    this.notifyAll();
                }
            }
        }catch( JMSException jmse) {
                jmse.printStackTrace();
        }
    }

    public void close() throws JMSException{
        qreceiver.close();
        qsession.close();
        qcon.close();
    }
}
