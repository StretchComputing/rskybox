package com.stretchcom.rskybox.models;

import java.util.logging.Logger;

public class NotificationDetails {
    private static final Logger log = Logger.getLogger(NotificationDetails.class.getName());
    
	private String applicationId;
	private String applicationName;
	private Integer clientLogCount;
	private String clientLogMessage;
	private String clientLogId;
	private Integer updatedLogCount;
	private String updatedLogMessage;
	private String updatedLogId;
	private Integer crashCount;
	private String crashMessage;
	private String crashId;
	private Integer feedbackCount;
	private String feedbackMessage;
	private String feedbackId;
	private String emailAddress;
	private String smsEmailAddress;
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
	
	public Integer getUpdatedLogCount() {
		return updatedLogCount;
	}
	public void setUpdatedLogCount(Integer updatedLogCount) {
		this.updatedLogCount = updatedLogCount;
	}
	
	public String getUpdatedLogMessage() {
		return updatedLogMessage;
	}
	public void setUpdatedLogMessage(String updatedLogMessage) {
		this.updatedLogMessage = updatedLogMessage;
	}
	
	public String getUpdatedLogId() {
		return updatedLogId;
	}
	public void setUpdatedLogId(String updatedLogId) {
		this.updatedLogId = updatedLogId;
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
	
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public String getSmsEmailAddress() {
		return smsEmailAddress;
	}
	public void setSmsEmailAddress(String smsEmailAddress) {
		this.smsEmailAddress = smsEmailAddress;
	}
}
