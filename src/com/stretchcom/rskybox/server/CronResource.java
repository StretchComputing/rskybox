package com.stretchcom.rskybox.server;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.stretchcom.rskybox.models.Application;
import com.stretchcom.rskybox.models.Incident;
import com.stretchcom.rskybox.models.Notification;
import com.stretchcom.rskybox.models.ClientLog;
import com.stretchcom.rskybox.models.Feedback;
import com.stretchcom.rskybox.models.CrashDetect;

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
    	} else if(this.job != null && this.job.equalsIgnoreCase("archiver")) {
    		runArchiver();
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
    		log.info("all notifications sent successfully");
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
    
    // Originally used to archive logs/crashes/feedbacks. That code commented out for now (may still find a need for it later)
    // Now, closes incidents
    private void runArchiver() {
    	log.info("runArchiver() entered");
    	
    	CronLog archiverCronLog = null;
//    	int numberOfClientLogsArchived = 0;
//    	int numberOfFeedbacksArchived = 0;
//    	int numberOfCrashDetectsArchived = 0;
    	int numberOfIncidentsClosed = 0;
    	
    	EntityManager emMessages = EMF.get().createEntityManager();
    	
    	try {
    		//////////////////
        	// Close Incidents
    		//////////////////
    		List<Incident> incidents = (List<Incident>)emMessages.createNamedQuery("Incident.getOldActiveThru")
				.setParameter("currentDate", new Date())
				.setParameter("status", Incident.OPEN_STATUS)
				.getResultList();
        	log.info("number of incidents ready for closing = " + incidents.size());
        	
    		for(Incident i : incidents) {
    	    	emMessages.getTransaction().begin();
    	    	Incident anIncident = (Incident)emMessages.createNamedQuery("Incident.getByKey")
        			.setParameter("key", i.getKey())
        			.getSingleResult();
    			
    	    	// ::PERFORMANCE::
    	    	// Not calling Incident.changeStatus because that would require first getting the application object via a datastore
    	    	// call and then updating the application event count inside the changeStatus call. That would require retrieving
    	    	// the Application entity twice.  Code below only retrieves it once.
    	    	Application.adjustOpenEventCount(anIncident.getWellKnownTag(), false, anIncident.getApplicationId());
    	    	anIncident.hiddenSetStatus(Incident.CLOSED_STATUS);
    			emMessages.getTransaction().commit();
    		}
    		log.info("all incidents closed successfully");
    		numberOfIncidentsClosed = incidents.size();

    		/////////////////////
        	// Archive ClientLogs
    		/////////////////////
//    		List<ClientLog> clientLogs = (List<ClientLog>)emMessages.createNamedQuery("ClientLog.getOldActiveThru")
//				.setParameter("currentDate", new Date())
//				.setParameter("status", ClientLog.NEW_STATUS)
//				.getResultList();
//        	log.info("number of client logs ready for archiving = " + clientLogs.size());
//        	
//    		for(ClientLog cl : clientLogs) {
//    	    	emMessages.getTransaction().begin();
//    	    	ClientLog aClientLog = (ClientLog)emMessages.createNamedQuery("ClientLog.getByKey")
//        			.setParameter("key", cl.getKey())
//        			.getSingleResult();
//    			
//    	    	aClientLog.setStatus(ClientLog.ARCHIVED_STATUS);
//    			emMessages.getTransaction().commit();
//    		}
//    		log.info("all client logs archived successfully");
//    		numberOfClientLogsArchived = clientLogs.size();
    		
    		/////////////////////
        	// Archive Feedbacks
    		/////////////////////
//        	List<Feedback> feedbacks = (List<Feedback>)emMessages.createNamedQuery("Feedback.getOldActiveThru")
//				.setParameter("currentDate", new Date())
//				.setParameter("status", Feedback.NEW_STATUS)
//				.getResultList();
//        	log.info("number of feedbacks ready for archiving = " + feedbacks.size());
//        	
//    		for(Feedback fb : feedbacks) {
//    	    	emMessages.getTransaction().begin();
//    	    	Feedback aFeedback = (Feedback)emMessages.createNamedQuery("Feedback.getByKey")
//        			.setParameter("key", fb.getKey())
//        			.getSingleResult();
//    			
//    	    	aFeedback.setStatus(Feedback.ARCHIVED_STATUS);
//    			emMessages.getTransaction().commit();
//    		}
//    		log.info("all feedbacks archived successfully");
//    		numberOfFeedbacksArchived = feedbacks.size();
    		
    		///////////////////////
        	// Archive CrashDetects
    		///////////////////////
//        	List<CrashDetect> crashDetects = (List<CrashDetect>)emMessages.createNamedQuery("CrashDetect.getOldActiveThru")
//				.setParameter("currentDate", new Date())
//				.setParameter("status", CrashDetect.NEW_STATUS)
//				.getResultList();
//        	log.info("number of crash detects ready for archiving = " + crashDetects.size());
//        	
//    		for(CrashDetect cd : crashDetects) {
//    	    	emMessages.getTransaction().begin();
//    	    	CrashDetect aCrashDetect = (CrashDetect)emMessages.createNamedQuery("CrashDetect.getByKey")
//        			.setParameter("key", cd.getKey())
//        			.getSingleResult();
//    			
//    	    	aCrashDetect.setStatus(CrashDetect.ARCHIVED_STATUS);
//    			emMessages.getTransaction().commit();
//    		}
//    		log.info("all crash detects archived successfully");
//    		numberOfCrashDetectsArchived = crashDetects.size();
    	} finally {
    		emMessages.close();
    	}
    	
    	archiverCronLog = new CronLog();
    	archiverCronLog.setJobName("archiver");
//    	String logMessage = "Number of client logs archived = " + numberOfClientLogsArchived + "." +
//    			            "Number of feedbacks archived = " + numberOfFeedbacksArchived + "." +
//    			            "Number of crash detects archived = " + numberOfCrashDetectsArchived + ".";
    	String logMessage = "Number of incidents closed = " + numberOfIncidentsClosed;
    	
    	archiverCronLog.setLogMessage(logMessage);
    	archiverCronLog.setCreatedGmtDate(new Date());
    	
    	EntityManager emCronLog = EMF.get().createEntityManager();
    	try {
    		emCronLog.persist(archiverCronLog);
    	} catch(Exception e) {
    		log.severe("exception persisting cron Logs. Message = " + e.getMessage());
    	} finally {
    		emCronLog.close();
    	}
    }
}
