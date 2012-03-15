package com.stretchcom.rskybox.models;

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

@Entity
@NamedQueries({
    @NamedQuery(
    		name="AppMember.getAll",
    		query="SELECT am FROM AppMember am"
    ),
    @NamedQuery(
    		name="AppMember.getAllWithApplicationId",
    		query="SELECT am FROM AppMember am WHERE am.applicationId = :applicationId"
    ),
    @NamedQuery(
    		name="AppMember.getAllWithApplicationIdAndStatus",
    		query="SELECT am FROM AppMember am WHERE am.applicationId = :applicationId and am.status = :status"
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
    @NamedQuery(
    		name="AppMember.getByApplicationIdAndEmailAddress",
    		query="SELECT am FROM AppMember am WHERE am.applicationId = :applicationId and am.emailAddress = :emailAddress"
    ),
    @NamedQuery(
    		name="AppMember.getByEmailAddressAndEmailConfirmationCode",
    		query="SELECT am FROM AppMember am WHERE am.emailAddress = :emailAddress and am.emailConfirmationCode = :emailConfirmationCode"
    ),
    @NamedQuery(
    		name="AppMember.getByApplicationIdAndEmailAddressAndEmailConfirmationCode",
    		query="SELECT am FROM AppMember am WHERE am.applicationId = :applicationId and am.emailAddress = :emailAddress and am.emailConfirmationCode = :emailConfirmationCode"
    ),
    @NamedQuery(
    		name="AppMember.getByEmailAddressAndConfirmInitiated",
    		query="SELECT am FROM AppMember am WHERE am.emailAddress = :emailAddress and am.confirmInitiated = :confirmInitiated"
    ),
})
public class AppMember {
    private static final Logger log = Logger.getLogger(AppMember.class.getName());
    public static final String OWNER_ROLE = "owner";
    public static final String MANAGER_ROLE = "manager";
    public static final String MEMBER_ROLE = "member";
    public static final String PENDING_STATUS = "pending";
    public static final String ACTIVE_STATUS = "active";
	
	private String userId;
	private String emailAddress;
	private String phoneNumber;
	private String applicationId;
	private String applicationName;
	private String role;
	private String status;
	private Date createdGmtDate;
	private String emailConfirmationCode;
	private Boolean confirmInitiated = false;

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

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
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

	public String getEmailConfirmationCode() {
		return emailConfirmationCode;
	}

	public void setEmailConfirmationCode(String emailConfirmationCode) {
		this.emailConfirmationCode = emailConfirmationCode;
	}

	public Boolean getConfirmInitiated() {
		return confirmInitiated;
	}

	public void setConfirmInitiated(Boolean confirmInitiated) {
		this.confirmInitiated = confirmInitiated;
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
		if(theRole.equals(AppMember.MEMBER_ROLE) || theRole.equals(AppMember.MANAGER_ROLE) || theRole.equals(AppMember.OWNER_ROLE)) return true;
		return false;
	}
	
	// currently, only owner has owner authority
	public Boolean hasOwnerAuthority() {
		if(this.role == null) {return false;}
		if(this.role.equalsIgnoreCase(AppMember.OWNER_ROLE)) {
			return true;
		}
		return false;
	}
	
	// both an owner and manager have manager authority
	public Boolean hasManagerAuthority() {
		if(this.role == null) {return false;}
		if(this.role.equalsIgnoreCase(AppMember.OWNER_ROLE) || this.role.equalsIgnoreCase(AppMember.MANAGER_ROLE)) {
			return true;
		}
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
	
	public static AppMember getAppMember(String theApplicationId, String theUserId) {
        EntityManager em = EMF.get().createEntityManager();
        AppMember appMember = null;
        try {
    			appMember = (AppMember)em.createNamedQuery("AppMember.getByApplicationIdAndUserId")
    				.setParameter("applicationId", theApplicationId)
    				.setParameter("userId", theUserId)
    				.getSingleResult();
		} catch (NoResultException e) {
			log.info("appMember not found for applicationId = " + theApplicationId + " and userId = " + theUserId);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more userId/applicationId combinations in AppMember");
		}
        return appMember;
	}
	
	public static AppMember getAppMemberWithEmailAddress(String theApplicationId, String theEmailAddress) {
        EntityManager em = EMF.get().createEntityManager();
        AppMember appMember = null;
        try {
    			appMember = (AppMember)em.createNamedQuery("AppMember.getByApplicationIdAndEmailAddress")
    				.setParameter("applicationId", theApplicationId)
    				.setParameter("emailAddress", theEmailAddress)
    				.getSingleResult();
		} catch (NoResultException e) {
			log.info("appMember not found for applicationId = " + theApplicationId + " and emailAddress = " + theEmailAddress);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more emailAddress/applicationId combinations in AppMember");
		}
        return appMember;
	}
	
	public static AppMember addAppMember(String theApplicationId, Application theApplication, User theCurrentUser, String theRole) {
        EntityManager em = EMF.get().createEntityManager();
        AppMember appMember = null;
        
        // First verify user is NOT already an member of this application
        // ::OPTIMIZATION:: remove this check to reduce CPU time
    	String userId = KeyFactory.keyToString(theCurrentUser.getKey());
        try {
			appMember = (AppMember)em.createNamedQuery("AppMember.getByApplicationIdAndUserId")
				.setParameter("applicationId", theApplicationId)
				.setParameter("userId", userId)
				.getSingleResult();
			
			log.severe("ERROR: user with userId = " + userId + " is already a member of application with applicationId = " + theApplicationId);
			return null;
		} catch (NoResultException e) {
			log.info("as expected, user with userId = " + userId + " is not already a member of application with applicationId = " + theApplicationId);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more userId/applicationId combinations in AppMember");
			return null;
		}
        
    	EntityManager em2 = EMF.get().createEntityManager();
    	try {
    		em2.getTransaction().begin();
    		appMember = new AppMember();
    		appMember.setApplicationId(theApplicationId);
    		appMember.setUserId(userId);
    		appMember.setRole(theRole);
    		appMember.setStatus(AppMember.ACTIVE_STATUS);
    		appMember.setEmailAddress(theCurrentUser.getEmailAddress());
    		appMember.setPhoneNumber(theCurrentUser.getPhoneNumber());
    		appMember.setCreatedGmtDate(new Date());
    		appMember.setApplicationName(theApplication.getName());
    		em2.persist(appMember);
			em2.getTransaction().commit();
    	} catch (Exception e) {
        	log.severe("exeception = " + e.getMessage());
        	e.printStackTrace();
		} finally {
		    if (em2.getTransaction().isActive()) {
		    	em2.getTransaction().rollback();
		    }
		    em2.close();
		}
        return appMember;
	}
	
	// Send member verification email
	// TODO add SMS capability
	public static void sendMemberConfirmation(AppMember theNewAppMember, String theApplicationId) {
		try {
			Application application = Application.getApplicationWithId(theApplicationId);
			if(application == null) {
				log.severe("could not send member verification due to bad application ID");
				return;
			}
            
			String emailAddress = theNewAppMember.getEmailAddress();
			if(emailAddress == null || emailAddress.trim().length() == 0) {
				log.severe("could not send member verification due to empty email address");
				return;
			}

			String encodedEmailAddress = Reference.encode(emailAddress);
			String encodedConfirmationCode = Reference.encode(theNewAppMember.getEmailConfirmationCode());
			
	    	StringBuffer urlBuf = new StringBuffer();
	    	urlBuf.append(RskyboxApplication.MEMBER_VERIFICATION_PAGE);
	    	urlBuf.append("?");
	    	urlBuf.append("applicationId=");
	    	urlBuf.append(theApplicationId);
	    	urlBuf.append("&");
	    	urlBuf.append("emailAddress=");
	    	urlBuf.append(encodedEmailAddress);
	    	urlBuf.append("&");
	    	urlBuf.append("emailConfirmationCode=");
	    	urlBuf.append(encodedConfirmationCode);
	    	urlBuf.append("&");
	    	urlBuf.append("memberConfirmation=");
	    	urlBuf.append("true");

			String subject = "rSkybox verification";
            StringBuffer coreMsg = new StringBuffer();
            coreMsg.append("You have been added as a member of the rSkybox application ");
            coreMsg.append(application.getName());
			
            String body = Emailer.getConfirmedEmailBody(coreMsg.toString(), urlBuf.toString(), theNewAppMember.getEmailConfirmationCode());
            Emailer.send(emailAddress, subject, body, Emailer.NO_REPLY);
		} catch (Exception e) {
            log.severe("exception = " + e.getMessage());
        	e.printStackTrace();
        }
	}
	
	// if the user has a pending membership in any application, that membership is confirmed
	// returns true if membership confirmed; false otherwise
	// NOTE: only support finding a user via emailAddress right now
	public static Boolean confirmMember(User theUser) {
        EntityManager em = EMF.get().createEntityManager();
        List<AppMember> appMembers = null;
        Boolean wasConfirmed = false;
        try {
        	String emailAddress = theUser.getEmailAddress();
			appMembers = (List<AppMember>)em.createNamedQuery("AppMember.getByEmailAddressAndConfirmInitiated")
				.setParameter("emailAddress", emailAddress)
				.setParameter("confirmInitiated", true)
				.getResultList();
			log.info("confirming " + appMembers.size() + " memberships");
			
			for(AppMember am : appMembers) {
		        em.getTransaction().begin();
		        am.setUserId(KeyFactory.keyToString(theUser.getKey()));
		        am.setStatus(AppMember.ACTIVE_STATUS);
		        am.setConfirmInitiated(false);
				wasConfirmed = true;
	            em.persist(am);
	            em.getTransaction().commit();
			}
		} catch (Exception e) {
			log.severe("exception = " + e.getMessage());
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        return wasConfirmed;
	}
}
