package com.stretchcom.mobilePulse.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

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
import org.restlet.data.Status;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.stretchcom.mobilePulse.server.ApiStatusCode;
import com.stretchcom.mobilePulse.server.EMF;
import com.stretchcom.mobilePulse.server.MobilePulseApplication;
import com.stretchcom.mobilePulse.server.UsersResource;
import com.stretchcom.mobilePulse.server.Emailer;
import com.stretchcom.mobilePulse.server.Utility;

@Entity
@NamedQueries({
    @NamedQuery(
    		name="AppMember.getAll",
    		query="SELECT am FROM AppMember am"
    ),
    @NamedQuery(
    		name="AppMember.getAllWithApplicationId",
    		query="SELECT am FROM AppMember am and am.applictionId = :applicationId"
    ),
    @NamedQuery(
    		name="AppMember.getByUserId",
    		query="SELECT am FROM AppMember am WHERE am.userId = :userId"
    ),
    @NamedQuery(
    		name="AppMember.getByKey",
    		query="SELECT am FROM AppMember am WHERE am.key = :key"
    ),
    @NamedQuery(
    		name="AppMember.getByApplicationId",
    		query="SELECT am FROM AppMember am WHERE am.applicationId = :applicationId"
    ),
    @NamedQuery(
    		name="AppMember.getByApplicationIdAndUserId",
    		query="SELECT am FROM AppMember am WHERE am.applicationId = :applicationId and am.userId = :userId"
    ),
})
public class AppMember {
    private static final Logger log = Logger.getLogger(AppMember.class.getName());
    public static final String OWNER_ROLE = "owner";
    public static final String DEVELOPER_ROLE = "developer";
    public static final String PENDING_STATUS = "pending";
    public static final String ACTIVE_STATUS = "active";
	
	private String userId;
	private String emailAddress;
	private String applicationId;
	private String role;
	private String status;
	private Date createdGmtDate;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    public Key getKey() {
        return key;
    }
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}

	public Date getCreatedGmtDate() {
		return createdGmtDate;
	}

	public void setCreatedGmtDate(Date createdGmtDate) {
		this.createdGmtDate = createdGmtDate;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public Boolean isStatusValid(String theStatus) {
		if(theStatus.equals(AppMember.ACTIVE_STATUS) || theStatus.equals(AppMember.PENDING_STATUS)) return true;
		return false;
	}
	
	public Boolean isRoleValid(String theRole) {
		if(theRole.equals(AppMember.DEVELOPER_ROLE) || theRole.equals(AppMember.OWNER_ROLE)) return true;
		return false;
	}
	
	// Called when a user verifies membership with an application.  The userId is set in AppMember by finding the User with
	// the emailAddress stored in AppMember when the AppMember entity was created.  At this point, we know a registered
	// user is associated with the email address.
	public void setUserIdViaEmailAddress() {
        EntityManager em = EMF.get().createEntityManager();
        try {
        	User user = (User)em.createNamedQuery("User.getByEmailAddress")
				.setParameter("emailAddress", this.getEmailAddress())
				.getSingleResult();
        	this.setUserId(KeyFactory.keyToString(user.getKey()));
		} catch (NoResultException e) {
			log.info("setUserId(): cannot find user using email address");
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same email address");
		}
        
		return;
	}
	
}
