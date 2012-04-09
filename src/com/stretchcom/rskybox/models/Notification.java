package com.stretchcom.rskybox.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.json.JSONArray;
import org.json.JSONException;
import org.restlet.data.Reference;
import org.restlet.data.Status;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.stretchcom.rskybox.server.ApiStatusCode;
import com.stretchcom.rskybox.server.EMF;
import com.stretchcom.rskybox.server.Emailer;
import com.stretchcom.rskybox.server.GMT;
import com.stretchcom.rskybox.server.RskyboxApplication;
import com.stretchcom.rskybox.server.UsersResource;
import com.stretchcom.rskybox.server.Utility;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Only one notification entity per user. The one notification holds the notification details for all a user's notifications for all their applications
// Each of the user's applications has it's own NotificationDetails
// sendGmtDate field is shared among the applications and represents the shortest notifications time of notifications queued up
// (assumption: each application will eventually support setting a minimum notifications interval)
// Cron job that sends notifications can do so using just the data in this entity (not normalized for performance reasons)
// After notification sent, sendGmtDate is set to 2099 and notificationsDetails are cleared
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@Entity
@NamedQueries({
    @NamedQuery(
    		name="Notification.getByKey",
    		query="SELECT n FROM Notification n WHERE n.key = :key"
    ),
    @NamedQuery(
    		name="Notification.getByApplicationId",
    		query="SELECT n FROM Notification n WHERE n.applicationIds = :applicationId"
    ),
    @NamedQuery(
    		name="Notification.getByUserId",
    		query="SELECT n FROM Notification n WHERE n.userId = :userId"
    ),
    @NamedQuery(
    		name="Notification.getByUserIdAndSendGmtDate",
    		query="SELECT n FROM Notification n WHERE n.userId = :userId and n.sendGmtDate < :sendGmtDate"
    ),
    @NamedQuery(
    		name="Notification.getBySendGmtDate",
    		query="SELECT n FROM Notification n WHERE n.sendGmtDate < :sendGmtDate"
    ),
})
public class Notification {
    private static final Logger log = Logger.getLogger(Notification.class.getName());
    
    // notification types
	public static final String CRASH = "crash";
	public static final String CLIENT_LOG = "clientlog";
	public static final String FEEDBACK = "feedback";
	
	public static final int DEFAULT_NOTIFICATION_PERIOD = 5;

	private String userId;
	private String emailAddress;
	private String smsEmailAddress;
	private Date sendGmtDate;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    public Key getKey() {
        return key;
    }
	
	@Basic
	private List<String> applicationIds;
	
	@Basic
	private List<String> applicationNames;
	
	@Basic
	private List<Integer> clientLogCounts;
	
	@Basic
	private List<String> clientLogMessages;
	
	@Basic
	private List<String> clientLogIds;
	
	@Basic
	private List<Integer> crashCounts;
	
	@Basic
	private List<String> crashMessages;
	
	@Basic
	private List<String> crashIds;
	
	@Basic
	private List<Integer> feedbackCounts;
	
	@Basic
	private List<String> feedbackMessages;
	
	@Basic
	private List<String> feedbackIds;

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getSendGmtDate() {
		return sendGmtDate;
	}

