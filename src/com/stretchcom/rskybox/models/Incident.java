package com.stretchcom.rskybox.models;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.google.appengine.api.datastore.Key;

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
	
	public final static String CRASH_TAG = "crash";
	public final static String LOG_TAG = "log";
	public final static String FEEDBACK_TAG = "feedback";
	
	public final static String ACTIVE_REMOTE_CONTROL_MODE = "active";
	public final static String INACTIVE_REMOTE_CONTROL_MODE = "inactive";

	private Integer number;  // sequential number auto assigned to incidents with scope of the application
	private String eventName;
	private Integer eventCount;
	private Integer severity;
	private Date lastUpdatedGmtDate;
	private String endUser;
	private String status;
	private String applicationId;
	private Date activeThruGmtDate;  // Active thru this date.  Application specific.
	private Boolean inStatsOnlyMode;
	private Boolean wasAutoClosed;
	private String remoteControlMode;

	///////////////////////////////////////
	// place holder for future properties
	///////////////////////////////////////
	private String resolution;
	private Boolean wasResolved;
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
	
	public Boolean isStatusValid(String theStatus) {
		if(theStatus.equals(Incident.OPEN_STATUS) || theStatus.equals(Incident.CLOSED_STATUS)) return true;
		return false;
	}
	
	public Integer getNumber() {
		return number;
	}

	public void setLogLevel(Integer number) {
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
}
