package fft_battleground.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Component;

@Component
public class EmailSender {

	private String to;
	private String from;
	private String host;
	private Session session;
	
	public EmailSender() {
		this.to = "banmenow16@gmail.com";

	      // Sender's email ID needs to be mentioned
	      this.from = "banmenow16@gmail.com";

	      // Assuming you are sending email from localhost
	      this.host = "localhost";

	      // Get system properties
	      Properties properties = System.getProperties();

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", host);

	      // Get the default Session object.
	      this.session = Session.getDefaultInstance(properties);
	}
	
   public void sendMail(String str) {    
      // Recipient's email ID needs to be mentioned.


      try {
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(from));

         // Set To: header field of the header.
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

         // Set Subject: header field
         message.setSubject("This is the Subject Line!");

         // Now set the actual message
         message.setText(str);

         // Send message
         Transport.send(message);
         System.out.println("Sent message successfully....");
      } catch (MessagingException mex) {
         mex.printStackTrace();
      }
   }
}