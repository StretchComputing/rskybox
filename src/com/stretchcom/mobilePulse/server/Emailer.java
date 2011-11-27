package com.stretchcom.mobilePulse.server;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.restlet.data.Reference;
import org.restlet.data.Status;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;


public class Emailer {
	private static final Logger log = Logger.getLogger(Emailer.class.getName());
	private static final String BASE_FROM_EMAIL_ADDRESS = "@fruitionpartners.com";
	private static final String FROM_EMAIL_ADDRESS = "reply@fruitionpartners.com";
	private static final String FROM_EMAIL_USER = "automated QualityCenter email";
	public static final String MESSAGE_THREAD_BODY = "see recipeint body";
	public static final String MESSAGE_LINK_REPLY_BODY = "used only for message link reply";
	public static final String REPLY = "reply";
	public static final String NO_REPLY = "products"; //TODO have Chris create email account noreply@fruitionpartners.com and then add as developer to GAE
	public static final String SMS = "sms";
	public static final String JOIN = "join";
	
//	public static void send(String theEmailAddress, String theSubject, String theMessageBody) {
//		send(theEmailAddress, theSubject, theMessageBody, null);
//	}
	
	// Creates an email task and enqueues it.
	// TODO fromEmailUser should be a parameter passed in so email signature can be different when messageThread sent as email??
	public static void send(String theEmailAddress, String theSubject, String theMessageBody, String theFromUserName) {
		String fromEmailAddress = FROM_EMAIL_ADDRESS;
		if(theFromUserName != null) {
			fromEmailAddress = theFromUserName + BASE_FROM_EMAIL_ADDRESS;
		}
		
		// URL "/sendEmailTask" is routed to EmailTaskServlet in web.xml
		// not calling name() to name the task, so the GAE will assign a unique name that has not been used in 7 days (see book)
		// method defaults to POST, but decided to be explicit here
		// PRIORITY TODO need to somehow secure this URL. Book uses web.xml <security-constraint> but not sure why it restricts the
		//               URL to task queues (I see how it restricts it to admins)
		TaskOptions taskOptions = TaskOptions.Builder.url("/sendEmailTask")
				.method(Method.POST)
				.param("emailAddress", theEmailAddress)
				.param("fromEmailAddress", fromEmailAddress)
				.param("fromEmailUser", FROM_EMAIL_USER)
				.param("subject", theSubject)
				.param("message", theMessageBody);
		Queue queue = QueueFactory.getQueue("email"); // "email" queue is defined in WEB-INF/queue.xml
		queue.add(taskOptions);
	}
}
