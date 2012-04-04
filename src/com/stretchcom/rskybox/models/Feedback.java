package com.stretchcom.rskybox.models;

import java.util.Date;

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
    		name="Feedback.getAll",
    		query="SELECT fb FROM Feedback fb ORDER BY fb.recordedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Feedback.getAllWithApplicationId",
    		query="SELECT fb FROM Feedback fb WHERE fb.applicationId = :applicationId ORDER BY fb.recordedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Feedback.getByStatus",
    		query="SELECT fb FROM Feedback fb WHERE fb.status = :status ORDER BY fb.recordedGmtDate DESC"
    ),
    @NamedQuery(
    		name="Feedback.getByStatusAndApplicationId",
    		query="SELECT fb FROM Feedback fb WHERE fb.status = :status and fb.applicationId = :applicationId ORDER BY fb.recordedGmtDate DESC"
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
	public final static String NEW_STATUS = "new";
	public final static String ARCHIVED_STATUS = "archived";
	public final static String ALL_STATUS = "all";
	
	@Basic private Text voiceBase64;
	private Date recordedGmtDate;
	private String userName;
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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
		if(theStatus.equals(Feedback.NEW_STATUS) || theStatus.equals(Feedback.ARCHIVED_STATUS)) return true;
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
}
