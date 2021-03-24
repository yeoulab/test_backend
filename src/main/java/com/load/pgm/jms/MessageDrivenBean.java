package com.load.pgm.jms;

import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.ejb.ActivationConfigProperty;
 
@MessageDriven(
activationConfig = {
	@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") ,
	@ActivationConfigProperty(propertyName="connectionFactoryJndiName",propertyValue="com.wlsexpert.ConnectionFactory"),
	@ActivationConfigProperty(propertyName="destinationJndiName", propertyValue="com.wlsexpert.Queue")
	}
	,mappedName="com.wlsexpert.Queue" // 이 값도 Destination 의 jndi 로 세팅
)
 
public class MessageDrivenBean implements MessageListener
{
	public void onMessage(Message message)
	{
		TextMessage textMessage = (TextMessage) message;
		try {
			System.out.println("nnt(mdb) MyMDB Received : "+ textMessage.getText());
		}
		catch (JMSException e)
		{
			e.printStackTrace();
		}
	}
}