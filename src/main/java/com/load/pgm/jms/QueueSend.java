package com.load.pgm.jms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class QueueSend {
	public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
	public final static String JMS_FACTORY = "com.wlsexpert.ConnectionFactory";
	public final static String QUEUE = "com.wlsexpert.Queue";
	
	private QueueConnectionFactory qconFactory;
	private QueueConnection qcon;
	private QueueSession qsession;
	private QueueSender qsender;
	private Queue queue;
	private TextMessage msg;
	
	public static void main(String[] args) throws Exception{
		if(args.length != 1) {
			System.out.println("Usage: java QueueSend WebLogic URL");
			return;
		}
		
		InitialContext ic = getInitialConext(args[0]);
		QueueSend qs = new QueueSend();
		qs.init(ic, QUEUE);
		readAndSend(qs);
		qs.close();
	}	
	
	private static InitialContext getInitialConext(String url) throws NamingException{
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
		env.put(Context.PROVIDER_URL, url);
		return new InitialContext(env);
	}
	
	public void init(Context ctx, String queueName) throws NamingException, JMSException {
		qconFactory = (QueueConnectionFactory)ctx.lookup(JMS_FACTORY);
		qcon = qconFactory.createQueueConnection();
		qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		queue = (Queue)ctx.lookup(queueName);
		qsender = qsession.createSender(queue);
		msg = qsession.createTextMessage();
		qcon.start();
	}
	
	private static void readAndSend(QueueSend qs) throws IOException, JMSException{
		String line = "Test Message Body with counter = ";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		boolean readFlag = true;
		System.out.println("\n\tStart Sending Messsgaes (Enter Quit to stop): \n");
		while( readFlag ) {
			System.out.println("Msg_Sender: ");
			String msg = br.readLine(); 
			if( msg.equals("QUIT") || msg.equals("quit") ) {
				qs.send(msg);
				System.exit(0);;
			}
			qs.send(msg);
			System.out.println();
		}
		br.close();
	}	
	
	public void send(String message) throws JMSException{
		msg.setText(message);
		qsender.send(msg);
	}
	
	public void close() throws JMSException{
		qsender.close();
		qsession.close();
		qcon.close();
	}
	

	


}
