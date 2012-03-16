package com.stretchcom.rskybox.server;

import java.util.Date;

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
    		name="CronLog.getByKey",
    		query="SELECT cl FROM CronLog cl WHERE cl.key = :key"
    ),
})
public class CronLog {
	
	private String jobName;
	private String logMessage;          
	private Date createdGmtDate;

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
	
	public String getLogMessage() {
		return logMessage;
	}

	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}
	
    public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

}
