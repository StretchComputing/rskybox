package com.stretchcom.rskybox.models;

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
})
public class CrashDetect {
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

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    public Key getKey() {
        return key;
    }

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
	
}
