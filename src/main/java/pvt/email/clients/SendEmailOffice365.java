package pvt.email.clients;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendEmailOffice365 {

	private static final Logger LOGGER = Logger.getAnonymousLogger();

	private static final String SERVIDOR_SMTP = "smtp.office365.com";
	private static final int PORTA_SERVIDOR_SMTP = 587;
	private static String USER_NAME = "";
	private static String PASSWORD = "";

	private static String to = "satyabrata.kranti@gmail.com";

	private final String subject = "Test";
	private final String messageContent = "Test mail";
	
	private void getCredentials() {
		final Properties oAuthProperties = new Properties();
		try {
			oAuthProperties.load(App.class.getResourceAsStream("oAuth.properties"));
		} catch (IOException e) {
			System.out.println(
					"Unable to read OAuth configuration. Make sure you have a properly formatted oAuth.properties file. See README for details.");
			return;
		}

		USER_NAME = oAuthProperties.getProperty("username");
		PASSWORD = oAuthProperties.getProperty("password");
		to = oAuthProperties.getProperty("to");
	}

	public void sendEmail() {
		getCredentials();
		System.out.println("Loading session and Authenticating");
		final Session session = Session.getInstance(this.getEmailProperties(), new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(USER_NAME, PASSWORD);
			}

		});
		System.out.println("Authenticated, will try to send an email");

		try {
			final Message message = new MimeMessage(session);
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setFrom(new InternetAddress(USER_NAME));
			message.setSubject(subject);
			message.setText(messageContent);
			message.setSentDate(new Date());
			System.out.println("Sending the mail");
			Transport.send(message);
			System.out.println("Email Sent");
		} catch (final MessagingException ex) {
			LOGGER.log(Level.WARNING, "Error: " + ex.getMessage(), ex);
		}
	}

	public Properties getEmailProperties() {
		final Properties config = new Properties();
		config.put("mail.smtp.auth", "true");
		config.put("mail.smtp.starttls.enable", "true");
		config.put("mail.smtp.host", SERVIDOR_SMTP);
		config.put("mail.smtp.port", PORTA_SERVIDOR_SMTP);
		return config;
	}

	public static void main(final String[] args) {
		System.out.println("Starting send Mails");
		new SendEmailOffice365().sendEmail();
	}

}