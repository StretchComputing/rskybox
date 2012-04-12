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
	private Integer clientLogCount;
	private String clientLogMessage;
	private String clientLogId;
	private Integer crashCount;
	private String crashMessage;
	private String crashId;
	private Integer feedbackCount;
	private String feedbackMessage;
	private String feedbackId;
	
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
	
	public Integer getClientLogCount() {
		return clientLogCount;
	}
	public void setClientLogCount(Integer clientLogCount) {
		this.clientLogCount = clientLogCount;
	}
	
	public String getClientLogMessage() {
		return clientLogMessage;
	}
	public void setClientLogMessage(String clientLogMessage) {
		this.clientLogMessage = clientLogMessage;
	}

	public String getClientLogId() {
		return clientLogId;
	}
	public void setClientLogId(String clientLogId) {
		this.clientLogId = clientLogId;
	}

	public Integer getCrashCount() {
		return crashCount;
	}
	public void setCrashCount(Integer crashCount) {
		this.crashCount = crashCount;
	}
	
	public String getCrashMessage() {
		return crashMessage;
	}
	public void setCrashMessage(String crashMessage) {
		this.crashMessage = crashMessage;
	}

	public String getCrashId() {
		return crashId;
	}
	public void setCrashId(String crashId) {
		this.crashId = crashId;
	}

	public Integer getFeedbackCount() {
		return feedbackCount;
	}
	public void setFeedbackCount(Integer feedbackCount) {
		this.feedbackCount = feedbackCount;
	}

	public String getFeedbackMessage() {
		return feedbackMessage;
	}
	public void setFeedbackMessage(String feedbackMessage) {
		this.feedbackMessage = feedbackMessage;
	}

	public String getFeedbackId() {
		return feedbackId;
	}
	public void setFeedbackId(String feedbackId) {
		this.feedbackId = feedbackId;
	}
}
