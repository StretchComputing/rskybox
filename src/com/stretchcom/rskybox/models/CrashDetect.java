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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.stretchcom.rskybox.server.ApiStatusCode;
import com.stretchcom.rskybox.server.GMT;

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
    		name="CrashDetect.getAllWithApplicationIdAndIncidentId",
    		query="SELECT cd FROM CrashDetect cd WHERE " +
    		      "cd.applicationId = :applicationId" + " AND " +
    			  "cd.incidentId = :incidentId ORDER BY cd.detectedGmtDate DESC"
    ),
    @NamedQuery(
    		name="CrashDetect.getByStatus",
    		query="SELECT cd FROM CrashDetect cd WHERE cd.status = :status ORDER BY cd.detectedGmtDate DESC"
    ),
    @NamedQuery(
    		name="CrashDetect.getByIncident",
    		query="SELECT cd FROM CrashDetect cd WHERE cd.incidentId = :incidentId ORDER BY cd.detectedGmtDate DESC"
    ),
    @NamedQuery(
    		name="CrashDetect.getByStatusAndApplicationId",
    		query="SELECT cd FROM CrashDetect cd WHERE cd.status = :status and cd.applicationId = :applicationId ORDER BY cd.detectedGmtDate DESC"
    ),
    @NamedQuery(
    		name="CrashDetect.getByStatusAndApplicationIdAndIncidentId",
    		query="SELECT cd FROM CrashDetect cd WHERE " +
    		      "cd.status = :status" + " AND " + 
    			  "cd.applicationId = :applicationId" + " AND " +
    		      "cd.incidentId = :incidentId ORDER BY cd.detectedGmtDate DESC"
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
      @NamedQuery(
        		name="CrashDetect.getByActiveThruGmtDateIsNull",
        		query="SELECT cd FROM CrashDetect cd WHERE cd.activeThruGmtDate = NULL"
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
	private String userId;
	private String userName;
	private Text stackDataBase64;
	private String instanceUrl;
	private String status;
	private String applicationId;
	private Date activeThruGmtDate;  // Active thru this date.  Application specific.
	private Integer number;  // sequential number auto assigned to incidents with scope of the application
	private String incidentId; // foreign key to 'owning' incident

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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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
	
	public static Boolean isStatusParameterValid(String theStatus) {
		if(theStatus.equals(CrashDetect.NEW_STATUS) || theStatus.equals(CrashDetect.ARCHIVED_STATUS) || theStatus.equals(CrashDetect.ALL_STATUS) ) return true;
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

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}
	
	public String getIncidentId() {
		return incidentId;
	}

	public void setIncidentId(String incidentId) {
		this.incidentId = incidentId;
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
	
    
    public static JSONObject getJson(CrashDetect crashDetect, Boolean isList) {
    	return getJson(crashDetect, null, isList);
    }

    public static JSONObject getJson(CrashDetect crashDetect, String theApiStatus, Boolean isList) {
        JSONObject json = new JSONObject();

        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(crashDetect != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
        		json.put("id", KeyFactory.keyToString(crashDetect.getKey()));
        		json.put("summary", crashDetect.getSummary());
    			
            	Date detectedDate = crashDetect.getDetectedGmtDate();
            	if(detectedDate != null) {
            		json.put("date", GMT.convertToIsoDate(detectedDate));
            	}
            	
            	json.put("userId", crashDetect.getUserId());
            	json.put("userName", crashDetect.getUserName());
            	json.put("instanceUrl", crashDetect.getInstanceUrl());
            	json.put("incidentId", crashDetect.getIncidentId());
            	
            	JSONArray appActionsJsonArray = new JSONArray();
            	List<AppAction> appActions = crashDetect.getAppActions();
            	for(AppAction aa : appActions) {
            		JSONObject appActionJsonObj = new JSONObject();
            		appActionJsonObj.put("description", aa.getDescription());
            		appActionJsonObj.put("timestamp", GMT.convertToIsoDate(aa.getTimestamp()));
            		if(aa.getDuration() != null) appActionJsonObj.put("duration", aa.getDuration());
            		appActionsJsonArray.put(appActionJsonObj);
            	}
            	if(appActions.size() > 0) {json.put("appActions", appActionsJsonArray);}
            	
            	// TODO remove eventually, for backward compatibility before status field existed. If status not set, default to 'new'
            	String status = crashDetect.getStatus();
            	if(status == null || status.length() == 0) {status = "new";}
            	json.put("status", status);
            	json.put("appId", crashDetect.getApplicationId());
        	}
        } catch (JSONException e) {
        	log.severe("getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            return null;
        }
        return json;
    }
}
