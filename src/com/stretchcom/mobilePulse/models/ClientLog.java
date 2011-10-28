package com.stretchcom.mobilePulse.models;

import java.util.Date;

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
    		name="ClientLog.getByStatus",
    		query="SELECT cl FROM ClientLog cl WHERE cl.status = :status  ORDER BY cl.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="ClientLog.getByKey",
    		query="SELECT cl FROM ClientLog cl WHERE cl.key = :key"
    ),
    @NamedQuery(
    		name="ClientLog.getByApplicationId",
    		query="SELECT cl FROM ClientLog cl WHERE cl.applicationId = :applicationId"
    ),
})
public class ClientLog {
	public final static String NEW_STATUS = "new";
	public final static String ARCHIVED_STATUS = "archived";
	public final static String ALL_STATUS = "all";
	
	public final static String DEBUG_LOG_LEVEL = "debug";
	public final static String INFO_LOG_LEVEL = "info";
	public final static String ERROR_LOG_LEVEL = "error";
	public final static String EXCEPTION_LOG_LEVEL = "exception";

	private String logLevel;
	private String message;
	// TODO support time zone and GMT for dates
	private Date createdGmtDate;
	private String userName;
	private Text stackBackTrace;
	private String instanceUrl;
	private String status;
	private String applicationId;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

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
}
