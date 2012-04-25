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
    		"i.applicationId = :applicationId" + " AND" +
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
	
	public final static String CRASH_TAG = "crash";
	public final static String LOG_TAG = "log";
	public final static String FEEDBACK_TAG = "feedback";
	
	public final static String ACTIVE_REMOTE_CONTROL_MODE = "active";
	public final static String INACTIVE_REMOTE_CONTROL_MODE = "inactive";

	private Integer number;  // sequential number auto assigned to incidents with scope of the application
	private String eventName;
	private Integer eventCount;
	private Integer severity = Incident.LOW_SEVERITY;
	private Date lastUpdatedGmtDate;
	private Date createdGmtDate;
	private String endUser;
	private String status = Incident.OPEN_STATUS;
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

	public void setStatus(String status) {
		this.status = status;
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
	
	public static Boolean isModeValid(String theMode) {
		if(theMode.equalsIgnoreCase(Incident.ACTIVE_REMOTE_CONTROL_MODE) || theMode.equalsIgnoreCase(Incident.INACTIVE_REMOTE_CONTROL_MODE)) return true;
		return false;
	}
	
	public static Boolean isStatusValid(String theStatus) {
		if(theStatus.equalsIgnoreCase(Incident.OPEN_STATUS) || theStatus.equalsIgnoreCase(Incident.CLOSED_STATUS)) return true;
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
	
	// pretty much guaranteed to return an Incident (short of a server error)
	// either finds the 'owning' incident associated with the specified event or creates a new incident
	public static Incident fetchIncidentIncrementCount(String theEventName, String theWellKnownTag, String theIncidentId, Application theApplication, String theMessage) {
		Incident eventOwningIncident = null;
		List<Incident> relatedIncidents = null;
        EntityManager em = EMF.get().createEntityManager();
        Date now = new Date();
        Boolean isExistingIncident = true;
        
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
    			
    			// always choose the most recently created incident which will be on the top of the list
    			eventOwningIncident = relatedIncidents.get(0);
    		} catch (NoResultException e) {
    			// this is NOT an error -- there is just no incident yet associated with this event so create one
    			eventOwningIncident = Incident.createIncident(theEventName, theWellKnownTag, theApplication, theMessage);
				isExistingIncident = false;
    		} catch (NonUniqueResultException e) {
    			log.severe("should never happen - two or more google account users have same key");
    			return null;
    		}
        }
		
		// if incident recently closed, re-open it. Closed too long, then create a new one.
		if(eventOwningIncident.getStatus().equalsIgnoreCase(Incident.CLOSED_STATUS)) {
			Date finalRevivalDate = GMT.addDaysToDate(eventOwningIncident.getLastUpdatedGmtDate(), theApplication.getDaysInLimbo());
			if(finalRevivalDate.after(now)) {
				// reopen this puppy
				eventOwningIncident.setStatus(Incident.OPEN_STATUS);
				log.info("fetchIncidentIncrementCount() reopening existing incident");
			} else {
				// create a new incident
				eventOwningIncident = Incident.createIncident(theEventName, theWellKnownTag, theApplication, theMessage);
				isExistingIncident = false;
				log.info("fetchIncidentIncrementCount() existing incident CLOSED and too old to reopen");
			}
		}
		
		if(isExistingIncident){
			Integer eventCount = eventOwningIncident.getEventCount();
			eventOwningIncident.setEventCount(eventCount++);
			eventOwningIncident.setLastUpdatedGmtDate(now);
			
			// TODO enhance message by merging summaries?
			
			// update severity if appropriate
			// if severity changes, queue up notification
		}
        
		
		return eventOwningIncident;
	}
	
	public static Incident createIncident(String theEventName, String theWellKnownTag, Application theApplication, String theMessage) {
        EntityManager em = EMF.get().createEntityManager();
        Incident incident = null;
        
        em.getTransaction().begin();
		try {
			incident = new Incident();
			incident.setEventName(theEventName);
			incident.setEventCount(1);
			incident.addToTags(theWellKnownTag);
			incident.setMessage(theMessage);
			incident.setLastUpdatedGmtDate(new Date());
			incident.setCreatedGmtDate(new Date());
			incident.setApplicationId(theApplication.getId());
        	
			// Default status to 'open'
			incident.setStatus(Incident.OPEN_STATUS);
			
			// Default severity
			incident.setSeverity(Incident.LOW_SEVERITY);
			
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
		return incident;
	}
}
