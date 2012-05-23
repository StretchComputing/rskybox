package com.stretchcom.rskybox.models;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import com.google.appengine.api.datastore.Key;

@Entity
@NamedQueries({
    @NamedQuery(
    		name="EndUser.getAll",
    		query="SELECT eu FROM EndUser eu"
    ),
    @NamedQuery(
    		name="EndUser.getAllWithApplicationId",
    		query="SELECT eu FROM EndUser eu WHERE eu.applicationId = :applicationId"
    ),
    @NamedQuery(
    		name="EndUser.getByKey",
    		query="SELECT eu FROM EndUser eu WHERE eu.key = :key"
    ),
    @NamedQuery(
    		name="EndUser.getByUserId",
    		query="SELECT eu FROM EndUser eu WHERE eu.userId = :userId"
    ),
    @NamedQuery(
    		name="EndUser.getByApplicationId",
    		query="SELECT eu FROM EndUser eu WHERE eu.applicationId = :applicationId"
    ),
})
public class EndUser {
    private static final Logger log = Logger.getLogger(EndUser.class.getName());
    
	public final static Integer MAX_PAGE_SIZE = 1000;
	public final static Integer DEFAULT_PAGE_SIZE = 25;
	
	private String userId;  // unique ID assigned by 3rd party app and passed in to rSkybox
	private String userName;
	private String application;
	private String version;
	private String instanceUrl;
	private String summary;  // runtime environment contextual info (e.g. os version, app version, etc)
	private Date createdGmtDate;
	private Date versionUpdatedGmtDate;
	private String applicationId;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    public Key getKey() {
        return key;
    }

	public void setKey(Key key) {
        this.key = key;
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

	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}

	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	public String getInstanceUrl() {
		return instanceUrl;
	}
	public void setInstanceUrl(String instanceUrl) {
		this.instanceUrl = instanceUrl;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Date getCreatedGmtDate() {
		return createdGmtDate;
	}

	public void setCreatedGmtDate(Date createdGmtDate) {
		this.createdGmtDate = createdGmtDate;
	}

	public Date getVersionUpdatedGmtDate() {
		return versionUpdatedGmtDate;
	}

	public void setVersionUpdatedGmtDate(Date versionUpdatedGmtDate) {
		this.versionUpdatedGmtDate = versionUpdatedGmtDate;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	
	public static EndUser build(com.google.appengine.api.datastore.Entity theEntity) {
		EndUser eu = new EndUser();
		eu.setKey(theEntity.getKey());
		eu.setUserId((String)theEntity.getProperty("userId"));
		eu.setUserName((String)theEntity.getProperty("userName"));
		eu.setApplication((String)theEntity.getProperty("application"));
		eu.setVersion((String)theEntity.getProperty("version"));
		eu.setInstanceUrl((String)theEntity.getProperty("instanceUrl"));
		eu.setSummary((String)theEntity.getProperty("summary"));
		eu.setCreatedGmtDate((Date)theEntity.getProperty("createdGmtDate"));
		eu.setVersionUpdatedGmtDate((Date)theEntity.getProperty("versionUpdatedGmtDate"));
		eu.setApplicationId((String)theEntity.getProperty("applicationId"));
		return eu;
	}
}
