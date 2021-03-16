package com.load.pgm.jms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

public class TopicSend{
    public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
    public final static String CONN_FACTORY = "com.wlsexpert.ConnectionFactory";
    public final static String TOPIC = "com.wlsexpert.Topic";

    protected TopicConnectionFactory dutconFactory;
    protected TopicConnection dutcon;
    protected TopicSession dutsession;
    protected TopicPublisher dutpublisher;
    protected Topic dutopic;
    protected TextMessage msg;
    
    public static void main(String[] args) throws Exception{
            if( args.length != 1 ) {
                    System.out.println("error");
                    return;
            }

            InitialContext ic = getInitialContext(args[0]);
            TopicSend duts = new TopicSend();
            duts.init(ic, TOPIC);
            readAndSendMsg(duts);
            duts.close();
    }

    public void init(Context ctx, String topicName) throws NamingException, JMSException{
            dutconFactory = (TopicConnectionFactory)PortableRemoteObject.narrow(ctx.lookup(CONN_FACTORY), TopicConnectionFactory.class);
            dutcon = dutconFactory.createTopicConnection();
            dutsession = dutcon.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            dutopic = (Topic) PortableRemoteObject.narrow(ctx.lookup(topicName), Topic.class);
            dutpublisher = dutsession.createPublisher(dutopic);
            msg = dutsession.createTextMessage();
            dutcon.start();
    }

    protected static InitialContext getInitialContext(String url) throws NamingException{
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
            env.put(Context.PROVIDER_URL, url);
            env.put("weblogic.jndi.createIntermediateContexts", "true");
            return new InitialContext(env);
    }

    public void sendmsg(String message) throws JMSException{
            msg.setText(message);
            dutpublisher.publish(msg);
    }

    protected static void readAndSendMsg(TopicSend duts) throws IOException , JMSException{
            BufferedReader msgStream = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            do {
                    System.out.print("Enter Your message (\"quit\" to quit) : \n");
                    line = msgStream.readLine();
                    if( line != null && line.trim().length() != 0 ) {
                            duts.sendmsg(line);
                            System.out.println("Sent JMS Message: "+line+"\n");
                    }
            }
            while( line != null && !line.equalsIgnoreCase("quit"));
    }

    public void close() throws JMSException{
            dutpublisher.close();
            dutsession.close();
            dutcon.close();
    }
}
