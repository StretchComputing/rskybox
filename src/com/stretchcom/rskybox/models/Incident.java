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
import org.json.JSONObject;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.stretchcom.rskybox.server.EMF;
import com.stretchcom.rskybox.server.GMT;

@Entity
@NamedQueries({
    @NamedQuery(
    		name="Incident.getAll",
    		query="SELECT i FROM Incident i ORDER BY i.lastUpdatedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Incident.getAllWithApplicationId",
    		query="SELECT i FROM Incident i WHERE i.applicationId = :applicationId ORDER BY i.lastUpdatedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Incident.getByApplicationIdAndEventNameAndTag",
    		query="SELECT i FROM Incident i WHERE " +
    		      "i.applicationId = :applicationId" + " AND " +
    		      "i.eventName = :eventName" + " AND " +
    			  "i.tags = :tag ORDER BY i.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="Incident.getAllWithApplicationIdAndTag",
    		query="SELECT i FROM Incident i WHERE " +
    		"i.applicationId = :applicationId" + " AND " +
    		"i.tags = :tag ORDER BY i.lastUpdatedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Incident.getByStatus",
    		query="SELECT i FROM Incident i WHERE i.status = :status  ORDER BY i.lastUpdatedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Incident.getByStatusAndApplicationId",
    		query="SELECT i FROM Incident i WHERE i.status = :status and i.applicationId = :applicationId ORDER BY i.lastUpdatedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Incident.getByStatusAndApplicationIdAndTag",
    		query="SELECT i FROM Incident i WHERE " +  
    				"i.status = :status" + " AND " + 
    				"i.applicationId = :applicationId" + " AND " +  
    				"i.tags = :tag ORDER BY i.lastUpdatedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Incident.getByKey",
    		query="SELECT i FROM Incident i WHERE i.key = :key"
    ),
    @NamedQuery(
    		name="Incident.getByApplicationId",
    		query="SELECT i FROM Incident i WHERE i.applicationId = :applicationId"
    ),
	@NamedQuery(
    		name="Incident.getOldActiveThru",
    		query="SELECT i FROM Incident i WHERE " + 
    				"i.activeThruGmtDate < :currentDate"  + " AND " +
    				"i.status = :status"
      ),
    @NamedQuery(
    		name="Incident.getByActiveThruGmtDateIsNull",
      		query="SELECT i FROM Incident i WHERE i.activeThruGmtDate = NULL"
    ),
})
public class Incident {
	private static final Logger log = Logger.getLogger(Incident.class.getName());
	
	public final static String OPEN_STATUS = "open";
	public final static String CLOSED_STATUS = "closed";
	public final static String ALL_STATUS = "all";

	public final static Integer CRITICAL_SEVERITY = 10;
	public final static Integer HIGH_SEVERITY = 8;
	public final static Integer MEDIUM_SEVERITY = 5;
	public final static Integer LOW_SEVERITY = 3;
	public final static Integer MINIMUM_SEVERITY = 1;
	public final static Integer INITIALIZATION_SEVERITY = 0;
	
	public final static String CRASH_TAG = "crash";
	public final static String LOG_TAG = "log";
	public final static String FEEDBACK_TAG = "feedback";
	
	public final static String ACTIVE_REMOTE_CONTROL_MODE = "active";
	public final static String INACTIVE_REMOTE_CONTROL_MODE = "inactive";

	private Integer number;  // sequential number auto assigned to incidents with scope of the application
	private String eventName;
	private Integer eventCount;
	private Boolean maxEventCountReached = false;
	private Integer severity = Incident.MINIMUM_SEVERITY;
	private Integer oldSeverity = Incident.INITIALIZATION_SEVERITY;
	private Integer severityUpVotes;
	private Integer severityDownVotes;
	private Date lastUpdatedGmtDate;
	private Date createdGmtDate;
	private String endUser;
	private String status = Incident.CLOSED_STATUS;  // initialization code will "changeStatus()" 
	private String applicationId;
	private Date activeThruGmtDate;  // Active thru this date.  Application specific.
	private Boolean inStatsOnlyMode = false;
	private Boolean wasAutoClosed = false;
	private String remoteControlMode = Incident.ACTIVE_REMOTE_CONTROL_MODE;
	private String summary;
	private String message;