	public void setSendGmtDate(Date sendGmtDate) {
		this.sendGmtDate = sendGmtDate;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public String getSmsEmailAddress() {
		return smsEmailAddress;
	}
	public void setSmsEmailAddress(String smsEmailAddress) {
		this.smsEmailAddress = smsEmailAddress;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	// NOTE:: changes to notification entity made by this routine are persisted by the calling routine
	//////////////////////////////////////////////////////////////////////////////////////////////////
	public Boolean createNotificationDetailsList(List<NotificationDetails> theNewNotificationDetailsList) {
		if(theNewNotificationDetailsList == null || theNewNotificationDetailsList.size() == 0) {
			return false;
		}
		
		initNotificationDetails();
		
		for(NotificationDetails nd : theNewNotificationDetailsList) {
			addNotificationDetails(nd);
		}
		
		return true;
	}
	
	// guaranteed to return a non-null list
	public List<NotificationDetails> getNotificationDetailsList() {
		List<NotificationDetails> notificationDetails = new ArrayList<NotificationDetails>();
		
		// doesn't matter which list we used to get the size -- all notification details lists are the same size
		if(this.applicationIds == null || this.applicationIds.size() == 0) {
			// return the empty list
			return notificationDetails;
		}
		// all appAction arrays are same size, so it doesn't matter which one size is taken from
		int listSize = this.applicationIds.size();
		for(int i=0; i<listSize; i++) {
			NotificationDetails nd = new NotificationDetails();

			///////////////////////////////////////////////////////////////////////
			// Convert "default" values stored in Big Table to "normal Java" values
			///////////////////////////////////////////////////////////////////////
			String applicationId = null;
			if(this.applicationIds.size() > i) {
				applicationId = this.applicationIds.get(i).equals("") ? null : this.applicationIds.get(i);
			} else {
				log.severe("applicationIds array size corrupt");
			}
			nd.setApplicationId(applicationId);
			
			String applicationName = null;
			if(this.applicationNames.size() > i) {
				applicationName = this.applicationNames.get(i).equals("") ? null : this.applicationNames.get(i);
			} else {
				log.severe("applicationNames array size corrupt");
			}
			nd.setApplicationName(applicationName);
			
			Integer clientLogCount = null;
			if(this.clientLogCounts.size() > i) {
				clientLogCount = this.clientLogCounts.get(i);
			} else {
				log.severe("clientLogCounts array size corrupt");
			}
			nd.setClientLogCount(clientLogCount);
			
			String clientLogMessage = null;
			if(this.clientLogMessages.size() > i) {
				clientLogMessage = this.clientLogMessages.get(i).equals("") ? null : this.clientLogMessages.get(i);
			} else {
				log.severe("client log messages array size corrupt");
			}
			nd.setClientLogMessage(clientLogMessage);
			
			String clientLogId = null;
			if(this.clientLogIds.size() > i) {
				clientLogId = this.clientLogIds.get(i).equals("") ? null : this.clientLogIds.get(i);
			} else {
				log.severe("clientLogIds array size corrupt");
			}
			nd.setClientLogId(clientLogId);
			
			Integer crashCount = null;
			if(this.crashCounts.size() > i) {
				crashCount = this.crashCounts.get(i);
			} else {
				log.severe("crashCounts array size corrupt");
			}
			nd.setCrashCount(crashCount);
			
			String crashMessage = null;
			if(this.crashMessages.size() > i) {
				crashMessage = this.crashMessages.get(i).equals("") ? null : this.crashMessages.get(i);
			} else {
				log.severe("crash messages array size corrupt");
			}
			nd.setCrashMessage(crashMessage);
			
			String crashId = null;
			if(this.crashIds.size() > i) {
				crashId = this.crashIds.get(i).equals("") ? null : this.crashIds.get(i);
			} else {
				log.severe("crashIds array size corrupt");
			}
			nd.setCrashId(crashId);
			
			Integer feedbackCount = null;
			if(this.feedbackCounts.size() > i) {
				feedbackCount = this.feedbackCounts.get(i);
			} else {
				log.severe("feedbackCounts array size corrupt");
			}
			nd.setFeedbackCount(feedbackCount);
			
			String feedbackMessage = null;
			if(this.feedbackMessages.size() > i) {
				feedbackMessage = this.feedbackMessages.get(i).equals("") ? null : this.feedbackMessages.get(i);
			} else {
				log.severe("feedback messages array size corrupt");
			}
			nd.setFeedbackMessage(feedbackMessage);
			
			String feedbackId = null;
			if(this.feedbackIds.size() > i) {
				feedbackId = this.feedbackIds.get(i).equals("") ? null : this.feedbackIds.get(i);
			} else {
				log.severe("itemIds array size corrupt");
			}
			nd.setFeedbackId(feedbackId);
			
			notificationDetails.add(nd);
		}
		return notificationDetails;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// NOTE:: changes to notification entity made by this routine are persisted by the calling routine
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// Update the NotificationDetails matching the application ID if it exists. If not, just add the new NotificationDetails into the list.
	public Boolean updateNotificationDetailsList(NotificationDetails theNewNotificationDetails) {
		///////////////////////////////////////////////
		// No notification details yet, so create lists
		///////////////////////////////////////////////
		if(this.applicationIds == null) {
			List<NotificationDetails> notificationDetailsList = new ArrayList<NotificationDetails>();
			notificationDetailsList.add(theNewNotificationDetails);
			return this.createNotificationDetailsList(notificationDetailsList);
		} else if(this.applicationIds.size() == 0){
			//////////////////////////////////////////////////
			// Lists exist, but are empty, so just add to list
			//////////////////////////////////////////////////
			log.info("notification array sizes are zero");
			addNotificationDetails(theNewNotificationDetails);
		} else {
			log.info("applicationIds.size = " + this.applicationIds.size());
			//////////////////////////////////////////////////////////
			// Lists are non-empty, so must match using application ID
			//////////////////////////////////////////////////////////
			Integer applicationIdIndex = getApplicationIdIndex(theNewNotificationDetails.getApplicationId());
			if(applicationIdIndex == null) {
				// no entry for this application ID yet, so just add to the end of the list
				addNotificationDetails(theNewNotificationDetails);
			} else {
				// must modify the entry for this application ID
				updateNotificationDetails(theNewNotificationDetails, applicationIdIndex);
			}
		}
		return true;
	}
	
	public static void queueNotification(User theUser, String theApplicationId, AppMember theAppMember, String theNotificationType, 
			                             String theMessage, String theItemId, Boolean theIsEmailActive, Boolean theIsSmsActive) {
        EntityManager em = EMF.get().createEntityManager();
        
        String userId = null;
        try {
        	userId = KeyFactory.keyToString(theUser.getKey());
		} catch (IllegalArgumentException e1) {
			log.severe("exception = " + e1.getMessage());
			e1.printStackTrace();
			return;
		}

		em.getTransaction().begin();
        try {
        	Notification notification = null;
        	try {
            	notification = (Notification)em.createNamedQuery("Notification.getByUserId")
        				.setParameter("userId", userId)
        				.getSingleResult();
            	log.info("existing notification found in datastore");
        	} catch (NoResultException e) {
    			// this is NOT an error, just the very first time a notification is being sent. Notification will be created just below ...
    		} catch (NonUniqueResultException e) {
    			log.severe("should never happen - two or more Users have the same key");
    			e.printStackTrace();
    		}
        	
        	//////////////////////////////////////////////////////////////////////
        	// there is no Notification entity for this user yet, so create it now
        	//////////////////////////////////////////////////////////////////////
        	if(notification == null) {
        		log.info("new notification instantiated");
        		notification = new Notification();
        		notification.setUserId(userId);
        		notification.setSendGmtDateToFarFuture();  // to start, entity for this user is inactive
        	}
        	
        	NotificationDetails notificationDetails = new NotificationDetails();
        	notificationDetails.setApplicationId(theApplicationId);
        	notificationDetails.setApplicationName(theAppMember.getApplicationName());
        	
        	notificationDetails.setCrashCount(0);
        	notificationDetails.setClientLogCount(0);
        	notificationDetails.setFeedbackCount(0);

        	if(theNotificationType.equalsIgnoreCase(Notification.CRASH)) {
        		notificationDetails.setCrashCount(1);
            	notificationDetails.setCrashMessage(theMessage);
            	notificationDetails.setCrashId(theItemId);
        	} else if(theNotificationType.equalsIgnoreCase(Notification.CLIENT_LOG)) {
        		notificationDetails.setClientLogCount(1);
            	notificationDetails.setClientLogMessage(theMessage);
            	notificationDetails.setClientLogId(theItemId);
        	} else if(theNotificationType.equalsIgnoreCase(Notification.FEEDBACK)) {
        		notificationDetails.setFeedbackCount(1);
            	notificationDetails.setFeedbackMessage(theMessage);
            	notificationDetails.setFeedbackId(theItemId);
        	}
        	notification.updateNotificationDetailsList(notificationDetails);
        	
        	///////////////////////////////////////////////////////////////////////////////////////////
        	// update emailAddress and smsEmailAddress based on whether email and SMS are now activated
        	///////////////////////////////////////////////////////////////////////////////////////////
        	if(theIsEmailActive) {
        		notification.setEmailAddress(theUser.getEmailAddress());
        	} else {
        		notification.setEmailAddress(null);
        	}
        	if(theIsSmsActive) {
        		notification.setSmsEmailAddress(theUser.getSmsEmailAddress());
        	} else {
        		notification.setSmsEmailAddress(null);
        	}
        	
        	// check if sendGmtDate needs to be updated
        	if(!GMT.isDateBeforeNowPlusOffsetMinutes(notification.getSendGmtDate(), DEFAULT_NOTIFICATION_PERIOD)) {
        		log.info("setting sendGmtDate to 5 minutes in the future");
        		// set sendGmtDate to five minutes in the future
        		notification.setSendGmtDate(GMT.addMinutesToDate(new Date(), DEFAULT_NOTIFICATION_PERIOD));
        	}
        	
        	em.persist(notification);
        	em.getTransaction().commit();
		}  finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
	}
	
	private void initNotificationDetails() {
		this.applicationIds = new ArrayList<String>();
		this.applicationNames = new ArrayList<String>();
		this.clientLogCounts = new ArrayList<Integer>();
		this.clientLogMessages = new ArrayList<String>();
		this.clientLogIds = new ArrayList<String>();
		this.crashCounts = new ArrayList<Integer>();
		this.crashMessages = new ArrayList<String>();
		this.crashIds = new ArrayList<String>();
		this.feedbackCounts = new ArrayList<Integer>();
		this.feedbackMessages = new ArrayList<String>();
		this.feedbackIds = new ArrayList<String>();
	}
	
	private void addNotificationDetails(NotificationDetails nd) {
		log.info("addNotificationDetails() entered");
		////////////////////////////////////////////////////////////////
		// Convert "normal Java" values to "default" values in Big Table
		////////////////////////////////////////////////////////////////
		String applicationId = nd.getApplicationId() == null ? "" : nd.getApplicationId();
		this.applicationIds.add(applicationId);
		
		String applicationName = nd.getApplicationName() == null ? "" : nd.getApplicationName();
		this.applicationNames.add(applicationName);

		// if empty, replace with 0
		Integer clientLogCount = nd.getClientLogCount() == null ? 0 : nd.getClientLogCount();
		this.clientLogCounts.add(clientLogCount);

		String clientLogMessage = nd.getClientLogMessage() == null ? "" : nd.getClientLogMessage();
		this.clientLogMessages.add(clientLogMessage);

		String clientLogId = nd.getClientLogId() == null ? "" : nd.getClientLogId();
		this.clientLogIds.add(clientLogId);

		// if empty, replace with 0
		Integer crashCount = nd.getCrashCount() == null ? 0 : nd.getCrashCount();
		this.crashCounts.add(crashCount);

		String crashMessage = nd.getCrashMessage() == null ? "" : nd.getCrashMessage();
		log.info("about to add crashMessage = " + crashMessage + " to the crashMessages array");
		this.crashMessages.add(crashMessage);

		String crashId = nd.getCrashId() == null ? "" : nd.getCrashId();
		log.info("about to add crashId = " + crashId + " to the crashIds array");
		this.crashIds.add(crashId);

		// if empty, replace with 0
		Integer feedbackCount = nd.getFeedbackCount() == null ? 0 : nd.getFeedbackCount();
		this.feedbackCounts.add(feedbackCount);

		String feedbackMessage = nd.getFeedbackMessage() == null ? "" : nd.getFeedbackMessage();
		this.feedbackMessages.add(feedbackMessage);

		String feedbackId = nd.getFeedbackId() == null ? "" : nd.getFeedbackId();
		this.feedbackIds.add(feedbackId);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// NOTE:: changes to notification entity made by this routine are persisted by the calling routine
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// Each user has a single entry in the notification entity. This method is called because notifications are pending.
	// Multiple applications can have notifications pending
	// Each application can have multiple notifications pending
	// All pending notifications will be sent and the sendGmtDate will be set far into the future
	public void sendPending() {
		String emailNotification = getEmailNotification();
		String smsNotification = getSmsNotification();
        
		///////////////////////
        // send digest messages
		///////////////////////
		// email address is only set in notification if user is configured to send email notifications
		// SMS email address is only set in notification if user is configured to send SMS notifications
		if(emailNotification != null) {
        	log.info("sending email to " + this.getEmailAddress());
        	Emailer.send(this.getEmailAddress(), "rSkybox", emailNotification, Emailer.NO_REPLY);
        }
        if(smsNotification != null) {
        	log.info("sending SMS to " + this.getSmsEmailAddress());
        	Emailer.send(this.getSmsEmailAddress(), "rSkybox", smsNotification, Emailer.NO_REPLY);
        }
        
        // all notifications taken care of, so clear them out (i.e. initialize all the lists)
        initNotificationDetails();
        
        // setting the sendGmtDate far in the future effective "marks" this user's notification entity as inactive
        setSendGmtDateToFarFuture();
	}
	
	private void setSendGmtDateToFarFuture() {
        // 100 years past 1970.  100 years is 3153600000000 milliseconds
        Date farFuture = new Date(3153600000000L);
        this.setSendGmtDate(farFuture);
	}
	
	// returns the index of the NotificationDetail in the list that matches the specified application ID; null otherwise
	private Integer getApplicationIdIndex(String theApplicationId) {
		Integer applicationIdIndex = null;
		
		int listSize = this.applicationIds.size();
		for(int i=0; i<listSize; i++) {
			if(this.applicationIds.get(i).equals(theApplicationId)) {
				applicationIdIndex = i;
				break;
			}
		}
		return applicationIdIndex;
	}
	
	// Algorithm: only the first log and ID are persisted for display, the additional ones are discarded.
	private void updateNotificationDetails(NotificationDetails theNewNotificationDetails, Integer applicationIdIndex) {
		log.info("updateNotificationDetails() entered");
		try {
			String applicationId = theNewNotificationDetails.getApplicationId() == null ? "" : theNewNotificationDetails.getApplicationId();
			this.applicationIds.set(applicationIdIndex, applicationId);
			
			String applicationName = theNewNotificationDetails.getApplicationName() == null ? "" : theNewNotificationDetails.getApplicationName();
			this.applicationNames.set(applicationIdIndex, applicationName);
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// ID and Message Fields updated only for the first entry (that is, when the count is going from zero to one)
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			/////////////////////////////////////////////////////////////////////////////////////
			// Counts are NOT set, but incremented based on value in notificationDetail passed in
			/////////////////////////////////////////////////////////////////////////////////////
			Integer newClientLogCount = theNewNotificationDetails.getClientLogCount();
			if(newClientLogCount > 0) {
				Integer originalClientLogCount = this.clientLogCounts.get(applicationIdIndex);
				if(originalClientLogCount == 0) {
					String clientLogMessage = theNewNotificationDetails.getClientLogMessage() == null ? "" : theNewNotificationDetails.getClientLogMessage();
					this.clientLogMessages.set(applicationIdIndex, clientLogMessage);

					String clientLogId = theNewNotificationDetails.getClientLogId() == null ? "" : theNewNotificationDetails.getClientLogId();
					this.clientLogIds.set(applicationIdIndex, clientLogId);
				}
				originalClientLogCount++;
				this.clientLogCounts.set(applicationIdIndex, originalClientLogCount);
			}
			
			Integer newCrashCount = theNewNotificationDetails.getCrashCount();
			if(newCrashCount > 0) {
				Integer originalCrashCount = this.crashCounts.get(applicationIdIndex);
				if(originalCrashCount == 0) {
					String crashMessage = theNewNotificationDetails.getCrashMessage() == null ? "" : theNewNotificationDetails.getCrashMessage();
					this.crashMessages.set(applicationIdIndex, crashMessage);

					String crashId = theNewNotificationDetails.getCrashId() == null ? "" : theNewNotificationDetails.getCrashId();
					this.crashIds.set(applicationIdIndex, crashId);
				}
				originalCrashCount++;
				this.crashCounts.set(applicationIdIndex, originalCrashCount);
			}
			
			Integer newFeedbackCount = theNewNotificationDetails.getFeedbackCount();
			if(newFeedbackCount > 0) {
				Integer originalFeedbackCount = this.feedbackCounts.get(applicationIdIndex);
				if(originalFeedbackCount == 0) {
					String feedbackMessage = theNewNotificationDetails.getFeedbackMessage() == null ? "" : theNewNotificationDetails.getFeedbackMessage();
					this.feedbackMessages.set(applicationIdIndex, feedbackMessage);

					String feedbackId = theNewNotificationDetails.getFeedbackId() == null ? "" : theNewNotificationDetails.getFeedbackId();
					this.feedbackIds.set(applicationIdIndex, feedbackId);
				}
				originalFeedbackCount++;
				this.feedbackCounts.set(applicationIdIndex, originalFeedbackCount);
			}
		} catch(IndexOutOfBoundsException e) {
			log.severe("IndexOutOfBoundsException =" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private String getEmailNotification() {
        if(this.getEmailAddress() == null) {
        	return null;
        }
    	return Emailer.getNotificationEmailBody(this.getNotificationDetailsList(), RskyboxApplication.APPLICATION_BASE_URL);
	}
	
	private String getSmsNotification() {
        if(this.getSmsEmailAddress() == null) {
        	return null;
        }

        List<NotificationDetails> notificationDetailsList = this.getNotificationDetailsList();
        boolean prior = false;
        StringBuffer smsEmailAddressBuf = new StringBuffer();
        for(NotificationDetails nd : notificationDetailsList) {
        	if(smsEmailAddressBuf != null) {
        		if(smsEmailAddressBuf.length() > 0) {smsEmailAddressBuf.append(",");}
        		smsEmailAddressBuf.append(nd.getApplicationName());
        		smsEmailAddressBuf.append("[");
        		if(nd.getCrashCount() != null) {
        			smsEmailAddressBuf.append("crash:");
        			smsEmailAddressBuf.append(nd.getCrashCount());
            		prior = true;
        		}
        		if(nd.getClientLogCount() != null) {
        			if(prior) {smsEmailAddressBuf.append("|");}
        			smsEmailAddressBuf.append("clientlog:");
        			smsEmailAddressBuf.append(nd.getClientLogCount());
            		prior = true;
        		}
        		if(nd.getFeedbackCount() != null) {
        			if(prior) {smsEmailAddressBuf.append("|");}
        			smsEmailAddressBuf.append("feedback:");
        			smsEmailAddressBuf.append(nd.getFeedbackCount());
        		}
        		smsEmailAddressBuf.append("]");
        	}
		}
        
        // include a link to the rSkybox application
        if(smsEmailAddressBuf.length() > 0) {
        	smsEmailAddressBuf.append(" ");
        	smsEmailAddressBuf.append(RskyboxApplication.APPLICATION_BASE_URL);
        }
        
        return smsEmailAddressBuf.toString();
	}
}
