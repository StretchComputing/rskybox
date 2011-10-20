package com.stretchcom.mobilePulse.models;

import java.util.Date;
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
    		name="BetaTester.getAll",
    		query="SELECT bt FROM BetaTester bt"
    ),
    @NamedQuery(
    		name="BetaTester.getByKey",
    		query="SELECT bt FROM BetaTester bt WHERE bt.key = :key"
    ),
})
public class BetaTester {
    private static final Logger log = Logger.getLogger(BetaTester.class.getName());
	
	private String userName;
	private String application;
	private String version;
	private String instanceUrl;
	private Date createdGmtDate;
	private Date versionUpdatedGmtDate;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    public Key getKey() {
        return key;
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
}