	///////////////////////////////////////
	// place holder for future properties
	///////////////////////////////////////
	private String resolution;
	private Boolean wasResolved = false;
	private String githubUrl;
	@Basic
	private List<String> comments;	
	@Basic
	private List<Key> releatedIncidents;	
	///////////////////////////////////////
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;
	
	// well defined tags constants defined above
	// well defined tags are mutually exclusive, but 'tags' can hold multiple user defined tags
	@Basic
	private List<String> tags;

	public Key getKey() {
        return key;
    }

    public Integer getEventCount() {
		return eventCount;
	}

	public void setEventCount(Integer eventCount) {
		this.eventCount = eventCount;
	}
	public Boolean getMaxEventCountReached() {
		return maxEventCountReached;
	}

	public void setMaxEventCountReached(Boolean maxEventCountReached) {
		this.maxEventCountReached = maxEventCountReached;
	}

	
	public void incrementEventCount(Integer theMaxEventsPerIncident) {
		this.eventCount++;
		log.info("incremented incident event count = " + this.eventCount + " theMaxEventsPerIncident = " + theMaxEventsPerIncident);
		if(eventCount >= theMaxEventsPerIncident) {
			this.maxEventCountReached = true;
		}
	}

	public Date getLastUpdatedGmtDate() {
		return lastUpdatedGmtDate;
	}

	public void setLastUpdatedGmtDate(Date lastUpdatedGmtDate) {
		this.lastUpdatedGmtDate = lastUpdatedGmtDate;
	}

	public Date getCreatedGmtDate() {
		return createdGmtDate;
	}

	public void setCreatedGmtDate(Date createdGmtDate) {
		this.createdGmtDate = createdGmtDate;
	}

	public String getEndUser() {
		return endUser;
	}

	public void setEndUser(String endUser) {
		this.endUser = endUser;
	}
	
	public String getStatus() {
		return status;
	}

	public void changeStatus(String theWellKnownTag, String theNewStatus, Application theApplication) {
		// TODO ::ROBUSTNESS:: sometimes when changeStatus is called, the incident may not have been updated yet and it could still fail so this could cause
		// the application count of its incidents to get out of synch
		
		// if status is not changing, book out
		if(this.status.equalsIgnoreCase(theNewStatus)) {return;}
		
		Boolean isIncrement = true;
		if(this.status.equalsIgnoreCase(Incident.OPEN_STATUS) && theNewStatus.equalsIgnoreCase(Incident.CLOSED_STATUS)) {
			// if changing from open to closed, decrement active incident count
			isIncrement =false;
		}
		Application.adjustOpenEventCount(theWellKnownTag, isIncrement, theApplication.getId());
		
		this.status = theNewStatus;
	}
	
