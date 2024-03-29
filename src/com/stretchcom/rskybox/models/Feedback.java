package com.stretchcom.rskybox.models;

import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

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
    		name="Feedback.getAll",
    		query="SELECT fb FROM Feedback fb ORDER BY fb.recordedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Feedback.getAllWithApplicationId",
    		query="SELECT fb FROM Feedback fb WHERE fb.applicationId = :applicationId ORDER BY fb.recordedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Feedback.getAllWithApplicationIdAndIncidentId",
    		query="SELECT fb FROM Feedback fb WHERE " +
    		      "fb.applicationId = :applicationId" + " AND " +
    			  "fb.incidentId = :incidentId ORDER BY fb.recordedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Feedback.getByStatus",
    		query="SELECT fb FROM Feedback fb WHERE fb.status = :status ORDER BY fb.recordedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Feedback.getByIncident",
    		query="SELECT fb FROM Feedback fb WHERE fb.incidentId = :incidentId ORDER BY fb.recordedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Feedback.getByStatusAndApplicationId",
    		query="SELECT fb FROM Feedback fb WHERE fb.status = :status and fb.applicationId = :applicationId ORDER BY fb.recordedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Feedback.getByStatusAndApplicationIdAndIncidentId",
    		query="SELECT fb FROM Feedback fb WHERE " +
    		      "fb.status = :status" + " AND " + 
    			  "fb.applicationId = :applicationId" + " AND " +
    		      "fb.incidentId = :incidentId ORDER BY fb.recordedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Feedback.getByKey",
    		query="SELECT fb FROM Feedback fb WHERE fb.key = :key"
    ),
    @NamedQuery(
    		name="Feedback.getByapplicationId",
    		query="SELECT fb FROM Feedback fb WHERE fb.applicationId = :applicationId"
    ),
	@NamedQuery(
    		name="Feedback.getOldActiveThru",
    		query="SELECT fb FROM Feedback fb WHERE " + 
    				"fb.activeThruGmtDate < :currentDate"  + " AND " +
    				"fb.status = :status"
      ),
      @NamedQuery(
      		name="Feedback.getByActiveThruGmtDateIsNull",
      		query="SELECT fb FROM Feedback fb WHERE fb.activeThruGmtDate = NULL"
      ),
})
public class Feedback {
	private static final Logger log = Logger.getLogger(Feedback.class.getName());
	
	public final static String NEW_STATUS = "new";
	public final static String ARCHIVED_STATUS = "archived";
	public final static String ALL_STATUS = "all";
	
	@Basic private Text voiceBase64;
	private Date recordedGmtDate;
	private String userId;
	private String userName;
	private String localEndpoint;
	private String remoteEndpoint;
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

	public String getVoiceBase64() {
		return this.voiceBase64 == null? null : this.voiceBase64.getValue();
	}

	public void setVoiceBase64(String voiceBase64) {
		this.voiceBase64 = new Text(voiceBase64);
	}
	
	public Date getRecordedGmtDate() {
		return recordedGmtDate;
	}

	public void setRecordedGmtDate(Date recordedGmtDate) {
		this.recordedGmtDate = recordedGmtDate;
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

	public String getLocalEndpoint() {
		return localEndpoint;
	}

	public void setLocalEndpoint(String localEndpoint) {
		this.localEndpoint = localEndpoint;
	}

	public String getRemoteEndpoint() {
		return remoteEndpoint;
	}

	public void setRemoteEndpoint(String remoteEndpoint) {
		this.remoteEndpoint = remoteEndpoint;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public Boolean isStatusValid(String theStatus) {
		if(theStatus.equals(Feedback.NEW_STATUS) || theStatus.equals(Feedback.ARCHIVED_STATUS)) return true;
		return false;
	}
	
	public static Boolean isStatusParameterValid(String theStatus) {
		if(theStatus.equals(Feedback.NEW_STATUS) || theStatus.equals(Feedback.ARCHIVED_STATUS) || theStatus.equals(Feedback.ALL_STATUS) ) return true;
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
	
    public static JSONObject getJson(Feedback feedback, Boolean isList) {
    	return getJson(feedback, null, isList);
    }

    public static JSONObject getJson(Feedback feedback, String theApiStatus, Boolean isList) {
    	
        JSONObject json = new JSONObject();
        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(feedback != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
        		json.put("id", KeyFactory.keyToString(feedback.getKey()));
    			
            	Date recordedDate = feedback.getRecordedGmtDate();
            	if(recordedDate != null) {
            		json.put("date", GMT.convertToIsoDate(recordedDate));
            	}
            	
            	json.put("userId", feedback.getUserId());
            	json.put("userName", feedback.getUserName());
            	json.put("localEndpoint", feedback.getLocalEndpoint());
            	json.put("remoteEndpoint", feedback.getRemoteEndpoint());
            	json.put("status", feedback.getStatus());
            	json.put("appId", feedback.getApplicationId());
            	json.put("incidentId", feedback.getIncidentId());
        	}
        } catch (JSONException e) {
        	log.severe("getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            return null;
        }
        return json;
    }
}
