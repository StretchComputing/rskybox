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
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.json.JSONArray;
import org.json.JSONException;
import org.restlet.data.Reference;
import org.restlet.data.Status;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.stretchcom.rskybox.server.ApiStatusCode;
import com.stretchcom.rskybox.server.EMF;
import com.stretchcom.rskybox.server.Emailer;
import com.stretchcom.rskybox.server.RskyboxApplication;
import com.stretchcom.rskybox.server.UsersResource;
import com.stretchcom.rskybox.server.Utility;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Only one notification entity per user. The one notification holds the notification details for all a user's notifications for all their applications
// Each of the user's applications has it's own NotificationDetails
// sendGmtDate field is shared among the applications and represents the shortest notifications time of notifications queued up
// (assumption: each application will eventually support setting a minimum notifications interval)
// Cron job that sends notifications can do so using just the data in this entity (not normalized for performance reasons)
// After notification sent, sendGmtDate is set to 2099 and notificationsDetails are cleared
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@Entity
@NamedQueries({
    @NamedQuery(
    		name="Notification.getByApplicationId",
    		query="SELECT n FROM Notification n WHERE n.applicationIds = :applicationId"
    ),
    @NamedQuery(
    		name="Notification.getByUserIdAndSendGmtDate",
    		query="SELECT n FROM Notification n WHERE n.userId = :userId and n.sendGmtDate < :sendGmtDate"
    ),
})
public class Notification {
    private static final Logger log = Logger.getLogger(Notification.class.getName());
	
	private String userId;
	private String emailAddress;
	private String phoneNumber;
	private Date sendGmtDate;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    public Key getKey() {
        return key;
    }
	
	@Basic
	private List<String> applicationIds;
	
	@Basic
	private List<String> applicationNames;
	
	@Basic
	private List<String> messages;
	
	@Basic
	private List<Integer> clientLogCount;
	
	@Basic
	private List<Integer> crashCount;
	
	@Basic
	private List<Integer> feedbackCount;

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getSendGmtDate() {
		return sendGmtDate;
	}

	public void setSendGmtDate(Date sendGmtDate) {
		this.sendGmtDate = sendGmtDate;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Boolean createNotificationDetails(List<NotificationDetails> theNewNotificationDetailsList) {
		// TODO implement
		return true;
	}
	
	public List<AppAction> getNotificationDetails() {
		// TODO implement
		return null;
	}
	
	public Boolean updateNotificationDetails(NotificationDetails theNewNotificationDetails) {
		// TODO implement
		return true;
	}
}
