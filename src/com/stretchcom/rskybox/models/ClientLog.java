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
    		name="ClientLog.getAll",
    		query="SELECT cl FROM ClientLog cl ORDER BY cl.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="ClientLog.getAllWithApplicationId",
    		query="SELECT cl FROM ClientLog cl WHERE cl.applicationId = :applicationId ORDER BY cl.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="ClientLog.getByStatus",
    		query="SELECT cl FROM ClientLog cl WHERE cl.status = :status  ORDER BY cl.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="ClientLog.getByStatusAndApplicationId",
    		query="SELECT cl FROM ClientLog cl WHERE cl.status = :status and cl.applicationId = :applicationId ORDER BY cl.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="ClientLog.getByKey",
    		query="SELECT cl FROM ClientLog cl WHERE cl.key = :key"
    ),
    @NamedQuery(
    		name="ClientLog.getByApplicationId",
    		query="SELECT cl FROM ClientLog cl WHERE cl.applicationId = :applicationId"
    ),
	@NamedQuery(
    		name="ClientLog.getOldActiveThru",
    		query="SELECT cl FROM ClientLog cl WHERE " + 
    				"cl.activeThruGmtDate < :currentDate"  + " AND " +
    				"cl.status = :status"
      ),
      @NamedQuery(
      		name="ClientLog.getByActiveThruGmtDateIsNull",
      		query="SELECT cl FROM ClientLog cl WHERE cl.activeThruGmtDate = NULL"
      ),
})
public class ClientLog {
	private static final Logger log = Logger.getLogger(ClientLog.class.getName());
	
	public final static String NEW_STATUS = "new";
	public final static String ARCHIVED_STATUS = "archived";
	public final static String ALL_STATUS = "all";
	
	public final static String DEBUG_LOG_LEVEL = "debug";
	public final static String INFO_LOG_LEVEL = "info";
	public final static String ERROR_LOG_LEVEL = "error";
	public final static String EXCEPTION_LOG_LEVEL = "exception";

	private String logLevel;
	private String logName;
	private String message;
	// TODO support time zone and GMT for dates
	private Date createdGmtDate;
	private String userName;
	private Text stackBackTrace;  // deprecated on 4/14/2012. Supported for legacy data in datastore. Replaces by array stackBackTraces below.
	private String instanceUrl;
	private String status;
	private String applicationId;
	private Date activeThruGmtDate;  // Active thru this date.  Application specific.
	private String summary;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;
	
	@Basic
	private List<String> appActionDescriptions;
	
	@Basic
	private List<Date> appActionTimestamps;
	
	@Basic
	private List<Integer> appActionDurations;
	
	@Basic
	private List<String> stackBackTraces;


	public Key getKey() {
        return key;
    }

    public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getCreatedGmtDate() {
		return createdGmtDate;
	}

	public void setCreatedGmtDate(Date createdGmtDate) {
		this.createdGmtDate = createdGmtDate;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getStackBackTrace() {
		return this.stackBackTrace == null? null : this.stackBackTrace.getValue();
	}

	public void setStackBackTrace(String stackBackTrace) {
		this.stackBackTrace = new Text(stackBackTrace);
	}

	public String getInstanceUrl() {
		return instanceUrl;
	}

	public void setInstanceUrl(String instanceUrl) {
		this.instanceUrl = instanceUrl;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public Boolean isStatusValid(String theStatus) {
		if(theStatus.equals(ClientLog.NEW_STATUS) || theStatus.equals(ClientLog.ARCHIVED_STATUS)) return true;
		return false;
	}
	
	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}
	
	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}

	public Boolean isLogLevelValid(String theLogLevel) {
		if(theLogLevel.equals(ClientLog.DEBUG_LOG_LEVEL) || theLogLevel.equals(ClientLog.INFO_LOG_LEVEL) ||
		   theLogLevel.equals(ClientLog.ERROR_LOG_LEVEL) || theLogLevel.equals(ClientLog.EXCEPTION_LOG_LEVEL)) {
			return true;
		}
		return false;
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

    public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
    	
	public List<String> getStackBackTraces() {
		return stackBackTraces;
	}

	public void setStackBackTraces(List<String> stackBackTraces) {
		this.stackBackTraces = stackBackTraces;
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
