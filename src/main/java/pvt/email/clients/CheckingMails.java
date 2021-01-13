package pvt.email.clients;

import java.io.Console;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * POC code for :
 * 1. Retrieve some mails
 * 2. Send some mails 
 * @author Kranti
 */
public class CheckingMails {

	private static final String POP3 = "pop3s";
	private static final String IMAP = "imaps";
	private static final String SMTP = "smtp";
	private static final String PROTOCOL_RECEIVE = "PROTOCOL_RECEIVE";
	private static final String PROTOCOL_SEND = "PROTOCOL_SEND";

	private String password = null;
	private Properties oAuthProperties = null;
	private String propertyFile;
	private Scanner sc;

	/**
	 * Constructor takes property file that contains email server details and user name
	 * Also, initialize the Scanner object with current Input Stream (System.in)
	 * @param proportyFile - property file name, included under maven resources folder
	 */
	public CheckingMails(String proportyFile) {
		this.propertyFile = proportyFile;
		loadProperties(this.propertyFile);
		sc = new Scanner(System.in);
	}
	
	/**
	 * Send an email to an user.
	 * User provides following details:
	 * 1. TO - Recipient ID
	 * 2. Subject - Email Subject
	 * 3. Content - Email content
	 */
	public void sendEmail() {
		System.out.println("Loading session and Authenticating");
		final Session session = Session.getInstance(getProtocolProporties(PROTOCOL_SEND), new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(getProperty("username"), getPassword());
			}

		});
		if(null == session) {
			System.err.println("Session is null, cann't proceed");
			return ;
		}
		
		System.out.println("Authenticated, will try to send an email");

		try {
			final Message message = new MimeMessage(session) {
				//Print the message to console
				@Override
				public String toString() {
					StringBuilder stringBuilder = new StringBuilder();
					try {
						stringBuilder.append("\n").append("From : ").append("\n");
						for (Address address : this.getFrom())
							stringBuilder.append(address).append("\n");
						stringBuilder.append("\n").append("To : ").append("\n");
						for (Address address : this.getRecipients(Message.RecipientType.TO))
							stringBuilder.append(address).append("\n");
						stringBuilder.append("Subject : ").append("\n").append(this.getSubject());
						stringBuilder.append("\n").append("Sent Date : ").append(this.getSentDate());
					} catch (MessagingException e) {
						e.printStackTrace();
					}
					return stringBuilder.toString();
				}
			};
			
			//Get Mail field details
			message.setFrom(new InternetAddress(getProperty("username")));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(getUserInput("to")));
			message.setSubject(getUserInput("subject"));
			message.setText(getUserInput("messageContent"));
			message.setSentDate(new Date());
			System.out.println("Sending the mail as details below :");
			System.out.println(message);
			
			//Send email here, this throws exception if fails
			Transport.send(message);
			
			System.out.println("Email Sent");
		} catch (final MessagingException ex) {
			System.err.println("Error: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Return user input as String for the given text message
	 * @param userMessage : show it to user, must be informative
	 * @return String : whatever user enters, not supporting weird text
	 */
	public String getUserInput(String userMessage) {
		System.out.println(userMessage);
		return sc.nextLine();
	}

	/**
	 * Fetch all mails from the given folder and print first 30 of them
	 * 
	 * @param folderName - Email Folder Name, by default it's Inbox
	 */
	public void checkMails(String folderName) {
		try {
			Store store = getStore();
			if (folderName.isBlank() || folderName.isEmpty())
				folderName = "INBOX";
			// create the folder object and open it
			Folder emailFolder = store.getFolder(folderName);
			emailFolder.open(Folder.READ_ONLY);

			// retrieve the messages from the folder in an array and print it
			Message[] messages = emailFolder.getMessages();
			System.out.println("messages.length---" + messages.length);

			for (int i = 0, n = messages.length; i < n && i < 30; i++) {
				Message message = messages[i];
				System.out.println("---------------------------------");
				System.out.println("Email Number " + (i + 1));
				System.out.println("Subject: " + message.getSubject());
				System.out.println(
						"From: " + ((message.getFrom() != null && message.getFrom().length != 0) ? message.getFrom()[0]
								: "NULL"));
				System.out.println("Text: " + message.getContent().toString());
			}
			// close the store and folder objects
			emailFolder.close(false);
			store.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param protocol
	 * @return Mail Store Object
	 * @throws MessagingException
	 */
	private Store getStore() throws MessagingException {
		// create properties field
		Properties properties = getProtocolProporties(PROTOCOL_RECEIVE);
		Session emailSession = Session.getDefaultInstance(properties);
		return connectSessionStore(emailSession);
	}

	/**
	 * @param emailSession
	 * @return Mail Store Object
	 * @throws MessagingException
	 */
	private Store connectSessionStore(Session emailSession) throws MessagingException {
		Store store = null;
		switch (getProperty(PROTOCOL_RECEIVE)) {
		case POP3:
			// create the POP3 store object and connect with the pop server
			store = emailSession.getStore(POP3);
			// Connect to the store
			store.connect(getProperty("POP3_HOST"), getProperty("username"), getPassword());
			break;
		case IMAP:
			// create the IMAP store object and connect with the imap server
			store = emailSession.getStore(IMAP);
			// Connect to the store
			store.connect(getProperty("IMAP4_HOST"), getProperty("username"), getPassword());
			break;
		}
		return store;
	}

	/**
	 * @return Mail Server and User Properties 
	 */
	private Properties getProtocolProporties(String protocol) {
		Properties properties = new Properties();
		switch (getProperty(protocol)) {
		case POP3:
			properties.put("mail.pop3.host", getProperty("POP3_HOST"));
			properties.put("mail.pop3.port", getProperty("POP3_PORT"));
			properties.put("mail.pop3.starttls.enable", "true");
			break;
		case SMTP:
			properties.put("mail.smtp.port", getProperty("SMTP_PORT"));
			properties.put("mail.smtp.host", getProperty("SMTP_HOST"));
			// properties.put("mail.smtp.ssl.enable", "true");
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.starttls.enable", "true");
			break;
		case IMAP:
			properties.put("mail.imap.user", getProperty("username"));
			properties.put("mail.imap.host", getProperty("IMAP4_HOST"));
			properties.put("mail.imap.port", getProperty("IMAP4_PORT"));
			properties.put("mail.imap.ssl.enable", "true");
			break;
		default:
			return null;
		}
		return properties;
	}

	private Properties loadProperties(String propertyFile) {
		oAuthProperties = new Properties();
		try {
			oAuthProperties.load(App.class.getResourceAsStream(propertyFile));
		} catch (IOException e) {
			System.err.println("Unable to read " + propertyFile
					+ " configuration. Make sure you have a properly formatted  " + propertyFile + " file.");
			return null;
		}
		return oAuthProperties;
	}

	public String getProperty(String propertyName) {
		System.out.println(propertyName + " - " + oAuthProperties.getProperty(propertyName));
		return oAuthProperties.getProperty(propertyName);
	}

	/**
	 * 
	 * @return get Password from user and return as String object
	 */
	private String getPassword() {
		if (null == password || password.isBlank() || password.isEmpty()) {
			Console cons;
			char[] passwd = null;
			if ((cons = System.console()) != null) {
				passwd = cons.readPassword("[%s]", "Password : ");
				password = Arrays.toString(passwd);
			} else {
				System.err.println("Console not found");
				password = getProperty("password");
			}
		}
		return password;
	}

	/**
	 * Just loop through 3 choices user have
	 * 1. Read Email from a folder (default - Inbox)
	 * 2. Send an Email to an user
	 * 3. Close the program
	 */
	private void userInteraction() {
		System.out.println("Starting user Interaction");
		while (true) {
			int input = Integer.parseInt(getUserInput("1 - Read, 2 - Send Email, 3 - Exit..."));
			switch (input) {
			case 1:
				checkMails(getUserInput("Type Folder Name to view details : "));
				break;
			case 2:
				sendEmail();
				break;
			default:
				System.exit(0);
			}
		}
	}

	public static void main(String[] args) {
		CheckingMails checkingMails = new CheckingMails("oAuth-O365.properties");
		checkingMails.userInteraction();
	}

}