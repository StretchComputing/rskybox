package com.stretchcom.rskybox.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.appengine.api.datastore.KeyFactory;
import com.stretchcom.rskybox.models.Notification;


public class CronResource extends ServerResource {
	private static final Logger log = Logger.getLogger(CronResource.class.getName());
	
	String job;

    @Override  
    protected void doInit() throws ResourceException {  
    	log.info("CronResource::doInit() entered");
    	
        this.job = (String)getRequest().getAttributes().get("job");
        if(this.job != null) {
            this.job = Reference.decode(this.job);
            log.info("CronResource() - decoded job = " + this.job);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// NOTE: Other Resource classes can extract URL embedded query parameters in this doInit() method
    	//       but I was unable to extract parameters in this case which are embedded inside the HTTP frame.
    	//       By the time sendEmail() is called below, the extract works fine. Not sure why this is ....
    	////////////////////////////////////////////////////////////////////////////////////////////////////////
    }  

    @Get  
    public StringRepresentation runCronJob(Form theForm) {
    	JSONObject jsonReturn = new JSONObject();
    	
    	if(this.job != null && this.job.equalsIgnoreCase("notifications")) {
    		runNotificationSender();
    	}
    	
    	return new StringRepresentation("success");
    }
    
    private void runNotificationSender() {
    	log.info("runNotificationSender() entered");
    	
    	CronLog notificationsCronLog = null;
    	int numberOfUsersWithPendingNotifications = 0;
    	
    	// MessageThreads that are ACTIVE_STATUS
    	EntityManager emNotification = EMF.get().createEntityManager();
    	try {
        	List<Notification> pendingNotifications = (List<Notification>)emNotification.createNamedQuery("Notification.getBySendGmtDate")
				.setParameter("sendGmtDate", new Date())
				.getResultList();
        	log.info("number of pending notifications found = " + pendingNotifications.size());
        	
    		for(Notification n : pendingNotifications) {
    	    	emNotification.getTransaction().begin();
    	    	Notification aNotification = (Notification)emNotification.createNamedQuery("Notification.getByKey")
        			.setParameter("key", n.getKey())
        			.getSingleResult();
    			
    	    	aNotification.sendPending();
    			emNotification.getTransaction().commit();
    		}
    		log.info("all messageThreads archived successfully");
    		numberOfUsersWithPendingNotifications = pendingNotifications.size();
    	} finally {
    		emNotification.close();
    	}
    	
    	notificationsCronLog = new CronLog();
    	notificationsCronLog.setJobName("notifications");
    	String logMessage = "Number of users sent notifications = " + numberOfUsersWithPendingNotifications + ".";
    	notificationsCronLog.setLogMessage(logMessage);
    	notificationsCronLog.setCreatedGmtDate(new Date());
    	
    	EntityManager emCronLog = EMF.get().createEntityManager();
    	try {
    		emCronLog.persist(notificationsCronLog);
    	} catch(Exception e) {
    		log.severe("exception persisting cron Logs. Message = " + e.getMessage());
    	} finally {
    		emCronLog.close();
    	}
    }
}
