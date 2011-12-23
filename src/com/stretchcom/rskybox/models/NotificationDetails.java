package com.stretchcom.rskybox.models;

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

public class NotificationDetails {
    private static final Logger log = Logger.getLogger(NotificationDetails.class.getName());
    
	private String applicationId;
	private String applicationName;
	private String message;
	private Integer clientLogCount;
	private Integer crashCount;
	private Integer feedbackCount;

	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public Integer getClientLogCount() {
		return clientLogCount;
	}
	public void setClientLogCount(Integer clientLogCount) {
		this.clientLogCount = clientLogCount;
	}
	
	public Integer getCrashCount() {
		return crashCount;
	}
	public void setCrashCount(Integer crashCount) {
		this.crashCount = crashCount;
	}
	
	public Integer getFeedbackCount() {
		return feedbackCount;
	}
	public void setFeedbackCount(Integer feedbackCount) {
		this.feedbackCount = feedbackCount;
	}
}
