package com.stretchcom.rskybox.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.stretchcom.rskybox.server.EMF;
import com.stretchcom.rskybox.server.Emailer;
import com.stretchcom.rskybox.server.GMT;
import com.stretchcom.rskybox.server.RskyboxApplication;

// Refactor: 8/25/2013
// -------------------
// The Notification entity in the datastore was not working because the same Notification entry would get corrupt when new logs came in too fast
// (an entry in the datastore can only be updated 4 or 5 times every second -- which is inadequate for notifications).
//
// Notification Layered Architecture
// ---------------------------------
// Logs are potentially created very rapidly. To allow the user notification updates to keep pace, the first layer of notifications are
// implemented in memcache. A memcache cron job runs every minute and merges notification strings in memcache into the Notification entity in the
// datastore.  The datastore cron job runs every 5 minutes and posts any user notifications that are "ready".
//
// Memcache challenge
//-------------------
// Memcache storage is not reliable and it is possible that memcache can get cleared.  If this happens, accumulated notifications for the last minute
// will be lost. This design assumes that the a memcache clear happens infrequently enough that a minute of lost notifications be tolerated when
// it does occur.
//
// Two Memcache Queues: Accumulating and Merging
//----------------------------------------------
// Two memcache notification queues are needed so that notifications can accumulate by CreateLog request threads at the same time the memcache cron
// job is merging notification strings into the datastore. This makes the memcache portion of the design multi-thread safe.
//
// Accumulate Notifications
//------------------------
// As logs come in via CreateLog API requests, two memcache constructs are used to accumulate notifications. The first is a user Notification string
// which is stored in memcache with the user ID as key. The Notification string "mirrors" the notification entity in the datastore but is obviously
// a string based version. Like the Notification entity in the datastore, the Notification string is composed of multiple NotificationDetails strings.
// Again, the NotificationDetails string "mirrors" the NotificationDetails object used by the Notification entity.
//
// As new logs come in, the memcache user notification strings can quickly be accessed using the user ID as key.  If the user Notification string
// does not yet exist, it is created.  As each user notification string is created, the user ID is added to a sequential list of Pending Users also
// stored in the memcache.
//
// Merging Notifications
//---------------------
// The memcache cron job runs once a minute. The first thing it does is flip flop the accumulating and merging queues. After this point, any new logs come
// in are added to the 'new' accumulating queue which starts empty.  The cron job -- using what is now the merging queue -- goes through the Pending User
// sequential list one-by-one to see which users need merging.  Then, for each pending user, the notification string is merged into the associated
// Notification entity in the datastore.  The pending user list and all notification strings in merging queue are deleted and the memcache cron job finishes.
// The merginq queue is now empty and is ready to become the accumulating queue at the beginning of the next cycle of the memcache cron job.
//
// Sending Notifications
// ---------------------
// The datastore cron job runs once every 5 minutes.  It queries for any notifications that are ready to send using the sendGmtDate and sends the
// notifications to the appropriate users. Currently, the notification frequency is every 5 minutes. Eventually, each user will be able to configure
// their own notification frequency based on parameters like severity.

