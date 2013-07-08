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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.stretchcom.rskybox.server.ApiStatusCode;
import com.stretchcom.rskybox.server.EMF;
import com.stretchcom.rskybox.server.GMT;

@Entity
@NamedQueries({
    @NamedQuery(
    		name="Filter.getAll",
    		query="SELECT f FROM Filter f ORDER BY f.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="Filter.getAllWithApplicationId",
    		query="SELECT f FROM Filter f WHERE f.applicationId = :applicationId ORDER BY f.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="Filter.getByStatus",
    		query="SELECT f FROM Filter f WHERE f.status = :status  ORDER BY f.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="Filter.getByStatusAndApplicationId",
    		query="SELECT f FROM Filter f WHERE f.status = :status and f.applicationId = :applicationId ORDER BY f.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="Filter.getByKey",
    		query="SELECT f FROM Filter f WHERE f.key = :key"
    ),
    @NamedQuery(
    		name="Filter.getByApplicationId",
    		query="SELECT cl FROM Filter cl WHERE cl.applicationId = :applicationId"
    ),
})
public class Filter {
	private static final Logger log = Logger.getLogger(Filter.class.getName());
	
	public final static String ACTIVE_STATUS = "active";
	public final static String SUSPENDED_STATUS = "suspended";
	public final static String ALL_STATUS = "all";

	private Date createdGmtDate;
	private String userId;
	private String status;
	private String applicationId;
	private String localEndpoint;
	private String remoteEndpoint;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

	public Key getKey() {
        return key;
    }

	public Date getCreatedGmtDate() {
		return createdGmtDate;
	}

	public void setCreatedGmtDate(Date createdGmtDate) {
		this.createdGmtDate = createdGmtDate;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public static Boolean isStatusValid(String theStatus) {
		if(theStatus.equals(Filter.ACTIVE_STATUS) || theStatus.equals(Filter.SUSPENDED_STATUS)) return true;
		return false;
	}
	
	public static Boolean isStatusParameterValid(String theStatus) {
		if(theStatus.equals(Filter.ACTIVE_STATUS) || theStatus.equals(Filter.SUSPENDED_STATUS) || theStatus.equals(Filter.ALL_STATUS) ) return true;
		return false;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
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
	
    public static JSONObject getJson(Filter clientLog, Boolean isList) {
//    	return getJson(clientLog, null, isList);
    	return null;
    }

//    public static JSONObject getJson(Filter clientLog, String theApiStatus, Boolean isList) {
//        JSONObject json = new JSONObject();
//
//        try {
//        	if(theApiStatus != null) {
//        		json.put("apiStatus", theApiStatus);
//        	}
//        	if(clientLog != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
//        		json.put("id", KeyFactory.keyToString(clientLog.getKey()));
//    			
//            	Date createdDate = clientLog.getCreatedGmtDate();
//            	if(createdDate != null) {
//            		json.put("date", GMT.convertToIsoDate(createdDate));
//            	}
//            	json.put("userId", clientLog.getUserId());
//            	json.put("userName", clientLog.getUserName());
//            	json.put("instanceUrl", clientLog.getInstanceUrl());
//            	json.put("logLevel", clientLog.getLogLevel());
//            	json.put("logName", clientLog.getLogName());
//            	json.put("message", clientLog.getMessage());
//            	json.put("summary", clientLog.getSummary());
//            	json.put("incidentId", clientLog.getIncidentId());
//            	
//            	JSONArray stackBackTracesJsonArray = new JSONArray();
//            	List<String> stackBackTraces = clientLog.getStackBackTraces();
//            	
//            	//////////////////////////////////////////////////
//            	// TODO - remove support of stackBackTrace string
//            	//////////////////////////////////////////////////
//            	if(stackBackTraces == null || stackBackTraces.size() == 0) {
//        			log.info("returning legacy stackBackTrace");
//            		stackBackTraces = new ArrayList<String>();
//            		String stackBackTrace = clientLog.getStackBackTrace();
//            		if(stackBackTrace != null && stackBackTrace.length() > 0) {
//                		stackBackTraces.add(stackBackTrace);
//            		}
//            	}
//            	//////////////////////////////////////////////////
//
//            	for(String sbt: stackBackTraces) {
//            		stackBackTracesJsonArray.put(sbt);
//            	}
//            	log.info("stackBackTraces # of parts = " + stackBackTraces.size());
//            	json.put("stackBackTrace", stackBackTracesJsonArray);
//            	
//            	JSONArray appActionsJsonArray = new JSONArray();
//            	List<AppAction> appActions = clientLog.getAppActions();
//            	for(AppAction aa : appActions) {
//            		JSONObject appActionJsonObj = new JSONObject();
//            		appActionJsonObj.put("description", aa.getDescription());
//            		appActionJsonObj.put("timestamp", GMT.convertToIsoDate(aa.getTimestamp()));
//            		if(aa.getDuration() != null) appActionJsonObj.put("duration", aa.getDuration());
//            		appActionsJsonArray.put(appActionJsonObj);
//            	}
//            	if(appActions.size() > 0) {json.put("appActions", appActionsJsonArray);}
//            	
//            	// TODO remove eventually, for backward compatibility before status field existed. If status not set, default to 'new'
//            	String status = clientLog.getStatus();
//            	if(status == null || status.length() == 0) {status = "new";}
//            	json.put("status", status);
//            	json.put("appId", clientLog.getApplicationId());
//        	}
//        } catch (JSONException e) {
//        	log.severe("UsersResrouce::getUserJson() error creating JSON return object. Exception = " + e.getMessage());
//            return null;
//        }
//        return json;
//    }
}
