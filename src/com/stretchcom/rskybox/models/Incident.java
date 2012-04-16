package com.stretchcom.rskybox.models;

import java.util.ArrayList;
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
import com.google.appengine.api.datastore.Text;

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

	public Boolean createAppActions(List<AppAction> theNewAppActionList) {
		if(theNewAppActionList == null || theNewAppActionList.size() == 0) {
			return false;
		}
		
		this.appActionDescriptions = new ArrayList<String>();
		this.appActionTimestamps = new ArrayList<Date>();
		this.appActionDurations = new ArrayList<Integer>();
		
		for(AppAction aa : theNewAppActionList) {
			////////////////////////////////////////////////////////////////
			// Convert "normal Java" values to "default" values in Big Table
			////////////////////////////////////////////////////////////////
			String description = aa.getDescription() == null ? "" : aa.getDescription();
			this.appActionDescriptions.add(description);
			
			if(aa.getTimestamp() == null) {
				log.severe("AppAction has a null timestamp -- not allowed");
				return false;
			}
			this.appActionTimestamps.add(aa.getTimestamp());
			
			// if empty, replace with -1
			Integer duration = aa.getDuration() == null ? -1 : aa.getDuration();
			this.appActionDurations.add(duration);
		}
		
		return true;
	}
	
	public List<AppAction> getAppActions() {
		List<AppAction> appActions = new ArrayList<AppAction>();
		
		if(this.appActionDescriptions == null || this.appActionDescriptions.size() == 0) {
			// return the empty list
			return appActions;
		}
		// all appAction arrays are same size, so it doesn't matter which one size is taken from
		int listSize = this.appActionDescriptions.size();
		for(int i=0; i<listSize; i++) {
			AppAction aa = new AppAction();
			
			///////////////////////////////////////////////////////////////////////
			// Convert "default" values stored in Big Table to "normal Java" values
			///////////////////////////////////////////////////////////////////////
			String description = null;
			if(appActionDescriptions.size() > i) {
				description = this.appActionDescriptions.get(i).equals("") ? null : this.appActionDescriptions.get(i);
			} else {
				log.severe("appActionDescriptions array size corrupt");
			}
			aa.setDescription(description);
			
			Date timestamp = null;
			if(appActionTimestamps.size() > i) {
				timestamp = this.appActionTimestamps.get(i);
			} else {
				log.severe("appActionTimestamps array size corrupt");
			}
			aa.setTimestamp(timestamp);
			
			Integer duration = null;
			if(appActionDurations.size() > i) {
				duration = this.appActionDurations.get(i).equals(-1) ? null : this.appActionDurations.get(i);
			} else {
				log.severe("appActionDurations array size corrupt");
			}
			aa.setDuration(duration);
			
			appActions.add(aa);
		}
		return appActions;
	}
}