@Entity
@NamedQueries({
    @NamedQuery(
    		name="Notification.getByKey",
    		query="SELECT n FROM Notification n WHERE n.key = :key"
    ),
    @NamedQuery(
    		name="Notification.getAll",
    		query="SELECT n FROM Notification n LIMIT 1000"
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
	public static final String UPDATED_LOG = "updatedLog";
	
	public static final int DEFAULT_NOTIFICATION_PERIOD = 5;
	private static final String ACCUMULATE_QUEUE_INDICATOR_KEY = "accumulateQueueIndicator";
	private static final String BASE_PENDING_USER_LIST_COUNTER_KEY = "_pendingUsersListCounter";
	private static final String BASE_PENDING_USER_LIST_ENTRY_KEY = "_pendingUserListEntry";
	private static final String BASE_NOTIFICATION_STRING_KEY = "_notificationString";
	
	private static final String QUEUE1 = "q1";
	private static final String QUEUE2 = "q2";
	private static final String FIELD_DELIMITER = "_!~";							// designed to have no regex special characters
	private static final String ENCODED_FIELD_DELIMITER = "@#%";					// designed to have no regex special characters
	private static final String NOTIFICATION_DETAILS_DELIMITER = "__!!~~";			// designed to have no regex special characters
	private static final String ENCODED_NOTIFICATION_DETAILS_DELIMITER = "@@##%%";	// designed to have no regex special characters
	
	private static Pattern fieldDelimiterPattern = Pattern.compile(FIELD_DELIMITER);
	private static Pattern encodedFieldDelimiterPattern = Pattern.compile(ENCODED_FIELD_DELIMITER);
	private static Pattern notificationDetailsDelimiterPattern = Pattern.compile(NOTIFICATION_DETAILS_DELIMITER);
	private static Pattern encodedNotificationDetailsDelimiterPattern = Pattern.compile(ENCODED_NOTIFICATION_DETAILS_DELIMITER);


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
	private List<Integer> updatedLogCounts;
	
	@Basic
	private List<String> updatedLogMessages;
	
	@Basic
	private List<String> updatedLogIds;
	
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
			
			// TODO
			// if anything corrupt below, maybe need to delete the record from the datastore

			///////////////////////////////////////////////////////////////////////
			// Convert "default" values stored in Big Table to "normal Java" values
			///////////////////////////////////////////////////////////////////////
			String applicationId = null;
			if(this.applicationIds.size() > i) {
				applicationId = this.applicationIds.get(i).equals("") ? null : this.applicationIds.get(i);
			} else {
				log.severe("applicationIds array size corrupt: i = " + i + " array size = " + this.applicationIds.size());
			}
			nd.setApplicationId(applicationId);
			
			String applicationName = null;
			if(this.applicationNames.size() > i) {
				applicationName = this.applicationNames.get(i).equals("") ? null : this.applicationNames.get(i);
			} else {
				log.severe("applicationNames array size corrupt: i = " + i + " array size = " + this.applicationNames.size());
			}
			nd.setApplicationName(applicationName);
			
			Integer clientLogCount = null;
			if(this.clientLogCounts.size() > i) {
				clientLogCount = this.clientLogCounts.get(i);
			} else {
				log.severe("clientLogCounts array size corrupt: i = " + i + " array size = " + this.clientLogCounts.size());
			}
			nd.setClientLogCount(clientLogCount);
			
			String clientLogMessage = null;
			if(this.clientLogMessages.size() > i) {
				clientLogMessage = this.clientLogMessages.get(i).equals("") ? null : this.clientLogMessages.get(i);
			} else {
				log.severe("client log messages array size corrupt: i = " + i + " array size = " + this.clientLogMessages.size());
			}
			nd.setClientLogMessage(clientLogMessage);
			
			String clientLogId = null;
			if(this.clientLogIds.size() > i) {
				clientLogId = this.clientLogIds.get(i).equals("") ? null : this.clientLogIds.get(i);
			} else {
				log.severe("clientLogIds array size corrupt: i = " + i + " array size = " + this.clientLogIds.size());
			}
			nd.setClientLogId(clientLogId);
			
			Integer updatedLogCount = null;
			if(this.updatedLogCounts.size() > i) {
				updatedLogCount = this.updatedLogCounts.get(i);
			} else {
				log.severe("updatedLogCounts array size corrupt: i = " + i + " array size = " + this.updatedLogCounts.size());
			}
			nd.setUpdatedLogCount(updatedLogCount);
			
			String updatedLogMessage = null;
			if(this.updatedLogMessages.size() > i) {
				updatedLogMessage = this.updatedLogMessages.get(i).equals("") ? null : this.updatedLogMessages.get(i);
			} else {
				log.severe("updated log messages array size corrupt: i = " + i + " array size = " + this.updatedLogMessages.size());
			}
			nd.setUpdatedLogMessage(updatedLogMessage);
			
			String updatedLogId = null;
			if(this.updatedLogIds.size() > i) {
				updatedLogId = this.updatedLogIds.get(i).equals("") ? null : this.updatedLogIds.get(i);
			} else {
				log.severe("updatedLogIds array size corrupt: i = " + i + " array size = " + this.updatedLogIds.size());
			}
			nd.setUpdatedLogId(updatedLogId);
			
			Integer crashCount = null;
			if(this.crashCounts.size() > i) {
				crashCount = this.crashCounts.get(i);
			} else {
				log.severe("crashCounts array size corrupt: i = " + i + " array size = " + this.crashCounts.size());
			}
			nd.setCrashCount(crashCount);
			
			String crashMessage = null;
			if(this.crashMessages.size() > i) {
				crashMessage = this.crashMessages.get(i).equals("") ? null : this.crashMessages.get(i);
			} else {
				log.severe("crash messages array size corrupt: i = " + i + " array size = " + this.crashMessages.size());
			}
			nd.setCrashMessage(crashMessage);
			
			String crashId = null;
			if(this.crashIds.size() > i) {
				crashId = this.crashIds.get(i).equals("") ? null : this.crashIds.get(i);
			} else {
				log.severe("crashIds array size corrupt: i = " + i + " array size = " + this.crashIds.size());
			}
			nd.setCrashId(crashId);
			
			Integer feedbackCount = null;
			if(this.feedbackCounts.size() > i) {
				feedbackCount = this.feedbackCounts.get(i);
			} else {
				log.severe("feedbackCounts array size corrupt: i = " + i + " array size = " + this.feedbackCounts.size());
			}
			nd.setFeedbackCount(feedbackCount);
			
			String feedbackMessage = null;
			if(this.feedbackMessages.size() > i) {
				feedbackMessage = this.feedbackMessages.get(i).equals("") ? null : this.feedbackMessages.get(i);
			} else {
				log.severe("feedback messages array size corrupt: i = " + i + " array size = " + this.feedbackMessages.size());
			}
			nd.setFeedbackMessage(feedbackMessage);
			
			String feedbackId = null;
			if(this.feedbackIds.size() > i) {
				feedbackId = this.feedbackIds.get(i).equals("") ? null : this.feedbackIds.get(i);
			} else {
				log.severe("itemIds array size corrupt: i = " + i + " array size = " + this.feedbackIds.size());
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
			log.info("updateNotificationDetailsList(): notification array sizes are zero");
			addNotificationDetails(theNewNotificationDetails);
		} else {
			log.info("updateNotificationDetailsList(): applicationIds.size = " + this.applicationIds.size());
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
	
	private void initNotificationDetails() {
		this.applicationIds = new ArrayList<String>();
		this.applicationNames = new ArrayList<String>();
		this.clientLogCounts = new ArrayList<Integer>();
		this.clientLogMessages = new ArrayList<String>();
		this.clientLogIds = new ArrayList<String>();
		this.updatedLogCounts = new ArrayList<Integer>();
		this.updatedLogMessages = new ArrayList<String>();
		this.updatedLogIds = new ArrayList<String>();
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
		Integer updatedLogCount = nd.getUpdatedLogCount() == null ? 0 : nd.getUpdatedLogCount();
		this.updatedLogCounts.add(updatedLogCount);

		String updatedLogMessage = nd.getUpdatedLogMessage() == null ? "" : nd.getUpdatedLogMessage();
		this.updatedLogMessages.add(updatedLogMessage);

		String updatedLogId = nd.getUpdatedLogId() == null ? "" : nd.getUpdatedLogId();
		this.updatedLogIds.add(updatedLogId);

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
		
		String emailAddress = nd.getEmailAddress() == null ? "" : nd.getEmailAddress();
		this.emailAddress = emailAddress;
		
		String smsEmailAddress = nd.getSmsEmailAddress() == null ? "" : nd.getSmsEmailAddress();
		this.smsEmailAddress = smsEmailAddress;
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
        	Emailer.send(this.getEmailAddress(), getEmailSubject(), emailNotification, Emailer.NO_REPLY);
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
				this.clientLogCounts.set(applicationIdIndex, originalClientLogCount+newClientLogCount);
			}
			
			Integer newUpdatedLogCount = theNewNotificationDetails.getUpdatedLogCount();
			if(newUpdatedLogCount > 0) {
				Integer originalUpdatedLogCount = this.updatedLogCounts.get(applicationIdIndex);
				if(originalUpdatedLogCount == 0) {
					String updatedLogMessage = theNewNotificationDetails.getUpdatedLogMessage() == null ? "" : theNewNotificationDetails.getUpdatedLogMessage();
					this.updatedLogMessages.set(applicationIdIndex, updatedLogMessage);

					String updatedLogId = theNewNotificationDetails.getUpdatedLogId() == null ? "" : theNewNotificationDetails.getUpdatedLogId();
					this.updatedLogIds.set(applicationIdIndex, updatedLogId);
				}
				this.updatedLogCounts.set(applicationIdIndex, originalUpdatedLogCount+newUpdatedLogCount);
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
				this.crashCounts.set(applicationIdIndex, originalCrashCount+newCrashCount);
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
				this.feedbackCounts.set(applicationIdIndex, originalFeedbackCount+newFeedbackCount);
			}
			
			String emailAddress = theNewNotificationDetails.getEmailAddress() == null ? "" : theNewNotificationDetails.getEmailAddress();
			this.emailAddress = emailAddress;
			
			String smsEmailAddress = theNewNotificationDetails.getSmsEmailAddress() == null ? "" : theNewNotificationDetails.getSmsEmailAddress();
			this.smsEmailAddress = smsEmailAddress;
		} catch(IndexOutOfBoundsException e) {
			log.severe("IndexOutOfBoundsException =" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private String getEmailNotification() {
        if(this.getEmailAddress() == null || this.getEmailAddress().trim().length() == 0) {
        	return null;
        }
    	return Emailer.getNotificationEmailBody(this.getNotificationDetailsList(), RskyboxApplication.APPLICATION_BASE_URL);
	}
	
	private String getSmsNotification() {
        if(this.getSmsEmailAddress() == null || this.getSmsEmailAddress().trim().length() == 0) {
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
        			smsEmailAddressBuf.append("log:");
        			smsEmailAddressBuf.append(nd.getClientLogCount());
            		prior = true;
        		}
//        		if(nd.getUpdatedLogCount() != null) {
//        			if(prior) {smsEmailAddressBuf.append("|");}
//        			smsEmailAddressBuf.append("updated log:");
//        			smsEmailAddressBuf.append(nd.getUpdatedLogCount());
//            		prior = true;
//        		}
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
        	smsEmailAddressBuf.append(getMostSpecificUrl());
        }
        
        return smsEmailAddressBuf.toString();
	}
	
	private String getEmailSubject() {
		StringBuffer sb = new StringBuffer();
		if(this.getNotificationDetailsList().size() > 1) {
			sb.append("new incident");
		}
		else {
			NotificationDetails nd = this.getNotificationDetailsList().get(0);
			sb.append(nd.getApplicationName());
			sb.append(" app: ");
			if(nd.getCrashCount() > 0  && (nd.getClientLogCount() == 0 && nd.getFeedbackCount() == 0)) {
				sb.append(nd.getCrashCount());
				sb.append(" new ");
				if(nd.getCrashCount() > 1) {
					sb.append("crashes");
				} else {
					sb.append("crash");
				}
			} else if(nd.getClientLogCount() > 0  && (nd.getCrashCount() == 0 && nd.getFeedbackCount() == 0)) {
				sb.append(nd.getClientLogCount());
				sb.append(" new ");
				if(nd.getClientLogCount() > 1) {
					sb.append("logs");
				} else {
					sb.append("log");
				}
			} else if(nd.getFeedbackCount() > 0  && (nd.getClientLogCount() == 0 && nd.getCrashCount() == 0)) {
				sb.append(nd.getFeedbackCount());
				sb.append(" new feedback");
			} else {
				sb.append("new incident");
			}
		}
		return sb.toString();
	}
	
	private String getMostSpecificUrl() {
        String url = "";
        // Algorithm: link provided "zooms" in to be as specific as possible without over zooming
        if(this.getNotificationDetailsList().size() > 1) {
            // if more than one application, link is to rSkybox
            url = RskyboxApplication.APPLICATION_BASE_URL;
        } else {
        	String rskyboxBaseUrl = RskyboxApplication.APPLICATION_BASE_URL + "html5";
        	NotificationDetails nd = this.getNotificationDetailsList().get(0);
        	
        	// for now, default it to the application url
        	url = rskyboxBaseUrl + "#application?appId=" + nd.getApplicationId();
        	
        	if(nd.getCrashCount() > 0  && (nd.getClientLogCount() == 0 && nd.getFeedbackCount() == 0)) {
        		if(nd.getCrashCount() == 1) {
        			// only one crash, link all the way down to the crash detail page
        			url = rskyboxBaseUrl + "#crash?id=" + nd.getCrashId() + "&appId=" + nd.getApplicationId();
        		} else {
        			// multiple crashes so link to the crash list
        			url = rskyboxBaseUrl + "#crashes?appId=" + nd.getApplicationId() + "&status=open";
        		}
        	} else if(nd.getClientLogCount() > 0  && (nd.getCrashCount() == 0 && nd.getFeedbackCount() == 0)) {
        		if(nd.getClientLogCount() == 1) {
        			// only one clientLog, link all the way down to the clientLog detail page
        			url = rskyboxBaseUrl + "#log?id=" + nd.getClientLogId() + "&appId=" + nd.getApplicationId();
        		} else {
        			// multiple logs so link to the clientLog list
        			url = rskyboxBaseUrl + "#logs?appId=" + nd.getApplicationId() + "&status=open";
        		}
        	} else if(nd.getFeedbackCount() > 0  && (nd.getClientLogCount() == 0 && nd.getCrashCount() == 0)) {
        		if(nd.getFeedbackCount() == 1) {
        			// only one feedback, link all the way down to the feedback detail page
        			url = rskyboxBaseUrl + "#feedback?id=" + nd.getFeedbackId() + "&appId=" + nd.getApplicationId();
        		} else {
        			// multiple crashes so link to the feedback list
        			url = rskyboxBaseUrl + "#feedbackList?appId=" + nd.getApplicationId() + "&status=open";
        		}
        	}
        }
    	
    	log.info("SMS URL = " + url);
        return url;
	}
	
	private static void ensureMemcacheValid(MemcacheService theMemcache) {
		if(!theMemcache.contains(ACCUMULATE_QUEUE_INDICATOR_KEY)) {
			log.info("isMemcacheValid() memcache was NOT valid, had to be initialized");
			createNotificationQueues(theMemcache);
		}
	}
	
	private static void createNotificationQueues(MemcacheService theMemcache) {
		log.info("createNotificationQueues() entered");

		// q1 is initially the accumulating queue -- it flip flops from there every cycle of the memcache cron job
		theMemcache.put(ACCUMULATE_QUEUE_INDICATOR_KEY, "q1");

		// initialize Pending User List for Accumulating queue
		String accumulatingPendingUserListCounterKey = getAccumulatingPendingUserListCounterKey(theMemcache);
		log.info("accumulatingPendingUserListCounterKey = " + accumulatingPendingUserListCounterKey);
		theMemcache.put(accumulatingPendingUserListCounterKey, 0);
		
		// initialize Pending User List for Merging queue
		String mergingPendingUserListCounterKey = getMergingPendingUserListCounterKey(theMemcache);
		log.info("mergingPendingUserListCounterKey = " + mergingPendingUserListCounterKey);
		theMemcache.put(getMergingPendingUserListCounterKey(theMemcache), 0);
	}
	
	private static String getAccumulatingQueueKey(MemcacheService theMemcache) {
		return (String)theMemcache.get(ACCUMULATE_QUEUE_INDICATOR_KEY);
	}
	
	private static String getMergingQueueKey(MemcacheService theMemcache) {
		String indicator = (String)theMemcache.get(ACCUMULATE_QUEUE_INDICATOR_KEY);
		if(indicator == null || indicator.equals(QUEUE2)) {
			return QUEUE1;
		} else {
			return QUEUE2;
		}
	}
	
	private static void flipFlopQueues(MemcacheService theMemcache) {
		String indicator = (String)theMemcache.get(ACCUMULATE_QUEUE_INDICATOR_KEY);
		if(indicator.equals(QUEUE1)) {
			log.info("flipFlopQueues(); QUEUE2 now accumulate queue");
			theMemcache.put(ACCUMULATE_QUEUE_INDICATOR_KEY, QUEUE2);
		} else {
			log.info("flipFlopQueues(); QUEUE1 now accumulate queue");
			theMemcache.put(ACCUMULATE_QUEUE_INDICATOR_KEY, QUEUE1);
		}
	}
	
	private static String getAccumulatingPendingUserListCounterKey(MemcacheService theMemcache) {
		return getAccumulatingQueueKey(theMemcache) + BASE_PENDING_USER_LIST_COUNTER_KEY;
	}
	
	private static String getMergingPendingUserListCounterKey(MemcacheService theMemcache) {
		return getMergingQueueKey(theMemcache) + BASE_PENDING_USER_LIST_COUNTER_KEY;
	}
	
	private static String getAccumulatingPendingUserListEntryKey(Integer theSequence, MemcacheService theMemcache) {
		return theSequence.toString() + "_" + getAccumulatingQueueKey(theMemcache) + BASE_PENDING_USER_LIST_ENTRY_KEY;
	}
	
	private static String getMergingPendingUserListEntryKey(Integer theSequence, MemcacheService theMemcache) {
		return theSequence.toString() + "_" + getMergingQueueKey(theMemcache) + BASE_PENDING_USER_LIST_ENTRY_KEY;
	}
	
	private static String getAccumulatingNotificationStringKey(String theUserId, MemcacheService theMemcache) {
		return theUserId + "_" + getAccumulatingQueueKey(theMemcache) + BASE_NOTIFICATION_STRING_KEY;
	}
	
	private static String getMergingNotificationStringKey(String theUserId, MemcacheService theMemcache) {
		return theUserId + "_" + getMergingQueueKey(theMemcache) + BASE_NOTIFICATION_STRING_KEY;
	}
	
	// Operates on Accumulating Queue
	private static void updateNotificationString(String theUserId, NotificationDetails theNotificationsDetails, MemcacheService theMemcache) {
		log.info("updateNotificationString() entered: userId = " + theUserId);
		log.info("theNotificationsDetails = " + theNotificationsDetails.toString());
		String notificationStringKey = getAccumulatingNotificationStringKey(theUserId, theMemcache);
		String existingNotificationString = "";
		if(!theMemcache.contains(notificationStringKey)) {
			log.info("updateNotificationString(): no prior notifications -- adding to pending user list");
			// this user has no prior notifications -- so add to the Pending User list
			String pendingUserListCounterKey = getAccumulatingPendingUserListCounterKey(theMemcache);
			log.info("pendingUserListCounterKey = " + pendingUserListCounterKey);;
			// put the userId in the Pending User sequential list which is the work list for the cron job
			Integer nextSequence = (Integer)theMemcache.get(pendingUserListCounterKey);
			log.info("nextSequence = " + nextSequence);
			String entryKey = getAccumulatingPendingUserListEntryKey(nextSequence, theMemcache);
			theMemcache.put(entryKey, theUserId);
			theMemcache.increment(pendingUserListCounterKey, 1);
		} else {
			existingNotificationString = (String)theMemcache.get(notificationStringKey);
			log.info("updateNotificationString(): existing notification string = " + existingNotificationString);
		}
		
		String newNotificationString = null;
		if(existingNotificationString.length() == 0) {
			// first user notification -- easy peasy
			newNotificationString = fromNotificationDetailsToString(theNotificationsDetails);
		} else {
			Integer targetNotificationDetailsIndex = getTargetNotificationDetails(existingNotificationString, theNotificationsDetails.getApplicationId());
			if(targetNotificationDetailsIndex == null) {
				// this notificationString does NOT have a notificationDetailsString for this application ID, so just add one to the end
				newNotificationString = existingNotificationString + fromNotificationDetailsToString(theNotificationsDetails);
			} else {
				newNotificationString = updateNotificatonDetailsString(theNotificationsDetails, existingNotificationString, targetNotificationDetailsIndex);
			}
		}
		log.info("updateNotificationString(): newNotificationString = " + newNotificationString);
		
		theMemcache.put(notificationStringKey, newNotificationString);
	}
	
	private static Integer getTargetNotificationDetails(String theExistingNotificationString, String theApplicationID) {
		Integer targetNotificationDetailsIndex = null;
		
		int notificationDetailsIndex = 0;
		while(true) {
			int appIdDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, notificationDetailsIndex);
			if(appIdDelimiterIndex == -1) {
				log.severe("getTargetNotificationDetails(): application ID field delimiter not found - should NOT happen");
				break;
			}
			
			String appId = theExistingNotificationString.substring(notificationDetailsIndex, appIdDelimiterIndex);
			if(appId.equalsIgnoreCase(theApplicationID)) {
				// found the matching NotificationDetails substring
				targetNotificationDetailsIndex = notificationDetailsIndex;
				break;
			}
			
			int notificationDetailsDelimiterIndex = theExistingNotificationString.indexOf(NOTIFICATION_DETAILS_DELIMITER, notificationDetailsIndex);
			if(notificationDetailsDelimiterIndex == -1) {
				log.severe("getTargetNotificationDetails(): notificationsDetails delimiter not found - should NOT happen");
				break;
			}
			
			notificationDetailsIndex = notificationDetailsDelimiterIndex + NOTIFICATION_DETAILS_DELIMITER.length();
			if(theExistingNotificationString.length() <= notificationDetailsIndex ) {
				// there are no more notifications details in this notification string so stop checking for match
				break;
			}
		}
		
		return targetNotificationDetailsIndex;
	}
	
	private static String fromNotificationDetailsToString(NotificationDetails theNotificationsDetails) {
		StringBuffer sb = new StringBuffer();
		if(theNotificationsDetails.getApplicationId() != null) sb.append(theNotificationsDetails.getApplicationId());
		sb.append(FIELD_DELIMITER);
		if(theNotificationsDetails.getApplicationName() != null) sb.append(theNotificationsDetails.getApplicationName());
		sb.append(FIELD_DELIMITER);
		
		if(theNotificationsDetails.getClientLogCount() != null) sb.append(theNotificationsDetails.getClientLogCount());
		sb.append(FIELD_DELIMITER);
		if(theNotificationsDetails.getClientLogMessage() != null) sb.append(encodeEmbeddedDelimiters(theNotificationsDetails.getClientLogMessage()));
		sb.append(FIELD_DELIMITER);
		if(theNotificationsDetails.getClientLogId() != null) sb.append(theNotificationsDetails.getClientLogId());
		sb.append(FIELD_DELIMITER);
		
		if(theNotificationsDetails.getUpdatedLogCount() != null) sb.append(theNotificationsDetails.getUpdatedLogCount());
		sb.append(FIELD_DELIMITER);
		if(theNotificationsDetails.getUpdatedLogMessage() != null) sb.append(encodeEmbeddedDelimiters(theNotificationsDetails.getUpdatedLogMessage()));
		sb.append(FIELD_DELIMITER);
		if(theNotificationsDetails.getUpdatedLogId() != null) sb.append(theNotificationsDetails.getUpdatedLogId());
		sb.append(FIELD_DELIMITER);
		
		if(theNotificationsDetails.getCrashCount() != null) sb.append(theNotificationsDetails.getCrashCount());
		sb.append(FIELD_DELIMITER);
		if(theNotificationsDetails.getCrashMessage() != null) sb.append(encodeEmbeddedDelimiters(theNotificationsDetails.getCrashMessage()));
		sb.append(FIELD_DELIMITER);
		if(theNotificationsDetails.getCrashId() != null) sb.append(theNotificationsDetails.getCrashId());
		sb.append(FIELD_DELIMITER);
		
		if(theNotificationsDetails.getFeedbackCount() != null) sb.append(theNotificationsDetails.getFeedbackCount());
		sb.append(FIELD_DELIMITER);
		if(theNotificationsDetails.getFeedbackMessage() != null) sb.append(encodeEmbeddedDelimiters(theNotificationsDetails.getFeedbackMessage()));
		sb.append(FIELD_DELIMITER);
		if(theNotificationsDetails.getFeedbackId() != null) sb.append(theNotificationsDetails.getFeedbackId());
		sb.append(FIELD_DELIMITER);
		
		if(theNotificationsDetails.getEmailAddress() != null) sb.append(theNotificationsDetails.getEmailAddress());
		sb.append(FIELD_DELIMITER);
		if(theNotificationsDetails.getSmsEmailAddress() != null) sb.append(theNotificationsDetails.getSmsEmailAddress());
		sb.append(FIELD_DELIMITER);
		sb.append(NOTIFICATION_DETAILS_DELIMITER);
		
		return sb.toString();
	}
	
	private static NotificationDetails fromStringToNotificationDetails(int theNotificationDetailsStartIndex, String theExistingNotificationString) {
		if(theExistingNotificationString == null) {
			return null;
		}
		
		NotificationDetails nd = new NotificationDetails();
		int startOfFieldIndex = 0;
		int fieldDelimiterIndex;
		
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): applicationID field delimiter not found - should NOT happen");
			return null;
		}
		String applicationId = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			applicationId = theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex);
		}
		nd.setApplicationId(applicationId);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): applicationName field delimiter not found - should NOT happen");
			return null;
		}
		String applicationName = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			applicationName = theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex);
		}
		nd.setApplicationName(applicationName);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		//////////////
		// Client Log
		/////////////
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): clientLogCount field delimiter not found - should NOT happen");
			return null;
		}
		Integer clientLogCount = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			try {
				clientLogCount = new Integer(theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex));
			} catch(NumberFormatException e) {
				log.severe("fromStringToNotificationDetails(): clientLogCount not an integer - should NOT happen");
			}
		}
		nd.setClientLogCount(clientLogCount);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): clientLogMessage field delimiter not found - should NOT happen");
			return null;
		}
		String clientLogMessage = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			clientLogMessage = theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex);
			clientLogMessage = decodeEmbeddedDelimiters(clientLogMessage);
		}
		nd.setClientLogMessage(clientLogMessage);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): clientLogId field delimiter not found - should NOT happen");
			return null;
		}
		String clientLogId = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			clientLogId = theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex);
		}
		nd.setClientLogId(clientLogId);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		//////////////
		// Updated Log
		//////////////
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): updatedLogCount field delimiter not found - should NOT happen");
			return null;
		}
		Integer updatedLogCount = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			try {
				updatedLogCount = new Integer(theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex));
			} catch(NumberFormatException e) {
				log.severe("fromStringToNotificationDetails(): updatedLogCount not an integer - should NOT happen");
			}
		}
		nd.setUpdatedLogCount(updatedLogCount);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): updatedLogMessage field delimiter not found - should NOT happen");
			return null;
		}
		String updatedLogMessage = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			updatedLogMessage = theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex);
			updatedLogMessage = decodeEmbeddedDelimiters(updatedLogMessage);
		}
		nd.setUpdatedLogMessage(updatedLogMessage);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): updatedLogId field delimiter not found - should NOT happen");
			return null;
		}
		String updatedLogId = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			updatedLogId = theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex);
		}
		nd.setUpdatedLogId(updatedLogId);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		////////
		// Crash
		////////
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): crashCount field delimiter not found - should NOT happen");
			return null;
		}
		Integer crashCount = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			try {
				crashCount = new Integer(theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex));
			} catch(NumberFormatException e) {
				log.severe("fromStringToNotificationDetails(): crashCount not an integer - should NOT happen");
			}
		}
		nd.setCrashCount(crashCount);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): crashMessage field delimiter not found - should NOT happen");
			return null;
		}
		String crashMessage = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			crashMessage = theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex);
			crashMessage = decodeEmbeddedDelimiters(crashMessage);
		}
		nd.setCrashMessage(crashMessage);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): crashId field delimiter not found - should NOT happen");
			return null;
		}
		String crashId = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			crashId = theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex);
		}
		nd.setCrashId(crashId);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		///////////
		// Feedback
		///////////
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): feedbackCount field delimiter not found - should NOT happen");
			return null;
		}
		Integer feedbackCount = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			try {
				feedbackCount = new Integer(theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex));
			} catch(NumberFormatException e) {
				log.severe("fromStringToNotificationDetails(): feedbackCount not an integer - should NOT happen");
			}
		}
		nd.setFeedbackCount(feedbackCount);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): feedbackMessage field delimiter not found - should NOT happen");
			return null;
		}
		String feedbackMessage = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			feedbackMessage = theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex);
			feedbackMessage = decodeEmbeddedDelimiters(feedbackMessage);
		}
		nd.setFeedbackMessage(feedbackMessage);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): feedbackId field delimiter not found - should NOT happen");
			return null;
		}
		String feedbackId = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			feedbackId = theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex);
		}
		nd.setFeedbackId(feedbackId);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		////////////////
		// Email Address
		////////////////
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): email address field delimiter not found - should NOT happen");
			return null;
		}
		String emailAddress = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			emailAddress = theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex);
		}
		nd.setEmailAddress(emailAddress);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();
		
		////////////////////
		// SMS Email Address
		////////////////////
		fieldDelimiterIndex = theExistingNotificationString.indexOf(FIELD_DELIMITER, startOfFieldIndex);
		if(fieldDelimiterIndex == -1) {
			log.severe("fromStringToNotificationDetails(): SMS email address field delimiter not found - should NOT happen");
			return null;
		}
		String smsEmailAddress = null;
		if(fieldDelimiterIndex > startOfFieldIndex) {
			smsEmailAddress = theExistingNotificationString.substring(startOfFieldIndex, fieldDelimiterIndex);
		}
		nd.setSmsEmailAddress(smsEmailAddress);
		startOfFieldIndex = fieldDelimiterIndex + FIELD_DELIMITER.length();

		return nd;
	}

	private static String updateNotificatonDetailsString(NotificationDetails theNewNotificationDetails, String theExistingNotificationString, Integer theTargetNotificationDetailsIndex) {
		log.info("updateNotificatonDetailsString() entered");
		// convert the existing, embedded NotificationDetailsString to a NotificationDetails object
		NotificationDetails existingNotificationDetails = fromStringToNotificationDetails(theTargetNotificationDetailsIndex, theExistingNotificationString);
		log.info("updateNotificatonDetailsString() existing notificationDetailsString = " + existingNotificationDetails.toString());
	
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ID and Message Fields updated only for the first entry (that is, when the count is going from zero to one)
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////////////////////////////
		// Counts are NOT set, but incremented based on value in notificationDetail passed in
		/////////////////////////////////////////////////////////////////////////////////////
		Integer newClientLogCount = theNewNotificationDetails.getClientLogCount();
		if(newClientLogCount > 0) {
			Integer originalClientLogCount = existingNotificationDetails.getClientLogCount();
			if(originalClientLogCount == 0) {
				String clientLogMessage = theNewNotificationDetails.getClientLogMessage() == null ? "" : theNewNotificationDetails.getClientLogMessage();
				existingNotificationDetails.setClientLogMessage(clientLogMessage);

				String clientLogId = theNewNotificationDetails.getClientLogId() == null ? "" : theNewNotificationDetails.getClientLogId();
				existingNotificationDetails.setClientLogId(clientLogId);
			}
			originalClientLogCount++;
			existingNotificationDetails.setClientLogCount(originalClientLogCount);
		}
		
		Integer newUpdatedLogCount = theNewNotificationDetails.getUpdatedLogCount();
		if(newUpdatedLogCount > 0) {
			Integer originalUpdatedLogCount = existingNotificationDetails.getUpdatedLogCount();
			if(originalUpdatedLogCount == 0) {
				String updatedLogMessage = theNewNotificationDetails.getUpdatedLogMessage() == null ? "" : theNewNotificationDetails.getUpdatedLogMessage();
				existingNotificationDetails.setUpdatedLogMessage(updatedLogMessage);

				String updatedLogId = theNewNotificationDetails.getUpdatedLogId() == null ? "" : theNewNotificationDetails.getUpdatedLogId();
				existingNotificationDetails.setUpdatedLogId(updatedLogId);
			}
			originalUpdatedLogCount++;
			existingNotificationDetails.setUpdatedLogCount(originalUpdatedLogCount);
		}
		
		Integer newCrashCount = theNewNotificationDetails.getCrashCount();
		if(newCrashCount > 0) {
			Integer originalCrashCount = existingNotificationDetails.getCrashCount();
			if(originalCrashCount == 0) {
				String crashMessage = theNewNotificationDetails.getCrashMessage() == null ? "" : theNewNotificationDetails.getCrashMessage();
				existingNotificationDetails.setCrashMessage(crashMessage);

				String crashId = theNewNotificationDetails.getCrashId() == null ? "" : theNewNotificationDetails.getCrashId();
				existingNotificationDetails.setCrashId(crashId);
			}
			originalCrashCount++;
			existingNotificationDetails.setCrashCount(originalCrashCount);
		}
		
		Integer newFeedbackCount = theNewNotificationDetails.getFeedbackCount();
		if(newFeedbackCount > 0) {
			Integer originalFeedbackCount = existingNotificationDetails.getFeedbackCount();
			if(originalFeedbackCount == 0) {
				String feedbackMessage = theNewNotificationDetails.getFeedbackMessage() == null ? "" : theNewNotificationDetails.getFeedbackMessage();
				existingNotificationDetails.setFeedbackMessage(feedbackMessage);

				String feedbackId = theNewNotificationDetails.getFeedbackId() == null ? "" : theNewNotificationDetails.getFeedbackId();
				existingNotificationDetails.setFeedbackId(feedbackId);
			}
			originalFeedbackCount++;
			existingNotificationDetails.setFeedbackCount(originalFeedbackCount);
		}
		
		// email and smsEmail are always set/overwritten to the most current value
		existingNotificationDetails.setEmailAddress(theNewNotificationDetails.getEmailAddress());
		existingNotificationDetails.setSmsEmailAddress(theNewNotificationDetails.getSmsEmailAddress());
		
		log.info("updateNotificatonDetailsString() updated notificationDetailsString = " + existingNotificationDetails.toString());
		
		String newNotificationString = replaceNotificatonDetailsString(existingNotificationDetails, theExistingNotificationString, theTargetNotificationDetailsIndex);
		log.info("updateNotificatonDetailsString() updated notificationString = " + newNotificationString);
		return newNotificationString;
	}

	private static String replaceNotificatonDetailsString(NotificationDetails theNewNotificationDetails, String theExistingNotificationString, Integer theTargetNotificationDetailsIndex) {
		String newNotificationDetailsString = fromNotificationDetailsToString(theNewNotificationDetails);
		
		// find the ending index of the old notificationDetailsString (we already know where it starts)
		
		int notificationDetailsdelimiterIndex = theExistingNotificationString.indexOf(NOTIFICATION_DETAILS_DELIMITER, theTargetNotificationDetailsIndex);
		if(notificationDetailsdelimiterIndex == -1) {
			log.severe("replaceNotificatonDetailsString(): notificationsDetails delimiter not found - should NOT happen");
			// deal with this by just returning the original NotificationString
			return theExistingNotificationString;
		}
		
		int nextNotificationDetailsIndex = notificationDetailsdelimiterIndex + NOTIFICATION_DETAILS_DELIMITER.length();
		
		// use substring and rebuild the NotificationString from the parts
		StringBuffer sb = new StringBuffer();
		if(theTargetNotificationDetailsIndex > 0) {
			// copy the NotificationDetailsStrings in the NotificationString BEFORE the one being replaced
			sb.append(theExistingNotificationString.substring(0, theTargetNotificationDetailsIndex));
		}
		// replace the notificationDetailsString with the new one
		sb.append(newNotificationDetailsString);
		if(theExistingNotificationString.length() > nextNotificationDetailsIndex ) {
			// copy the NotificationDetailsStrings in the NotificationString AFTER the one being replaced
			sb.append(theExistingNotificationString.substring(nextNotificationDetailsIndex));
		}
		
		return sb.toString();
	}
	
	private static String encodeEmbeddedDelimiters(String theStringToEncode) {
		if(theStringToEncode == null || theStringToEncode.length() == 0) {
			return theStringToEncode;
		}
		String encodedString = null;
		
		// first encode the any field delimiters embedded in theStringToEncode
		Matcher fieldMatcher = fieldDelimiterPattern.matcher(theStringToEncode);
		encodedString = fieldMatcher.replaceAll(ENCODED_FIELD_DELIMITER);
		
		// now encode the any notificationDetails delimiters embedded in theStringToEncode
		Matcher ndMatcher = notificationDetailsDelimiterPattern.matcher(encodedString);
		return ndMatcher.replaceAll(ENCODED_NOTIFICATION_DETAILS_DELIMITER);
	}
	
	private static String decodeEmbeddedDelimiters(String theStringToDecode) {
		if(theStringToDecode == null || theStringToDecode.length() == 0) {
			return theStringToDecode;
		}
		String decodedString = null;
		
		// first decode the any encoded field delimiters embedded in theStringToDecode
		Matcher fieldMatcher = encodedFieldDelimiterPattern.matcher(theStringToDecode);
		decodedString = fieldMatcher.replaceAll(FIELD_DELIMITER);
		
		// now decode the any encoded notificationDetails delimiters embedded in theStringToDecode
		Matcher ndMatcher = encodedNotificationDetailsDelimiterPattern.matcher(decodedString);
		return ndMatcher.replaceAll(NOTIFICATION_DETAILS_DELIMITER);
	}
	
	////////////////////
	// Major ENTRY POINT
	////////////////////
	// Operates on Accumulating Queue
	// Called by the CreateLog threads to queue notifications
	public static void queueNotification(User theUser, String theApplicationId, AppMember theAppMember, String theNotificationType, 
                                         String theMessage, String theIncidentId, String theEmailAddress, String theSmsEmailAddress) {
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		ensureMemcacheValid(memcache);
        
        String userId = null;
        try {
        	userId = KeyFactory.keyToString(theUser.getKey());
		} catch (IllegalArgumentException e1) {
			log.severe("exception = " + e1.getMessage());
			e1.printStackTrace();
			return;
		}
		
    	NotificationDetails notificationDetails = new NotificationDetails();
    	notificationDetails.setApplicationId(theApplicationId);
    	notificationDetails.setApplicationName(theAppMember.getApplicationName());
    	notificationDetails.setEmailAddress(theEmailAddress);
    	notificationDetails.setSmsEmailAddress(theSmsEmailAddress);
    	
    	notificationDetails.setCrashCount(0);
    	notificationDetails.setClientLogCount(0);
    	notificationDetails.setUpdatedLogCount(0);
    	notificationDetails.setFeedbackCount(0);

    	if(theNotificationType.equalsIgnoreCase(Notification.CRASH)) {
    		notificationDetails.setCrashCount(1);
        	notificationDetails.setCrashMessage(theMessage);
        	notificationDetails.setCrashId(theIncidentId);
    	} else if(theNotificationType.equalsIgnoreCase(Notification.CLIENT_LOG)) {
    		notificationDetails.setClientLogCount(1);
        	notificationDetails.setClientLogMessage(theMessage);
        	notificationDetails.setClientLogId(theIncidentId);
    	} else if(theNotificationType.equalsIgnoreCase(Notification.UPDATED_LOG)) {
    		notificationDetails.setUpdatedLogCount(1);
        	notificationDetails.setUpdatedLogMessage(theMessage);
        	notificationDetails.setUpdatedLogId(theIncidentId);
    	} else if(theNotificationType.equalsIgnoreCase(Notification.FEEDBACK)) {
    		notificationDetails.setFeedbackCount(1);
        	notificationDetails.setFeedbackMessage(theMessage);
        	notificationDetails.setFeedbackId(theIncidentId);
    	}
    	updateNotificationString(userId, notificationDetails, memcache);
	}

	////////////////////
	// Major ENTRY POINT
	////////////////////
	// called by the memcache cron job to merge memcache queued notifications into the datastore Notification entities
	public static Integer mergeQueuedNotifications() {
		log.info("mergeQueuedNotifications(); entered");
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		ensureMemcacheValid(memcache);
		
		// accumulating queue becomes merging and merging queue become accumulating  allowing both activities to proceed simultaneously
		flipFlopQueues(memcache);
		
		// walk thru the Merging Queue's Pending Users list. For each pending user, get the associated NotificationString and merge that into the datastore
		String pendingUserListCounterKey = getMergingPendingUserListCounterKey(memcache);
		Integer puCount = (Integer)memcache.get(pendingUserListCounterKey);
		log.info("number of pending users needing to be merged = " + puCount);
		int index;
		for(index=0; index<puCount; index++) {
			String pendingUserKey = getMergingPendingUserListEntryKey(index, memcache);
			String userId = (String)memcache.get(pendingUserKey);
			String notificationStringKey = getMergingNotificationStringKey(userId, memcache);
			String notificationString = (String)memcache.get(notificationStringKey);
			if(notificationString == null) {
				log.severe("notificationString at index = " + index + " is null. Number of pending users = " + puCount);
				log.severe("pendingUserKey = " + pendingUserKey + "  notificationStringKey = " + notificationStringKey);
			} else {
				mergeNotificationString(userId, notificationString);
			}
			
			// empty the memcache elements no longer needed
			memcache.delete(pendingUserKey);
			memcache.delete(notificationStringKey);
		}
		log.info("number of pending users merged = " + index);
		memcache.put(pendingUserListCounterKey, 0);
		return index;
	}
	
	// returns the index of the next NotificationDetailsString or null if there are no more
	private static Integer getNextNotificationDetails(String theExistingNotificationString, Integer theStartingOffset) {
		Integer nextNotificationDetailsIndex = null;
		
		int notificationDetailsDelimiterIndex = theExistingNotificationString.indexOf(NOTIFICATION_DETAILS_DELIMITER, theStartingOffset);
		if(notificationDetailsDelimiterIndex == -1) {
			log.severe("getNextNotificationDetails(): notificationsDetails delimiter not found - should NOT happen");
			return null;
		}
		nextNotificationDetailsIndex = notificationDetailsDelimiterIndex + NOTIFICATION_DETAILS_DELIMITER.length();
		if(nextNotificationDetailsIndex >= theExistingNotificationString.length()) {
			nextNotificationDetailsIndex = null;
		}
		return nextNotificationDetailsIndex;
	}

	
	private static void mergeNotificationString(String theUserId, String theNotificationString) {
		log.info("mergeNotificationString(); entered");

        EntityManager em = EMF.get().createEntityManager();

		em.getTransaction().begin();
        try {
        	Notification notification = null;
        	try {
            	notification = (Notification)em.createNamedQuery("Notification.getByUserId")
        				.setParameter("userId", theUserId)
        				.getSingleResult();
            	log.info("mergeNotificationString(); existing notification found in datastore");
        	} catch (NoResultException e) {
    			// this is NOT an error, just the very first time a notification is being sent. Notification will be created just below ...
        		log.info("mergeNotificationString(); no existing notification found in datastore so it will be added");
    		} catch (NonUniqueResultException e) {
    			log.severe("mergeNotificationString(): should never happen - two or more Users have the same key. exception = " + e.getMessage());
     		}
        	
        	//////////////////////////////////////////////////////////////////////
        	// there is no Notification entity for this user yet, so create it now
        	//////////////////////////////////////////////////////////////////////
        	if(notification == null) {
        		log.info("mergeNotificationString(); new notification instantiated");
        		notification = new Notification();
        		notification.setUserId(theUserId);
        		notification.setSendGmtDateToFarFuture();  // to start, entity for this user is inactive
        	}
        	
        	Integer nextNotificationDetailsIndex = 0;
        	do {
        		NotificationDetails nd = fromStringToNotificationDetails(nextNotificationDetailsIndex, theNotificationString);
            	notification.updateNotificationDetailsList(nd);
        		nextNotificationDetailsIndex = getNextNotificationDetails(theNotificationString, nextNotificationDetailsIndex);
        	} while (nextNotificationDetailsIndex != null);
        	
        	// check if sendGmtDate needs to be updated
        	if(!GMT.isDateBeforeNowPlusOffsetMinutes(notification.getSendGmtDate(), DEFAULT_NOTIFICATION_PERIOD)) {
        		log.info("mergeNotificationString(); setting sendGmtDate to 5 minutes in the future");
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
}
