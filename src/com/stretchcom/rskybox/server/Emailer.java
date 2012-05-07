package com.stretchcom.rskybox.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.stretchcom.rskybox.models.NotificationDetails;


public class Emailer {
	private static final Logger log = Logger.getLogger(Emailer.class.getName());
	private static final String BASE_FROM_EMAIL_ADDRESS = "@rskybox.com";
	private static final String FROM_EMAIL_ADDRESS = "reply@rskybox.com";
	private static final String FROM_EMAIL_USER = "automated rSkybox email";
	public static final String MESSAGE_THREAD_BODY = "see recipeint body";
	public static final String MESSAGE_LINK_REPLY_BODY = "used only for message link reply";
	public static final String REPLY = "reply";
	public static final String NO_REPLY = "noreply";
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
		TaskOptions taskOptions = TaskOptions.Builder.withUrl("/sendEmailTask")
				.method(Method.POST)
				.param("emailAddress", theEmailAddress)
				.param("fromEmailAddress", fromEmailAddress)
				.param("fromEmailUser", FROM_EMAIL_USER)
				.param("subject", theSubject)
				.param("message", theMessageBody);
		Queue queue = QueueFactory.getQueue("email"); // "email" queue is defined in WEB-INF/queue.xml
		queue.add(taskOptions);
	}
	
    
    public static String getConfirmedEmailBody(String theCoreMessage, String theUrl, String theConfirmationCode) {
    	StringBuffer sb = new StringBuffer();
    	buildStandardEmailHeader(sb);
    	
    	sb.append("<div style='margin-bottom:15px;'>");
    	sb.append(theCoreMessage);
    	sb.append("</div>");
    	
    	sb.append("<div style='margin-bottom:10px; margin-top:20px; font-weight:bold'>");
    	sb.append("Please confirm by clicking on the link below.");
    	sb.append("</div>");
    	
    	// end of div with background color. This div starts in the email header
    	sb.append("</div>");
    	
    	sb.append("<div style='height:20px;'></div>");
    	sb.append("<div>");
    	sb.append("<span style='margin-left:15px; margin-right:10px;'>");
    	sb.append("<img style='vertical-align:middle;' src='" + RskyboxApplication.APPLICATION_BASE_URL + "images/arrow_right_green_24.png' width='24' height='24' border='0' alt='*'>");
    	sb.append("</span>");
    	sb.append("<a href='" + theUrl + "'>Send confirmation now</a>");
    	sb.append("</div>");
    	
    	buildStandardEmailSignature(sb, RskyboxApplication.AUTO_SENDER, theConfirmationCode);
    	return sb.toString();
    }
    
    public static String getUserConfirmationEmailBody(String theCoreMessage, String theUrl, String theConfirmationCode) {
    	StringBuffer sb = new StringBuffer();
    	buildStandardEmailHeader(sb);
    	
    	sb.append("<div style='margin-bottom:15px;'>");
    	sb.append(theCoreMessage);
    	sb.append("</div>");
    	
    	sb.append("<div style='margin-bottom:10px; margin-top:20px; font-weight:bold'>");
    	sb.append("* If you are currently in the process of registering, just enter this confirmation code in the appropriate field.<br>");
    	sb.append("* To resume registering and re-launch the rSkybox registration page, click on the link below.");
    	sb.append("</div>");
    	
    	// end of div with background color. This div starts in the email header
    	sb.append("</div>");
    	
    	sb.append("<div style='height:20px;'></div>");
    	sb.append("<div>");
    	sb.append("<span style='margin-left:15px; margin-right:10px;'>");
    	sb.append("<img style='vertical-align:middle;' src='" + RskyboxApplication.APPLICATION_BASE_URL + "images/arrow_right_green_24.png' width='24' height='24' border='0' alt='*'>");
    	sb.append("</span>");
    	sb.append("<a href='" + theUrl + "'>Launch rSkybox registration</a>");
    	sb.append("</div>");
    	
    	buildStandardEmailSignature(sb, RskyboxApplication.AUTO_SENDER, theConfirmationCode);
    	return sb.toString();
    }
    
    public static String getNotificationEmailBody(List<NotificationDetails> theNotificationDetailsList, String theRskyboxBaseUrl) {
    	theRskyboxBaseUrl += "html5";
    	StringBuffer sb = new StringBuffer();
    	buildStandardEmailHeader(sb);

        // each notificationDetail in the list is for a separate application
    	int rowIndex = 0;
        for(NotificationDetails nd : theNotificationDetailsList) {
        	String applicationUrl = theRskyboxBaseUrl + "#application?appId=" + nd.getApplicationId();
        	sb.append("<div style='margin-bottom:5px;'><a href='" + applicationUrl + "'>" + nd.getApplicationName() + "</a></div>");
        	sb.append("<table style='margin-left:15px; border: 1px solid black;'>");
        	sb.append("<tr>");
         	String crashListUrl = theRskyboxBaseUrl + "#iCrashes?appId=" + nd.getApplicationId() + "&status=open";
        	sb.append("<td style='background-color:#F8F8F8; width:65px;'><a href='" + crashListUrl + "'>crash</a></td>");
        	sb.append("<td style='background-color:#F8F8F8; width:25px; text-align:center;'>" + nd.getCrashCount() + "</td>");
        	sb.append("<td style='background-color:#F8F8F8;'>");
        	if(nd.getCrashCount() > 0) {
            	String crashUrl = theRskyboxBaseUrl + "#iCrash?id=" + nd.getCrashId() + "&appId=" + nd.getApplicationId();
            	sb.append("<a href='" + crashUrl + "'>" + nd.getCrashMessage() + "</a>");
        	}
        	sb.append("</td>");
        	sb.append("</tr>");
        	sb.append("<tr>");
         	String logListUrl = theRskyboxBaseUrl + "#iLogs?appId=" + nd.getApplicationId() + "&status=open";
        	sb.append("<td style='background-color:#FFFFFF; width:65px;'><a href='" + logListUrl + "'>log</a></td>");
        	sb.append("<td style='background-color:#FFFFFF; width:25px; text-align:center;'>" + nd.getClientLogCount() + "</td>");
        	sb.append("<td style='background-color:#FFFFFF;'>");
        	if(nd.getClientLogCount() > 0) {
            	String logUrl = theRskyboxBaseUrl + "#iLog?id=" + nd.getClientLogId() + "&appId=" + nd.getApplicationId();
            	sb.append("<a href='" + logUrl + "'>" + nd.getClientLogMessage() + "</a>");
        	}
        	sb.append("</td>");
        	sb.append("</tr>");
        	
//        	sb.append("<tr>");
//        	sb.append("<td style='background-color:#FFFFFF;'><a href='" + logListUrl + "'>updated log</a></td>");
//        	sb.append("<td style='background-color:#FFFFFF; width:25px; text-align:center;'>" + nd.getUpdatedLogCount() + "</td>");
//        	sb.append("<td style='background-color:#FFFFFF;'>");
//        	if(nd.getUpdatedLogCount() > 0) {
//            	String logUrl = theRskyboxBaseUrl + "#log?id=" + nd.getUpdatedLogId() + "&appId=" + nd.getApplicationId();
//            	sb.append("<a href='" + logUrl + "'>" + nd.getUpdatedLogMessage() + "</a>");
//        	}
//        	sb.append("</td>");
//        	sb.append("</tr>");
        	
        	sb.append("<tr>");
         	String feedbackListUrl = theRskyboxBaseUrl + "#iFeedbackList?appId=" + nd.getApplicationId() + "&status=open";
        	sb.append("<td style='background-color:#FFFFFF; width:65px;'><a href='" + feedbackListUrl + "'>feedback</a></td>");
        	sb.append("<td style='background-color:#FFFFFF; width:25px; text-align:center;'>" + nd.getFeedbackCount() + "</td>");
        	sb.append("<td style='background-color:#FFFFFF;'>");
        	if(nd.getFeedbackCount() > 0) {
            	String feedbackUrl = theRskyboxBaseUrl + "#iFeedback?id=" + nd.getFeedbackId() + "&appId=" + nd.getApplicationId();
            	sb.append("<a href='" + feedbackUrl + "'>" + nd.getFeedbackMessage() + "</a>");
        	}
        	sb.append("</td>");
        	sb.append("</tr>");
        	sb.append("</table>");
        	
        	rowIndex++;
		}
    	
    	// end of div with background color. This div starts in the email header
    	sb.append("</div>");
    	
    	sb.append("<div style='height:1px;'></div>");
    	
    	buildStandardEmailSignature(sb, RskyboxApplication.AUTO_SENDER, null);
    	return sb.toString();
    }
    
    private static void buildStandardEmailHeader(StringBuffer sb) {
    	sb.append("<html>");
    	sb.append("<head></head>");
    	sb.append("<body>");
    	
    	sb.append("<div><img src='" + RskyboxApplication.APPLICATION_BASE_URL + "images/rskyboxEmailLogo.png' width='80' height='53' border='0' alt='rSkybox Logo'></div>");
    	sb.append("<div style='height:5px;'></div>");
    	
    	sb.append("<div style='padding:10px; border-radius:10px; -o-border-radius:10px; -icab-border-radius:10px; -khtml-border-radius:10px; ");
    	sb.append("-moz-border-radius:10px; -webkit-border-radius:10px; background-color: #ccc; border: 1px solid #000; width:85%;'>");
    }
    
    private static void buildStandardEmailSignature(StringBuffer sb) {
    	buildStandardEmailSignature(sb, null, null);
    }
    
    private static void buildStandardEmailSignature(StringBuffer sb, String theSenderName) {
    	buildStandardEmailSignature(sb, theSenderName, null);
    }
   
    private static void buildStandardEmailSignature(StringBuffer sb, String theSenderName, String theOneUseToken) {
    	sb.append("<div style='margin-top:30px; font-size:12px;'>");
    	sb.append("Regards,");
    	sb.append("</div>");
    	sb.append("<div style='margin-top:1px; font-size:12px;'>");
    	if(theSenderName == null) {
        	sb.append("(automated message from rTeam)");
    	} else {
    		sb.append(theSenderName);
    	}
    	sb.append("</div>");
    	
    	String settingsUrl = RskyboxApplication.APPLICATION_BASE_URL + "html5" + "#settings";
    	sb.append("<div style='margin-top:10px; font-size:10px;'>");
    	sb.append("(modify your ");
    	sb.append("<a href='" + settingsUrl + "'>settings</a>");
    	sb.append(")");
    	sb.append("</div>");
    	
    	sb.append("</body>");
    	sb.append("</html>");
    }
}
