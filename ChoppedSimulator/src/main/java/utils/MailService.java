package utils;

import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

public class MailService {
	
	private int randomCode;
	private Random rand = new Random();
	private String host = "smtp.gmail.com";
	private String user ="choppedsimulator@gmail.com";
	private String password="0e93c4fd4092a8e9a3febe2d2fcf9b57";

	/*
	 * sendRestCode(String toUser) - generating random code for resetting the password for the user.
	 * The function init all the connection to send the message.
	 * After the init of the connection, the simulation shows to the user that the "code has been send".
	 */
	public void sendRestCode(String toUser) {
		try {
		randomCode=rand.nextInt(999999);
		String subject="Reseting Code";
		String message ="Your reset code is "+randomCode;
		boolean sessionDebug = false;
		Properties props = System.getProperties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "host");
		props.put("mail.smtp.port","587");
		props.put("mail.smtp.auth","true");
		props.put("mail.smtp.starttls.required", "true");
		
		props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");   
		props.setProperty("mail.smtp.socketFactory.fallback", "false");   
		props.setProperty("mail.smtp.port", "465");   
		props.setProperty("mail.smtp.socketFactory.port", "465"); 
		
		Session session = Session.getDefaultInstance(props,  
			    new javax.mail.Authenticator() {
			       protected PasswordAuthentication getPasswordAuthentication() {  
			       return new PasswordAuthentication(user,password);  
			   }  
		}); 
		session = Session.getInstance(props);
		session.setDebug(sessionDebug);
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(user));
		InternetAddress [] address = {new InternetAddress(toUser)};
		msg.setRecipients(Message.RecipientType.TO, address);
		msg.setSubject(subject);
		msg.setText(message);
		Transport transport = session.getTransport("smtp");
		transport.connect(host, user, password);
		transport.sendMessage(msg, msg.getAllRecipients());
		transport.close();
		JOptionPane.showMessageDialog(null, "Code Has Been Send To Your Email");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * verifyCode(String code) - verify if the code that been send to the user is the same code that the user inserted.
	 */
	public boolean verifyCode(String code) {
		if(Integer.valueOf(code)==randomCode)
			return true;
		else
			return false;
	}
	
}
