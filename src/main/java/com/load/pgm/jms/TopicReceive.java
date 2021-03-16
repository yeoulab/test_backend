package com.load.pgm.jms;

import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

public class TopicReceive implements MessageListener{
	public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
	public final static String CONN_FACTORY = "com.wlsexpert.ConnectionFactory";
	public final static String TOPIC = "com.wlsexpert.Topic";
	
	protected TopicConnectionFactory dutconFactory;
	protected TopicConnection dutcon;
	protected TopicSession dutsession;
	protected TopicSubscriber dutsubscriber;
	protected Topic dutopic;
	private boolean quit = false;
	
	public void onMessage(Message msg) {
		try {
			String msgText;
			
			if( msg instanceof TextMessage) {
				msgText = ((TextMessage)msg).getText();
			}
			else {
				msgText = msg.toString();
			}
			System.out.println("Received JMS Message: " +msgText);
			
			if( msgText.equalsIgnoreCase("quit")) {
				synchronized( this ) {
					quit = true;
					this.notifyAll();
				}
			}
		}catch( JMSException jmse) {
			System.err.println("Exception : " +jmse.getMessage());
		}
	}
	
	public static void main(String[] args) throws Exception{
		if( args.length != 1 ) {
			System.out.println("error");
			return;
		}
		InitialContext ic = getInitialContext(args[0]);
		TopicReceive tr = new TopicReceive();
		tr.init(ic, TOPIC);
		System.out.println("JMS is now ready to receive message ( to quit, send \"quit\" message)");
		
		synchronized(tr) {
			while( ! tr.quit ) {
				try {
					tr.wait();
				}
				catch(InterruptedException ie) {
				}
			}
		}
		tr.close();
	}
	
	private static InitialContext getInitialContext(String url) throws NamingException{
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
		env.put(Context.PROVIDER_URL, url);
		env.put("weblogic.jndi.createIntermediateContexts", "true");
		return new InitialContext(env);
	}
	
	public void close() throws JMSException{
		dutsubscriber.close();
		dutsession.close();
		dutcon.close();
	}
	
	public void init(Context ctx, String topicName) throws NamingException, JMSException{
		dutconFactory = (TopicConnectionFactory)PortableRemoteObject.narrow(ctx.lookup(CONN_FACTORY), TopicConnectionFactory.class);
		dutcon = dutconFactory.createTopicConnection();
		
		dutcon.setClientID("wls");
		dutsession = dutcon.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		dutopic = (Topic)PortableRemoteObject.narrow(ctx.lookup(topicName), Topic.class);
		
		dutsubscriber = dutsession.createDurableSubscriber(dutopic, "Test");
		dutsubscriber.setMessageListener(this);
		dutcon.start();
	}
}