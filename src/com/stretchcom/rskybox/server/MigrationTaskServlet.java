package com.stretchcom.rskybox.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.stretchcom.rskybox.models.ClientLog;
import com.stretchcom.rskybox.models.CrashDetect;
import com.stretchcom.rskybox.models.Feedback;
import com.stretchcom.rskybox.models.Incident;
import com.stretchcom.rskybox.models.MobileCarrier;

public class MigrationTaskServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(MigrationTaskServlet.class.getName());

	private static int MAX_TASK_RETRY_COUNT = 3;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		log.info("MigrationTaskServlet.doGet() entered - SHOULD NOT BE CALLED!!!!!!!!!!!!!!!!!");
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.info("MigrationTaskServlet.doPost() entered");
		String response = "migration completed successfully";
		resp.setContentType("text/plain");

		try {
			// no parameters as of yet
			String migrationName = req.getParameter("migrationName");
			log.info("migrationName parameter: " + migrationName);
			
			// need to get the retry count
			String taskRetryCountStr = req.getHeader("X-AppEngine-TaskRetryCount");
			// default the retry count to max because if it can't be extracted, we are packing up the books and going home
			int taskRetryCount = MAX_TASK_RETRY_COUNT;
			try {
				taskRetryCount = new Integer(taskRetryCountStr);
			} catch (Exception e1) {
				log.severe("should never happen, but no harm, no foul");
			}
			log.info("taskRetryCount = " + taskRetryCount);

		    Properties props = new Properties();
		    Session session = Session.getDefaultInstance(props, null);
		    
		    // ensure valid parameters
		    if(migrationName == null || migrationName.length() == 0) {
		    	log.info("MigrationTaskServlet:doPost:parameters null or empty migrationName parameter");
		    	return;
		    }

	    	if(migrationName.equalsIgnoreCase("archiverTask")) {
	    		archiver();
	    	} else if(migrationName.equalsIgnoreCase("cleanRskyboxLogsTask")) {
	    		cleanRskyboxLogs();
	    	} else if(migrationName.equalsIgnoreCase("setLepRepInIncidentsTask")) {
	    		setLepRepInIncidents();
	    	} else {
	    		log.info("task unknown");
	    	}
		    
			// Return status depends on how many times this been attempted. If max retry count reached, return HTTP 200 so
		    // retries attempt stop.
		    if(taskRetryCount >= MAX_TASK_RETRY_COUNT) {
		    	resp.setStatus(HttpServletResponse.SC_OK);
		    }
		    
			resp.getWriter().println(response);
		}
		catch (Exception ex) {
			response = "Should not happen. MigrationTaskServlet failure : " + ex.getMessage();
			log.severe(response);
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println(response);
		}
	}
	
	private void cleanRskyboxLogs() {
		EntityManager emCleaner = EMF.get().createEntityManager();
		int numberOfClientLogsMatched = 0;
		int numberOfClientLogsCleaned = 0;
		//String RSKYBOX_APPLICATION_ID = "ahJyc2t5Ym94LXN0cmV0Y2hjb21yEQsSC0FwcGxpY2F0aW9uGHYM"; // local app id
		String RSKYBOX_APPLICATION_ID = "ahRzfnJza3lib3gtc3RyZXRjaGNvbXITCxILQXBwbGljYXRpb24Y0c4NDA"; // production app id for rSkybox
		
		List<ClientLog> clientLogs = (List<ClientLog>)emCleaner.createNamedQuery("ClientLog.getAllWithApplicationId")
				.setParameter("applicationId", RSKYBOX_APPLICATION_ID)
				.getResultList();
    	log.info("number of total client logs = " + clientLogs.size());
    	
		for(ClientLog cl : clientLogs) {
			if(matchesTargetDate(cl.getCreatedGmtDate())) {
				numberOfClientLogsMatched++;
			}
		}
    	log.info("number of numberOfClientLogsMatched = " + numberOfClientLogsMatched);
	}
	
	private void setLepRepInIncidents() {
		EntityManager emIncident = EMF.get().createEntityManager();
		int numberOfIncidentLepsUpdated = 0;
		int numberOfIncidentRepsUpdated = 0;
		
		List<Incident> incidents = (List<Incident>)emIncident.createNamedQuery("Incident.getAll")
				.getResultList();
    	log.info("setLepRepInIncidents(): number of total incidents = " + incidents.size());
    	
		for(Incident i : incidents) {
			emIncident.getTransaction().begin();
			if(i.getLocalEndpoint() == null) {
				numberOfIncidentLepsUpdated++;
				i.setLocalEndpoint(Incident.DEFAULT_ENDPOINT);
			}
			if(i.getRemoteEndpoint() == null) {
				numberOfIncidentRepsUpdated++;
				i.setRemoteEndpoint(Incident.DEFAULT_ENDPOINT);
			}
			emIncident.getTransaction().commit();
		}
    	log.info("setLepRepInIncidents(): number of incidents with LEPs updated = " + numberOfIncidentLepsUpdated);
    	log.info("setLepRepInIncidents(): number of incidents with REPs updated = " + numberOfIncidentRepsUpdated);
	}
	
	private Boolean matchesTargetDate(Date theDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(theDate);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		if(month == 3 && day == 28) {
			//log.info("month=" + month + " day=" + day + " returning true");
			return true;
		}
		//log.info("month=" + month + " day=" + day + " returning false");
		return false;
	}
	
    private void archiver() {
		EntityManager emArchiver = EMF.get().createEntityManager();
		int numberOfClientLogsPreparedForArchiving = 0;
		int numberOfCrashDetectsPreparedForArchiving = 0;
		int numberOfFeedbacksPreparedForArchiving = 0;
		
		try {
    		//////////////////////////////////////////////
        	// Prepare Historical ClientLogs for Archiving
    		//////////////////////////////////////////////
    		List<ClientLog> clientLogs = (List<ClientLog>)emArchiver.createNamedQuery("ClientLog.getAll").getResultList();
        	log.info("number of total client logs = " + clientLogs.size());
        	
    		for(ClientLog cl : clientLogs) {
    			emArchiver.getTransaction().begin();
    	    	ClientLog aClientLog = (ClientLog)emArchiver.createNamedQuery("ClientLog.getByKey")
        			.setParameter("key", cl.getKey())
        			.getSingleResult();
    			
    	    	Date createdDate = aClientLog.getCreatedGmtDate() == null ? new Date() : aClientLog.getCreatedGmtDate();
    	    	Date activeThruGmtDate = GMT.addDaysToDate(createdDate, 7);
    	    	aClientLog.setActiveThruGmtDate(activeThruGmtDate);
    	    	emArchiver.getTransaction().commit();
    		}
    		numberOfClientLogsPreparedForArchiving = clientLogs.size();
    		log.info("client logs prepared for archiving successfully = " + numberOfClientLogsPreparedForArchiving);

    		////////////////////////////////////////////////
        	// Prepare Historical CrashDetects for Archiving
    		////////////////////////////////////////////////
    		List<CrashDetect> crashDetects = (List<CrashDetect>)emArchiver.createNamedQuery("CrashDetect.getAll").getResultList();
        	log.info("number of total crash detects = " + crashDetects.size());
        	
    		for(CrashDetect cd : crashDetects) {
    			emArchiver.getTransaction().begin();
    	    	CrashDetect aCrashDetect = (CrashDetect)emArchiver.createNamedQuery("CrashDetect.getByKey")
        			.setParameter("key", cd.getKey())
        			.getSingleResult();
    			
    	    	Date detectedDate = aCrashDetect.getDetectedGmtDate() == null ? new Date() : aCrashDetect.getDetectedGmtDate();
    	    	Date activeThruGmtDate = GMT.addDaysToDate(detectedDate, 7);
    	    	aCrashDetect.setActiveThruGmtDate(activeThruGmtDate);
    	    	emArchiver.getTransaction().commit();
    		}
    		numberOfCrashDetectsPreparedForArchiving = crashDetects.size();
    		log.info("crash detects prepared for archiving successfully = " + numberOfCrashDetectsPreparedForArchiving);

    		////////////////////////////////////////////
        	// Prepare Historical Feedback for Archiving
    		////////////////////////////////////////////
    		List<Feedback> feedbacks = (List<Feedback>)emArchiver.createNamedQuery("Feedback.getAll").getResultList();
        	log.info("number of total feedbacks = " + feedbacks.size());
        	
    		for(Feedback fb : feedbacks) {
    			emArchiver.getTransaction().begin();
    			Feedback aFeedback = (Feedback)emArchiver.createNamedQuery("Feedback.getByKey")
        			.setParameter("key", fb.getKey())
        			.getSingleResult();
    			
    	    	Date recordedDate = aFeedback.getRecordedGmtDate() == null ? new Date() : aFeedback.getRecordedGmtDate();
    	    	Date activeThruGmtDate = GMT.addDaysToDate(recordedDate, 7);
    	    	aFeedback.setActiveThruGmtDate(activeThruGmtDate);
    	    	emArchiver.getTransaction().commit();
    		}
    		numberOfFeedbacksPreparedForArchiving = feedbacks.size();
    		log.info("feedbacks prepared for archiving successfully = " + numberOfFeedbacksPreparedForArchiving);
		} catch (Exception e) {
    		log.severe("archiver:Exception" + e.getMessage());
    	} finally {
		    emArchiver.close();
		}
    }
}
