package com.stretchcom.rskybox.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.stretchcom.rskybox.models.MobileCarrier;

public class EmailTaskServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(EmailTaskServlet.class.getName());
	private static int MAX_TASK_RETRY_COUNT = 3;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		log.info("EmailTaskServlet.doGet() entered - SHOULD NOT BE CALLED!!!!!!!!!!!!!!!!!");
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.info("EmailTaskServlet.doPost() entered");
		String response = "email sent successfully";
		resp.setContentType("text/plain");

		try {
			String emailAddress = req.getParameter("emailAddress");
			log.info("emailAddress parameter: "	+ emailAddress);
			String fromEmailAddress = req.getParameter("fromEmailAddress");
			log.info("fromEmailAddress parameter: "	+ fromEmailAddress);
			String fromEmailUser = req.getParameter("fromEmailUser");
			log.info("fromEmailUser parameter: "	+ fromEmailUser);
			String subject = req.getParameter("subject");
			log.info("subject parameter: "	+ subject);
			String message = req.getParameter("message");
			log.info("message parameter: "	+ message);
			
			// need to get the retry count
			String taskRetryCountStr = req.getHeader("X-AppEngine-TaskRetryCount");
			// default the retry count to max because if it can't be extracted, we are packing up the books and going home
			int taskRetryCount = MAX_TASK_RETRY_COUNT;
			try {
				taskRetryCount = new Integer(taskRetryCountStr);
			} catch (Exception e1) {
				log.info("should never happen, but no harm, no foul");
			}
			log.info("taskRetryCount = " + taskRetryCount);

		    Properties props = new Properties();
		    Session session = Session.getDefaultInstance(props, null);
		    
		    // ensure valid parameters
		    if(emailAddress == null || emailAddress.length() == 0 ||
		    		message == null || message.length() == 0) {
		    	log.severe("Emailer.send(): null or empty parameter");
		    	return;
		    }
			
			// ***** VERIZON iPhone PATCH ******
		    emailAddress = emailAddress.replaceFirst("vzwpix", "vtext");
			// **********************************

		    try {
		    	// If the MobileCarrier Email/SMS gateway uses the from address, then a GAE email can be sent; otherwise,
		    	// must send the email from Rackspace. 
		    	if(Utility.doesEmailAddressStartWithPhoneNumber(emailAddress) && !MobileCarrier.usesFromAddress(emailAddress)) {
		    		// For now, only allow StretchCom folks receive SMS notifications
		    		//if(memberStretchCom(emailAddress)) {
			    		String httpResponse = EmailToSmsClient.sendMail(subject, message, emailAddress, fromEmailAddress);
			    		log.info("EmailToSmsClient response = " + httpResponse);
		    		//}
		    	} else {
			        Message msg = new MimeMessage(session);
			        msg.setFrom(new InternetAddress(fromEmailAddress, fromEmailUser));
			        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress));
			        if(subject == null || subject.trim().length() == 0) {
			        	subject = "...";
			        	log.info("setting subject to ...");
			        }
			        msg.setSubject(subject);
			        msg.setContent(message, "text/html");
			        log.info("sending email to: " + emailAddress + " with subject: " + subject);
			        Transport.send(msg);
		    	}
		        
		        resp.setStatus(HttpServletResponse.SC_OK);
		    } catch (AddressException e) {
		        log.severe("email Address exception " + e.getMessage());
		        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		    } catch (MessagingException e) {
		    	log.severe("email had bad message: " + e.getMessage());
		    	resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		    } catch (UnsupportedEncodingException e) {
		    	log.severe("email address with unsupported format "  + e.getMessage());
		    	resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		    } catch (Exception e) {
		    	log.severe("email address exception "  + e.getMessage());
		    	resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		    	e.printStackTrace();
		    }
		    
			// Return status depends on how many times this been attempted. If max retry count reached, return HTTP 200 so
		    // retries attempt stop.
		    if(taskRetryCount >= MAX_TASK_RETRY_COUNT) {
		    	resp.setStatus(HttpServletResponse.SC_OK);
		    }
		    
			resp.getWriter().println(response);
		}
		catch (Exception ex) {
			response = "Should not happen. Email send: failure : " + ex.getMessage();
			log.info(response);
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println(response);
		}
	}
	
	private Boolean memberStretchCom(String theEmailAddress) {
		if(theEmailAddress.equalsIgnoreCase("joepwro@gmail.com") ||
		   theEmailAddress.equalsIgnoreCase("njw438@gmail.com")  ||
		   theEmailAddress.equalsIgnoreCase("terryroe@gmail.com")     ) {
			return true;
		}
		return false;
	}
}
