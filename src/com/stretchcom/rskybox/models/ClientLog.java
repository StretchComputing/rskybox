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
    		name="ClientLog.getAll",
    		query="SELECT cl FROM ClientLog cl ORDER BY cl.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="ClientLog.getAllWithApplicationId",
    		query="SELECT cl FROM ClientLog cl WHERE cl.applicationId = :applicationId ORDER BY cl.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="ClientLog.getAllWithApplicationIdAndIncidentId",
    		query="SELECT cl FROM ClientLog cl WHERE " +
    		      "cl.applicationId = :applicationId" + " AND " + 
    			  "cl.incidentId = :incidentId ORDER BY cl.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="ClientLog.getByStatus",
    		query="SELECT cl FROM ClientLog cl WHERE cl.status = :status  ORDER BY cl.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="ClientLog.getByIncident",
    		query="SELECT cl FROM ClientLog cl WHERE cl.incidentId = :incidentId ORDER BY cl.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="ClientLog.getByStatusAndApplicationId",
    		query="SELECT cl FROM ClientLog cl WHERE cl.status = :status and cl.applicationId = :applicationId ORDER BY cl.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="ClientLog.getByStatusAndApplicationIdAndIncidentId",
    		query="SELECT cl FROM ClientLog cl WHERE " +
    		      "cl.status = :status" + " AND " +
    			  "cl.applicationId = :applicationId" + " AND " +
    		      "cl.incidentId = :incidentId ORDER BY cl.createdGmtDate DESC"
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
	public final static String WARN_LOG_LEVEL = "warn";
	public final static String ERROR_LOG_LEVEL = "error";

	private String logLevel;
	private String logName;
	private String message;
	private Date createdGmtDate;
	private String userId;
	private String userName;
	private Text stackBackTrace;  // deprecated on 4/14/2012. Supported for legacy data in datastore. Replaced by array stackBackTraces below.
	private String localEndpoint;
	private String remoteEndpoint;
	private String status;
	private String applicationId;
	private Date activeThruGmtDate;  // Active thru this date.  Application specific.
	private String summary;
	private Integer number;  // sequential number auto assigned to incidents with scope of the application
	private String incidentId; // foreign key to 'owning' incident

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

	public String getStackBackTrace() {
		return this.stackBackTrace == null? null : this.stackBackTrace.getValue();
	}

	public void setStackBackTrace(String stackBackTrace) {
		this.stackBackTrace = new Text(stackBackTrace);
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
	
	public static Boolean isStatusValid(String theStatus) {
		if(theStatus.equals(ClientLog.NEW_STATUS) || theStatus.equals(ClientLog.ARCHIVED_STATUS)) return true;
		return false;
	}
	
	public static Boolean isStatusParameterValid(String theStatus) {
		if(theStatus.equals(ClientLog.NEW_STATUS) || theStatus.equals(ClientLog.ARCHIVED_STATUS) || theStatus.equals(ClientLog.ALL_STATUS) ) return true;
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
		   theLogLevel.equals(ClientLog.ERROR_LOG_LEVEL) || theLogLevel.equals(ClientLog.WARN_LOG_LEVEL)) {
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
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}
    	
	public List<String> getStackBackTraces() {
		return stackBackTraces;
	}

	public void setStackBackTraces(List<String> stackBackTraces) {
		this.stackBackTraces = stackBackTraces;
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
	
	// StringBuffer theSb:  out parameter to append markdow to ...
    public void getMarkDown(StringBuffer theSb) {
		theSb.append("* Summary: ");
		theSb.append(this.getSummary());
		theSb.append("\n");
		theSb.append("* User ID: ");
		theSb.append(this.getUserId());
		theSb.append("\n");
		theSb.append("* App Actions: ");
		theSb.append("\n");
		
    	List<AppAction> appActions = this.getAppActions();
    	for(AppAction aa : appActions) {
    		theSb.append("    * (");
    		theSb.append(GMT.dateToString(aa.getTimestamp()));
    		theSb.append(" GMT");
    		theSb.append(") ");
    		theSb.append(aa.getDescription());
    		theSb.append("\n");
    	}
    	
    	List<String> stackBackTraces = this.stackBackTraces;
    	if(stackBackTraces != null && stackBackTraces.size() > 0) {
    		theSb.append("* Stack Backtrace: ");
    		theSb.append("\n");
        	for(String trace : stackBackTraces) {
        		theSb.append("    * ");
        		theSb.append(trace);
        		theSb.append("\n");
        	}
    	}
    }
	
    public static JSONObject getJson(ClientLog clientLog, Boolean isList) {
    	return getJson(clientLog, null, isList);
    }

    public static JSONObject getJson(ClientLog clientLog, String theApiStatus, Boolean isList) {
        JSONObject json = new JSONObject();

        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(clientLog != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
        		json.put("id", KeyFactory.keyToString(clientLog.getKey()));
    			
            	Date createdDate = clientLog.getCreatedGmtDate();
            	if(createdDate != null) {
            		json.put("date", GMT.convertToIsoDate(createdDate));
            	}
            	json.put("userId", clientLog.getUserId());
            	json.put("userName", clientLog.getUserName());
            	json.put("localEndpoint", clientLog.getLocalEndpoint());
            	json.put("remoteEndpoint", clientLog.getRemoteEndpoint());
            	json.put("logLevel", clientLog.getLogLevel());
            	json.put("logName", clientLog.getLogName());
            	json.put("message", clientLog.getMessage());
            	json.put("summary", clientLog.getSummary());
            	json.put("incidentId", clientLog.getIncidentId());
            	
            	JSONArray stackBackTracesJsonArray = new JSONArray();
            	List<String> stackBackTraces = clientLog.getStackBackTraces();
            	
            	//////////////////////////////////////////////////
            	// TODO - remove support of stackBackTrace string
            	//////////////////////////////////////////////////
            	if(stackBackTraces == null || stackBackTraces.size() == 0) {
        			log.info("returning legacy stackBackTrace");
            		stackBackTraces = new ArrayList<String>();
            		String stackBackTrace = clientLog.getStackBackTrace();
            		if(stackBackTrace != null && stackBackTrace.length() > 0) {
                		stackBackTraces.add(stackBackTrace);
            		}
            	}
            	//////////////////////////////////////////////////

            	for(String sbt: stackBackTraces) {
            		stackBackTracesJsonArray.put(sbt);
            	}
            	log.info("stackBackTraces # of parts = " + stackBackTraces.size());
            	json.put("stackBackTrace", stackBackTracesJsonArray);
            	
            	JSONArray appActionsJsonArray = new JSONArray();
            	List<AppAction> appActions = clientLog.getAppActions();
            	for(AppAction aa : appActions) {
            		JSONObject appActionJsonObj = new JSONObject();
            		appActionJsonObj.put("description", aa.getDescription());
            		appActionJsonObj.put("timestamp", GMT.convertToIsoDate(aa.getTimestamp()));
            		if(aa.getDuration() != null) appActionJsonObj.put("duration", aa.getDuration());
            		appActionsJsonArray.put(appActionJsonObj);
            	}
            	if(appActions.size() > 0) {json.put("appActions", appActionsJsonArray);}
            	
            	// TODO remove eventually, for backward compatibility before status field existed. If status not set, default to 'new'
            	String status = clientLog.getStatus();
            	if(status == null || status.length() == 0) {status = "new";}
            	json.put("status", status);
            	json.put("appId", clientLog.getApplicationId());
        	}
        } catch (JSONException e) {
        	log.severe("UsersResrouce::getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            return null;
        }
        return json;
    }
    
	// Returns list of users matching specified email address
	public static void deleteSecondOldest(String theApplicationId, String theIncidentId) {
        EntityManager em = EMF.get().createEntityManager();
        List<ClientLog> clientLogs = null;

		try {
	    	clientLogs= (List<ClientLog>)em.createNamedQuery("ClientLog.getAllWithApplicationIdAndIncidentId")
	    			.setParameter("applicationId", theApplicationId)
	    			.setParameter("incidentId", theIncidentId)
	    			.getResultList();
	    	
            int numOfLogs = clientLogs.size();
            if(numOfLogs > 2) {
            	int secondOldestIndex = numOfLogs - 2;
            	ClientLog cl = clientLogs.get(secondOldestIndex);
            	em.remove(cl);
            	log.info("client log with name = " + cl.getLogName() + "and message = " + cl.getMessage() + " just removed from data store");
            }
		} catch (Exception e) {
			log.severe("exception = " + e.getMessage());
			e.printStackTrace();
		} finally {
			em.close();
		}
		return;
	}

}
