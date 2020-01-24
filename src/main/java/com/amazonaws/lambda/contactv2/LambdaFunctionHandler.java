package com.amazonaws.lambda.contactv2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LambdaFunctionHandler implements RequestHandler<ContactRequest, ContactResponse> {
	
	private DynamoDB dynamoDb;
	private String DYNAMODB_TABLE_NAME = "Contact"; // TODO: Edit to your DynamoDB Table name
	private Regions REGION = Regions.US_WEST_2;  // TODO: Edit to your DynamoDB Region
	private String greCaptchaURL = "https://www.google.com/recaptcha/api/siteverify";
	private String greCaptchaSecret = "6LfiUc0UAAAAAJmWZERjYxkrNsPm8k61neu9ZTPU"; // TODO: Edit to your reCaptcha Secret key.
	
    @Override
    public ContactResponse handleRequest(ContactRequest contactRequest, Context context) {
    	//context.getLogger().log("Input: " + contactRequest.toString()); // for Debug
    	ContactResponse contactResponse = new ContactResponse();

    	if(verifyRequest(contactRequest, context)) {     	
	        this.initDynamoDbClient();
	
	    	persistData(contactRequest);
	    	try {
				mailSend(contactRequest);
			} catch (AddressException e) {
				context.getLogger().log((e.getStackTrace().toString()));
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
				context.getLogger().log((e.getStackTrace().toString()));
			}
	    	contactResponse.setContact_id(contactRequest.getContact_id());
	    	
	
	    	contactResponse.setMessage("Send Successfully. Our team member will contact in next business day.");
	    	contactResponse.setCreate(contactRequest.getCreate()); 
    	} else {
    		contactResponse.setMessage("reCaptcha validation failed");
	    	contactResponse.setCreate(contactRequest.getCreate());
    	}

        return contactResponse;
    }

    private boolean verifyRequest(ContactRequest contactRequest,  Context context) {
    	boolean result = false;
    	try {
    		String urlParameters = "secret="+greCaptchaSecret +"&response="+contactRequest.getReCapVal();
			URL verifyURL = new URL(greCaptchaURL+"?"+urlParameters);
			InputStream res = verifyURL.openStream();
			ObjectMapper mapper = new ObjectMapper();
			Map<?, ?> resMap = mapper.readValue(res, Map.class);
			//context.getLogger().log("google response: " + resMap); //for Debug
			res.close();
			result = (boolean) resMap.get("success");
		} catch (MalformedURLException e) {
			result= false; 
		} catch (IOException e) {
			result = false;
		}
    	return result;
    }
    
    private PutItemOutcome persistData(ContactRequest contactRequest) throws ConditionalCheckFailedException {
    	return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
    			.putItem(new PutItemSpec()
    					.withItem(
    							new Item()
    							.withPrimaryKey("contact_id", contactRequest.getContact_id())
    							.withString("site", contactRequest.getSite())
    							.withString("name", contactRequest.getName())
    							.withString("email", contactRequest.getEmail())
    							.withString("message", contactRequest.getMessage())
    							.withString("create", contactRequest.getCreate())
						)
				);
    }
    
    private void initDynamoDbClient() {
    	AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(REGION).build();
    	
    	this.dynamoDb = new DynamoDB(client);
	}
    
    private void mailSend(ContactRequest contactRequest) throws AddressException, MessagingException {
    	String host = "smtp.gmail.com";
	    String username = ""; //TODO: put your Gmail account  
	    String password = ""; //TODO: put your Gmail password
	    int port= 465;
	    
	    String recipient = ""; 
	    String subject = "[Enquire]" + contactRequest.getName() +" from " + contactRequest.getSite();
	    StringBuffer body = new StringBuffer();
	    body.append("Contact_id : ");
	    body.append(contactRequest.getContact_id());
	    body.append("\n");
	    body.append("Site : ");
	    body.append(contactRequest.getSite());
	    body.append("\n");
	    body.append("Name : ");
	    body.append(contactRequest.getName());
	    body.append("\n");
	    body.append("Email : ");
	    body.append(contactRequest.getEmail());
	    body.append("\n");
	    body.append("Message : ");
	    body.append(contactRequest.getMessage());
	    body.append("\n");
	    body.append("Create Date : ");
	    body.append(contactRequest.getCreate());
	    body.append("\n");
	    
	    
	    Properties props = System.getProperties();
	    
	    props.put("mail.smtp.host", host);
	    props.put("mail.smtp.port", port);
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.ssl.enable", "true");
	    props.put("mail.smtp.trust", host);
	    
	    Session session = Session.getDefaultInstance(props, new Authenticator() {
	    	String un = username;
	    	String pw = password;
	    	protected PasswordAuthentication getPasswordAuthentication() {
	    		return new PasswordAuthentication(un, pw);
	    	}
		});
	    session.setDebug(false);
	    
	    Message mimeMessage = new MimeMessage(session);
	    
	    //mimeMessage.setFrom(new InternetAddress("")); // TODO: OPTION. set Sender Email Address. Such as 'no_reply@blahblah.com'
	    mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
	    mimeMessage.setRecipient(Message.RecipientType.CC, new InternetAddress(username));
	    mimeMessage.setSubject(subject);
	    mimeMessage.setText(body.toString());
	    
	    Transport.send(mimeMessage);
    }
}
