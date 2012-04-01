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
    		name="CrashDetect.getAll",
    		query="SELECT cd FROM CrashDetect cd ORDER BY cd.detectedGmtDate DESC"
    ),
    @NamedQuery(
    		name="CrashDetect.getAllWithApplicationId",
    		query="SELECT cd FROM CrashDetect cd WHERE cd.applicationId = :applicationId ORDER BY cd.detectedGmtDate DESC"
    ),
    @NamedQuery(
    		name="CrashDetect.getByStatus",
    		query="SELECT cd FROM CrashDetect cd WHERE cd.status = :status ORDER BY cd.detectedGmtDate DESC"
    ),
    @NamedQuery(
    		name="CrashDetect.getByStatusAndApplicationId",
    		query="SELECT cd FROM CrashDetect cd WHERE cd.status = :status and cd.applicationId = :applicationId ORDER BY cd.detectedGmtDate DESC"
    ),
    @NamedQuery(
    		name="CrashDetect.getByKey",
    		query="SELECT cd FROM CrashDetect cd WHERE cd.key = :key"
    ),
    @NamedQuery(
    		name="CrashDetect.getByApplicationId",
    		query="SELECT cd FROM CrashDetect cd WHERE cd.applicationId = :applicationId"
    ),
	@NamedQuery(
    		name="CrashDetect.getOldActiveThru",
    		query="SELECT cd FROM CrashDetect cd WHERE " + 
    				"cd.activeThruGmtDate < :currentDate"  + " AND " +
    				"cd.status = :status"
      ),
})
public class CrashDetect {
	private static final Logger log = Logger.getLogger(CrashDetect.class.getName());
	
	public final static String NEW_STATUS = "new";
	public final static String ARCHIVED_STATUS = "archived";
	public final static String ALL_STATUS = "all";

	private String summary;
	// TODO support time zone and GMT for dates
	private Date detectedGmtDate;
	private String userName;
	private Text stackDataBase64;
	private String instanceUrl;
	private String status;
	private String applicationId;
	private Date activeThruGmtDate;  // Active thru this date.  Application specific.

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    public Key getKey() {
        return key;
    }
	
	@Basic
	private List<String> appActionDescriptions;
	
	@Basic
	private List<Date> appActionTimestamps;
	
	@Basic
	private List<Integer> appActionDurations;

    public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Date getDetectedGmtDate() {
		return detectedGmtDate;
	}

	public void setDetectedGmtDate(Date detectedGmtDate) {
		this.detectedGmtDate = detectedGmtDate;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getStackDataBase64() {
		return this.stackDataBase64 == null? null : this.stackDataBase64.getValue();
	}

	public void setStackDataBase64(String stackDataBase64) {
		this.stackDataBase64 = new Text(stackDataBase64);
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
		if(theStatus.equals(CrashDetect.NEW_STATUS) || theStatus.equals(CrashDetect.ARCHIVED_STATUS)) return true;
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