	// DON'T call this method unless you are the CRON job!!!!!
	public void hiddenSetStatus(String theNewStatus) {
		this.status = theNewStatus;
	}
	
	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}
	
	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public Date getActiveThruGmtDate() {
		return activeThruGmtDate;
	}

	public void setActiveThruGmtDate(Date activeThruGmtDate) {
		this.activeThruGmtDate = activeThruGmtDate;
	}

    public Integer getSeverity() {
		return severity;
	}

	public void setSeverity(Integer severity) {
		this.severity = severity;
	}
	
	public Integer getOldSeverity() {
		return oldSeverity;
	}

	public void setOldSeverity(Integer oldSeverity) {
		this.oldSeverity = oldSeverity;
	}

	public Integer getSeverityUpVotes() {
		return severityUpVotes;
	}

	public void setSeverityUpVotes(Integer severityUpVotes) {
		this.severityUpVotes = severityUpVotes;
	}

	public Integer getSeverityDownVotes() {
		return severityDownVotes;
	}

	public void setSeverityDownVotes(Integer severityDownVotes) {
		this.severityDownVotes = severityDownVotes;
	}

	public Boolean getInStatsOnlyMode() {
		return inStatsOnlyMode;
	}

	public void setInStatsOnlyMode(Boolean inStatsOnlyMode) {
		this.inStatsOnlyMode = inStatsOnlyMode;
	}

	public Boolean getWasAutoClosed() {
		return wasAutoClosed;
	}

	public void setWasAutoClosed(Boolean wasAutoClosed) {
		this.wasAutoClosed = wasAutoClosed;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getRemoteControlMode() {
		return remoteControlMode;
	}

	public void setRemoteControlMode(String remoteControlMode) {
		this.remoteControlMode = remoteControlMode;
	}
	
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	// merges the provided tag list into the existing tag list
	public Boolean addToTags(List<String> theNewTagList) {
		if(theNewTagList == null || theNewTagList.size() == 0) {
			return false;
		}
		
		for(String nt : theNewTagList) {
			if(!this.tags.contains(nt)) {
				this.tags.add(nt);
			}
		}
		
		return true;
	}

	// returns true if the tag was added to the list; false otherwise
	public Boolean addToTags(String theNewTag) {
		// if there is no tag list, create it
		if(this.tags == null) {this.tags = new ArrayList<String>();}
		
		// only add the tag if it is not already in the list
		if(!this.tags.contains(theNewTag)) {
			this.tags.add(theNewTag);
			return true;
		}
		
		return false;
	}
	
	public String getId() {
		return KeyFactory.keyToString(this.key);
	}
	
	public Boolean isLog() {
		if(this.tags.contains(Incident.LOG_TAG)) {return true;}
		return false;
	}
	
	public Boolean isCrash() {
		if(this.tags.contains(Incident.CRASH_TAG)) {return true;}
		return false;
	}
	
	public Boolean isFeedback() {
		if(this.tags.contains(Incident.FEEDBACK_TAG)) {return true;}
		return false;
	}
	
	public String getNotificationTypeFromTag() {
    	String wellKnownTag = this.getWellKnownTag();
    	String notificationType = Notification.CLIENT_LOG;
    	if(wellKnownTag.equalsIgnoreCase(Notification.CRASH)) {
    		notificationType = Notification.CRASH;
    	} else if(wellKnownTag.equalsIgnoreCase(Notification.FEEDBACK)) {
    		notificationType = Notification.FEEDBACK;
    	}
    	return notificationType;
	}
	
	public JSONObject getJson() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("id", this.getId());
			jsonObject.put("number", this.number);
			jsonObject.put("status", this.status);
			jsonObject.put("severity", this.severity);
			jsonObject.put("name", this.eventName);
			jsonObject.put("lastUpdatedDate", GMT.convertToIsoDate(this.lastUpdatedGmtDate));
			jsonObject.put("createdDate", GMT.convertToIsoDate(this.createdGmtDate));
			
        	JSONArray tagsJsonArray = new JSONArray();
        	for(String tag : this.tags) {
        		tagsJsonArray.put(tag);
        	}
			jsonObject.put("tags", tagsJsonArray);
			
			jsonObject.put("eventCount", this.eventCount);
			jsonObject.put("message", this.message);
			jsonObject.put("summary", this.summary);
			jsonObject.put("mode", this.remoteControlMode);
			
		} catch (JSONException e) {
			log.severe("exception building Incident JSON object, message = " + e.getMessage());
		}
		return jsonObject;
	}
	
	public static Boolean isModeValid(String theMode) {
		if(theMode.equalsIgnoreCase(Incident.ACTIVE_REMOTE_CONTROL_MODE) || theMode.equalsIgnoreCase(Incident.INACTIVE_REMOTE_CONTROL_MODE)) return true;
		return false;
	}
	
	public static Boolean isStatusValid(String theStatus) {
		if(theStatus.equalsIgnoreCase(Incident.OPEN_STATUS) || theStatus.equalsIgnoreCase(Incident.CLOSED_STATUS)) return true;
		return false;
	}
	
	public static Boolean isStatusParameterValid(String theStatus) {
		if(theStatus.equalsIgnoreCase(Incident.OPEN_STATUS) || theStatus.equalsIgnoreCase(Incident.CLOSED_STATUS) || theStatus.equalsIgnoreCase(Incident.ALL_STATUS)) return true;
		return false;
	}
	
	public static Boolean isSeverityValid(Integer theSeverity) {
		if(theSeverity >= Incident.MINIMUM_SEVERITY && theSeverity <= Incident.CRITICAL_SEVERITY) return true;
		return false;
	}
	
	public static Boolean isWellKnownTagValid(String theTag) {
		if(theTag.equalsIgnoreCase(Incident.CRASH_TAG) || theTag.equalsIgnoreCase(Incident.LOG_TAG) || theTag.equalsIgnoreCase(Incident.FEEDBACK_TAG)) return true;
		return false;
	}
	
	public static Integer getWellKnownTagCount(List<String> theTags) {
		// must contain exactly one well known tag
		int crashCount = 0;
		int logCount = 0;
		int feedbackCount = 0;
		for(String tag : theTags) {
			if(tag.equalsIgnoreCase(Incident.CRASH_TAG)) {crashCount++;}
			else if(tag.equalsIgnoreCase(Incident.LOG_TAG)) {logCount++;}
			else if(tag.equalsIgnoreCase(Incident.FEEDBACK_TAG)) {feedbackCount++;}
		}
		int totalCount = crashCount + logCount + feedbackCount;
		return totalCount;
	}
	
	public String getWellKnownTag() {
		String wellKnownTag = null;
		for(String tag : this.tags) {
			if(tag.equalsIgnoreCase(Incident.CRASH_TAG) || tag.equalsIgnoreCase(Incident.LOG_TAG) ||tag.equalsIgnoreCase(Incident.FEEDBACK_TAG)) {
				wellKnownTag = tag;
				break;
			}
		}
		return wellKnownTag;
	}
	
	// pretty much guaranteed to return an Incident (short of a server error)
	// either finds the 'owning' incident associated with the specified event or creates a new incident
	public static Incident fetchIncidentIncrementCount(String theEventName, String theWellKnownTag, String theIncidentId, Application theApplication, String theMessage, String theSummary) {
		Incident eventOwningIncident = null;
		List<Incident> relatedIncidents = null;
        EntityManager em = EMF.get().createEntityManager();
        Date now = new Date();
        Boolean isExistingIncident = true;
        Boolean severityChanged = false;
		String severityMsg = "";
        
        try {
            if(theIncidentId != null) {
            	try {
        			eventOwningIncident = (Incident)em.createNamedQuery("Incident.getByKey")
        					.setParameter("key", KeyFactory.stringToKey(theIncidentId))
        					.getSingleResult();
            	} catch (NoResultException e) {
        			// this incident ID provided was not valid
        			return null;
        		} catch (NonUniqueResultException e) {
        			log.severe("should never happen - two or more incidents have same key");
        			return null;
        		}
            } else {
        		try {
        			relatedIncidents = (List<Incident>)em.createNamedQuery("Incident.getByApplicationIdAndEventNameAndTag")
        				.setParameter("applicationId", theApplication.getId())
        				.setParameter("eventName", theEventName)
        				.setParameter("tag", theWellKnownTag)
        				.getResultList();
        			if(relatedIncidents.size() == 0) {
            			// this is NOT an error -- there is just no incident yet associated with this event so create one
        				log.info("incident matching event name = " + theEventName + " NOT found. Creating a new incident.");
            			eventOwningIncident = Incident.createIncident(theEventName, theWellKnownTag, theApplication, theMessage, theSummary);
        				isExistingIncident = false;
        			} else {
            			// always choose the most recently created incident which will be on the top of the list
        				log.info("incident matching event name = " + theEventName + " WAS found");
            			eventOwningIncident = relatedIncidents.get(0);
         			}
        		}catch (Exception e) {
        			log.severe("should never happen - two or more google account users have same key");
        			return null;
        		}
            }
    		
    		// if incident recently closed, re-open it. Closed too long, then create a new one.
            // TODO maybe only re-open if it was auto closed
    		if(eventOwningIncident.getStatus().equalsIgnoreCase(Incident.CLOSED_STATUS)) {
    			Date finalRevivalDate = GMT.addDaysToDate(eventOwningIncident.getLastUpdatedGmtDate(), theApplication.getDaysInLimbo());
    			if(finalRevivalDate.after(now)) {
    				// reopen this puppy
    				eventOwningIncident.changeStatus(theWellKnownTag, Incident.OPEN_STATUS, theApplication);
    				log.info("fetchIncidentIncrementCount() reopening existing incident");
    			} else {
    				// create a new incident
    				eventOwningIncident = Incident.createIncident(theEventName, theWellKnownTag, theApplication, theMessage, theSummary);
    				isExistingIncident = false;
    				log.info("fetchIncidentIncrementCount() existing incident CLOSED and too old to reopen");
    			}
    		}
    		
    		if(isExistingIncident){
    			log.info("existing incident -- incident details are being updated");
    			eventOwningIncident.incrementEventCount(theApplication.getMaxEventsPerIncident());
    			eventOwningIncident.setLastUpdatedGmtDate(now);
    			
    			// TODO enhance message by merging summaries?
    		}
			
			// update severity if appropriate
			severityChanged = checkForSeverityUpdate(eventOwningIncident, theApplication);
			if(severityChanged) {
				// two scenarios to deal with
				// 1. this is a new incident (old severity = new severity)
				// 2. change in severity for an existing incident (old severity != new severity)
				if(eventOwningIncident.getOldSeverity().equals(eventOwningIncident.getSeverity())) {
					// this is a new incident
					severityMsg = "a new " + eventOwningIncident.getNotificationTypeFromTag() + " created";
					log.info("severity changed because this is a new incident: oldSeverity = " + eventOwningIncident.getOldSeverity() + " newSeverity = " + eventOwningIncident.getSeverity());
				} else {
	            	severityMsg = "Severity of " + eventOwningIncident.getNotificationTypeFromTag() + " changed from " + eventOwningIncident.getOldSeverity().toString() + " to " + eventOwningIncident.getSeverity().toString();
	            	log.info(severityMsg);
				}
				
				// queue up notification
	        	User.sendNotifications(theApplication.getId(), eventOwningIncident.getNotificationTypeFromTag(), severityMsg, eventOwningIncident.getId());
			}
        } finally {
        	// this should persist the changes
        	em.close();
        }
        
		return eventOwningIncident;
	}
	
	public static Incident createIncident(String theEventName, String theWellKnownTag, Application theApplication, String theMessage, String theSummary) {
        EntityManager em = EMF.get().createEntityManager();
        Incident incident = null;
        
        em.getTransaction().begin();
		try {
			incident = new Incident();
			incident.setEventName(theEventName);
			incident.setEventCount(1);
			incident.addToTags(theWellKnownTag);
			incident.setSummary(theSummary);
			incident.setMessage(theMessage);
			incident.setLastUpdatedGmtDate(new Date());
			incident.setCreatedGmtDate(new Date());
			incident.setApplicationId(theApplication.getId());
			
			// Default severity
			incident.setOldSeverity(Incident.INITIALIZATION_SEVERITY);
			if(incident.isLog()) {
				incident.setSeverity(Incident.MINIMUM_SEVERITY);
			} else if(incident.isCrash()) {
				incident.setSeverity(Incident.HIGH_SEVERITY);
			} else {
				incident.setSeverity(Incident.LOW_SEVERITY);
			}
			
			// Default status to 'open'
			incident.changeStatus(theWellKnownTag, Incident.OPEN_STATUS, theApplication);
			
			// Assign application unique incident number
			incident.setNumber(Application.getAndIncrementIncidentNumber(theApplication.getId()));
			
			// set the activeThruGmtDate for auto closing
			int daysUntilAutoArchive = theApplication.daysUntilAutoArchive();
			Date activeThruGmtDate = GMT.addDaysToDate(new Date(), daysUntilAutoArchive);
			incident.setActiveThruGmtDate(activeThruGmtDate);
			
			log.info("creating new incident for application " + theApplication.getName() + " with eventName = " + theEventName + " and well known tag = " + theWellKnownTag);
			em.persist(incident);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.severe("Incident::createIncident() exception = " + e.getMessage());
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
		
		log.info("Incident::createIncident() complete and returning. Incident key = " + incident.getKey());
		return incident;
	}
	
	// assumption -- error count was incremented right before this method was called. Uses the info to see if severity "threshold" was just exceeded.
	// return: true if severity changed and was updated in the incident object passed in; false otherwise
	public static Boolean checkForSeverityUpdate(Incident theIncident, Application theApplication) {
		Boolean didSeverityChange = false;
		
		// TODO enforce a maximum severity of 10?
		
		// if this is a brand new incident, place severities into steady state and return. A new incident is always
		// treated as a 'change in severity'
		if(theIncident.getOldSeverity().equals(Incident.INITIALIZATION_SEVERITY)) {
			// sync old severity with new severity. NOTE: only time severities are the same and a 'true' is returned 
			// is when it is a NEW incident
			log.info("checkForSeverityUpdate bypassed because this is a new incident");
			theIncident.setOldSeverity(theIncident.getSeverity());
			return true;
		}
		
		// this is an existing incident so we only update severity for logs
		if(!theIncident.isLog()) {
			log.info("checkForSeverityUpdate returning because the event is NOT a log");
			return false;
		}
		
		// need the number of end users for this application
		int numberOfEndUsers = theApplication.getNumberOfEndUsers();
		numberOfEndUsers = numberOfEndUsers < 1000 ? 1000 : numberOfEndUsers;
		
		// alogrithm: severity = (numOfErrors * 1000)/(numOfEndUsers * sensitivity)
		int newErrorCount = theIncident.getEventCount();
		int oldErrorCount = newErrorCount - 1;
		log.info("oldErrorCount = " + oldErrorCount + " newErrorCount = " + newErrorCount);
		float newSeverityFloat = (newErrorCount * 1000)/(numberOfEndUsers * theApplication.getSeveritySensitivity());
		log.info("newSeverityFloat = " + newSeverityFloat);
		int newSeverity = Math.round(newSeverityFloat);
		float oldSeverityFloat = (oldErrorCount * 1000)/(numberOfEndUsers * theApplication.getSeveritySensitivity());
		log.info("oldSeverityFloat = " + oldSeverityFloat);
		int oldSeverity = Math.round(oldSeverityFloat);
		
		if(newSeverity != oldSeverity) {
			log.info("change in incident severity detected");
			didSeverityChange = true;
			// NOTE: for existing incidents, when a change of severity is reported, then old and new severity are always a different value!
			theIncident.setOldSeverity(theIncident.getSeverity());
			theIncident.setSeverity(newSeverity);
		} else {
			log.info("incident severity did NOT change");
		}
		
		// TODO **** have to account for member voting - so factor voting into final severity assignment
		
		return didSeverityChange;
	}
}
