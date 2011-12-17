package com.stretchcom.rskybox.models;

import java.util.ArrayList;
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
import javax.persistence.Transient;

import org.json.JSONArray;
import org.json.JSONException;
import org.restlet.data.Status;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
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
    		name="User.getAll",
    		query="SELECT u FROM User u"
    ),
    @NamedQuery(
    		name="User.getByKey",
    		query="SELECT u FROM User u WHERE u.key = :key"
    ),
    @NamedQuery(
    		name="User.getByEmailAddress",
    		query="SELECT u FROM User u WHERE u.emailAddress = :emailAddress"
    ),
    @NamedQuery(
    		name="User.getByEmailAddressAndPassword",
    		query="SELECT u FROM User u WHERE u.emailAddress = :emailAddress and u.password = :password"
    ),
    @NamedQuery(
    		name="User.getByToken",
    		query="SELECT u FROM User u WHERE u.token = :token"
    ),
    @NamedQuery(
    		name="User.getByPhoneNumber",
    		query="SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber"
    ),
    @NamedQuery(
    		name="User.getByPhoneNumberAndPassword",
    		query="SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber and u.password = :password"
    ),
})
public class User {
    private static final Logger log = Logger.getLogger(User.class.getName());
	public static final String CURRENT = "current";
	public static final int MINIMUM_PASSWORD_SIZE = 6;
	
	private String firstName;
	private String lastName;
	private String emailAddress;
	private String phoneNumber;
	private String smsEmailAddress;
	private Boolean sendEmailNotifications = false;
	private Boolean sendSmsNotifications = false;
	private String organizationId;
	private String token;
	private String authHeader;
	private String password;
	private String passwordResetQuestion;
	private String passwordResetAnswer;
	private String phoneNumberConfirmationCode;
	private Text photoBase64;
	private Text thumbNailBase64;
	private Boolean isSmsConfirmed = false;
	private Boolean isEmailConfirmed = false;
	private String emailConfirmationCode;
	private String smsConfirmationCode;

	@Transient
	private Boolean isSuperAdmin = false;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    public Key getKey() {
        return key;
    }
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
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

	public String getSmsEmailAddress() {
		return smsEmailAddress;
	}

	public void setSmsEmailAddress(String smsEmailAddress) {
		this.smsEmailAddress = smsEmailAddress;
	}
	
	public Boolean getIsSuperAdmin() {
		return isSuperAdmin;
	}
	public void setIsSuperAdmin(Boolean isSuperAdmin) {
		this.isSuperAdmin = isSuperAdmin;
	}

	public Boolean getSendEmailNotifications() {
		return sendEmailNotifications;
	}
	public void setSendEmailNotifications(Boolean sendEmailNotifications) {
		this.sendEmailNotifications = sendEmailNotifications;
	}

	public Boolean getSendSmsNotifications() {
		return sendSmsNotifications;
	}
	public void setSendSmsNotifications(Boolean sendSmsNotifications) {
		this.sendSmsNotifications = sendSmsNotifications;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getAuthHeader() {
		return authHeader;
	}

	public void setAuthHeader(String authHeader) {
		this.authHeader = authHeader;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordResetQuestion() {
		return passwordResetQuestion;
	}

	public void setPasswordResetQuestion(String passwordResetQuestion) {
		this.passwordResetQuestion = passwordResetQuestion;
	}

	public String getPasswordResetAnswer() {
		return passwordResetAnswer;
	}

	public void setPasswordResetAnswer(String passwordResetAnswer) {
		this.passwordResetAnswer = passwordResetAnswer;
	}

	public String getPhoneNumberConfirmationCode() {
		return phoneNumberConfirmationCode;
	}

	public void setPhoneNumberConfirmationCode(String phoneNumberConfirmationCode) {
		this.phoneNumberConfirmationCode = phoneNumberConfirmationCode;
	}

	public Text getPhotoBase64() {
		return photoBase64;
	}

	public void setPhotoBase64(Text photoBase64) {
		this.photoBase64 = photoBase64;
	}

	public Text getThumbNailBase64() {
		return thumbNailBase64;
	}

	public void setThumbNailBase64(Text thumbNailBase64) {
		this.thumbNailBase64 = thumbNailBase64;
	}

	public Boolean getIsSmsConfirmed() {
		return isSmsConfirmed;
	}

	public void setIsSmsConfirmed(Boolean isSmsConfirmed) {
		this.isSmsConfirmed = isSmsConfirmed;
	}

	public Boolean getIsEmailConfirmed() {
		return isEmailConfirmed;
	}

	public void setIsEmailConfirmed(Boolean isEmailConfirmed) {
		this.isEmailConfirmed = isEmailConfirmed;
	}

	public String getEmailConfirmationCode() {
		return emailConfirmationCode;
	}

	public void setEmailConfirmationCode(String emailConfirmationCode) {
		this.emailConfirmationCode = emailConfirmationCode;
	}
	
	public String getSmsConfirmationCode() {
		return smsConfirmationCode;
	}

	public void setSmsConfirmationCode(String smsConfirmationCode) {
		this.smsConfirmationCode = smsConfirmationCode;
	}

	// Sends a notification (if appropriate) to all active members of the specified application
	// TODO notifications options should be application specific. When that happens, also put both the email address and phone
	//      number in AppMember then the notifications can be sent without having to retrieve the User entity for each AppMember
	public static void sendNotifications(String theApplicationId, String theMessage) {
        EntityManager em = EMF.get().createEntityManager();
        
        try {
            List<AppMember> appMembers = new ArrayList<AppMember>();
            JSONArray ja = new JSONArray();
            appMembers = (List<AppMember>) em.createNamedQuery("AppMember.getAllWithApplicationIdAndStatus")
            		.setParameter("applicationId", theApplicationId)
            		.setParameter("status", AppMember.ACTIVE_STATUS)
            		.getResultList();
            
            if(appMembers.size() > 0) {
            	log.info("email/SMS message = '" + theMessage + "' sent to " + appMembers.size() + " members.");
            } else {
            	log.info("no active members found for specified application");
            }
            
            String subject = "notification";
            String enhancedEmailMessage = theMessage + "<br><br>" + RskyboxApplication.APPLICATION_BASE_URL;
            String enhancedSmsMessage = theMessage + "  " + RskyboxApplication.APPLICATION_BASE_URL;
            for (AppMember am : appMembers) {
            	String userId = am.getUserId();
            	if(userId == null) {
            		log.severe("should never happen -- user ID for an active user is null");
            	} else {
                	User user = User.getUserWithId(userId);
                	if(user == null) {
                		log.severe("could not get user with user ID");
                	} else {
                        if(user.getIsEmailConfirmed() && user.getSendEmailNotifications() != null && user.getSendEmailNotifications()) {
                        	log.info("sending email to " + user.getEmailAddress());
                        	Emailer.send(user.getEmailAddress(), subject, enhancedEmailMessage, Emailer.NO_REPLY);
                        }
                        if(user.getIsSmsConfirmed() && user.getSendSmsNotifications() != null && user.getSendSmsNotifications()) {
                        	log.info("sending SMS to " + user.getSmsEmailAddress());
                        	Emailer.send(user.getSmsEmailAddress(), subject, enhancedSmsMessage, Emailer.NO_REPLY);
                        }
                	}
            	}
            }
        } catch (Exception e) {
            log.severe("exception = " + e.getMessage());
        	e.printStackTrace();
        }
	}
	
	// Returns list of users matching specified email address; null if no matching users found.
	public static User getUserWithId(String theUserId) {
        EntityManager em = EMF.get().createEntityManager();
        User user = null;
        
        Key userKey = null;
        try {
			userKey = KeyFactory.stringToKey(theUserId);
		} catch (IllegalArgumentException e1) {
			log.severe("exception = " + e1.getMessage());
			e1.printStackTrace();
			return null;
		}

		try {
    		user = (User)em.createNamedQuery("User.getByKey")
				.setParameter("key", userKey)
				.getSingleResult();
		} catch (NoResultException e) {
			log.severe("user not found");
			e.printStackTrace();
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more Users have the same key");
			e.printStackTrace();
		}
		return user;
	}
	
	// Returns list of users matching specified email address; null if no matching users found.
	public static List<User> getUsersWithEmailAddress(String theEmailAddress) {
        EntityManager em = EMF.get().createEntityManager();
        Boolean isAuthenticated = false;
        List<User> users = null;

		try {
    		users = (List<User>)em.createNamedQuery("User.getByEmailAddress")
				.setParameter("emailAddress", theEmailAddress.toLowerCase())
				.getResultList();
		} catch (NoResultException e) {
			// do nothing - ok if email address specified is not currently in use
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more Users have the same email address");
		}
		return users;
	}
	
	public static Boolean isAuthenticated(String theEmailAddress) {
        EntityManager em = EMF.get().createEntityManager();
        Boolean isAuthenticated = false;

		try {
    		User user = (User)em.createNamedQuery("User.getByEmailAddress")
				.setParameter("emailAddress", theEmailAddress.toLowerCase())
				.getSingleResult();
    		isAuthenticated = true;
		} catch (NoResultException e) {
			log.info("Google account user not found");
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more google account users have same key");
		}
		return isAuthenticated;
	}
	
	public static Boolean isAdmin() {
    	UserService userService = UserServiceFactory.getUserService();
    	if(userService == null) return false;
    	return (userService.isUserLoggedIn() && userService.isUserAdmin());
	}
	
	// Create a new user.
	// theEmailAddress: email address of new user.  Not checked for uniqueness. Assumed the caller knows email address is unique.
	// theNickName: from Google App Engine User object.  Could just be the email address again. Used to set new user's first name.
	public static User createUser(String theEmailAddress, String theNickName) {
        EntityManager em = EMF.get().createEntityManager();
        User user = null;
        
        em.getTransaction().begin();
		try {
			user = new User();
			user.setEmailAddress(theEmailAddress);
			user.setFirstName(theNickName);
			log.info("creating new user with email address = " + theEmailAddress);
			em.persist(user);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.severe("User::createUser() exception = " + e.getMessage());
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
		return user;
	}

	public List<Application> getApplications() {
        EntityManager em = EMF.get().createEntityManager();
        User user = null;
        
		List<AppMember> appMembers = null;
    	List<Application> applications = new ArrayList<Application>();
        try {
        	appMembers = (List<AppMember>)em.createNamedQuery("AppMember.getByUserId")
				.setParameter("userId", KeyFactory.keyToString(this.key))
				.getResultList();
        	log.info("number of applications found for currentUser = " + appMembers.size());
        	
        	for(AppMember au : appMembers) {
                try {
					try {
						Key appKey = KeyFactory.stringToKey(au.getApplicationId());
	            		Application application = (Application)em.createNamedQuery("Application.getByKey")
	            				.setParameter("key", appKey)
	            				.getSingleResult();
	            		application.setMemberRole(au.getRole());
	            		applications.add(application);
					} catch (IllegalArgumentException e) {
						log.severe("User.getApplications(): could not convert AppMember ApplicationId to an Application key");
					}
        		} catch (NoResultException e) {
        			log.info("Application referred to in ApplicationsUsers no longer exists");
        		} catch (NonUniqueResultException e) {
        			log.severe("should never happen - two or more applications have same key and application ID");
        		}
        	}
		} catch (Exception e) {
			log.severe("Error reading ApplicatinsUsers entity. exception = " + e.getMessage());
		}
        
		return applications;
	}
	
	
	public static String verifyUserMemberOfApplication(String theEmailAddress, String theApplicationId) {
        EntityManager em = EMF.get().createEntityManager();
        String apiStatus = ApiStatusCode.SUCCESS;

        User user = null;
        try {
    		user = (User)em.createNamedQuery("User.getByEmailAddress")
				.setParameter("emailAddress", theEmailAddress.toLowerCase())
				.getSingleResult();
		} catch (NoResultException e) {
			log.info("Google account user not found");
			apiStatus = ApiStatusCode.USER_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more google account users have same key");
			apiStatus = ApiStatusCode.SERVER_ERROR;
		}
        
        if(user != null) {
        	String userId = KeyFactory.keyToString(user.getKey());
            try {
        		AppMember appsUsers = (AppMember)em.createNamedQuery("AppMember.getByApplicationIdAndUserId")
        				.setParameter("applicationId", theApplicationId)
        				.setParameter("userId", userId)
        				.getSingleResult();
    		} catch (NoResultException e) {
    			log.info("User is not authorized to use this application");
    			apiStatus = ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION;
    		} catch (NonUniqueResultException e) {
    			log.severe("should never happen - two or more userId/applicationId combinations in ApplicationsUsers");
    			apiStatus = ApiStatusCode.SERVER_ERROR;
    		}
        }
        
		return apiStatus;
	}
	
	// returns User entity if found; null otherwise
	public static User getUser(EntityManager em, String theEmailAddress, String theEncryptedPassword) {
        User user = null;
        try {
        	if(theEncryptedPassword != null) {
        		log.info("query user by email address = " + theEmailAddress + " and encrypted password = " + theEncryptedPassword);
        		user = (User)em.createNamedQuery("User.getByEmailAddressAndPassword")
        				.setParameter("emailAddress", theEmailAddress.toLowerCase())
        				.setParameter("password", theEncryptedPassword)
        				.getSingleResult();
        	} else {
        		user = (User)em.createNamedQuery("User.getByEmailAddress")
        				.setParameter("emailAddress", theEmailAddress.toLowerCase())
        				.getSingleResult();
        	}
    		log.info("user with email address = " + theEmailAddress + " found");
		} catch (NoResultException e) {
			// not an error is user not found
			log.info("user with email address = " + theEmailAddress + " NOT found");
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more google account users have same emailAddress");
		}
        
        return user;
	}
	
	// Returns the user with the specified token or null if user not found
	public static User getUserWithToken(String theToken) {
        EntityManager em = EMF.get().createEntityManager();

        User user = null;
        try {
    		user = (User)em.createNamedQuery("User.getByToken")
				.setParameter("token", theToken)
				.getSingleResult();
    		log.info("user with token = " + theToken + " found");
		} catch (NoResultException e) {
			// not an error is user not found
			log.info("user with token = " + theToken + " NOT found");
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same token");
		}
        
        return user;
	}
	
	// returns User entity if found; null otherwise
	public static User getUserWithPhoneNumber(EntityManager em, String thePhoneNumber, String theEncryptedPassword) {
        User user = null;
        try {
        	if(theEncryptedPassword != null) {
        		user = (User)em.createNamedQuery("User.getByPhoneNumberAndPassword")
        				.setParameter("phoneNumber", thePhoneNumber)
        				.setParameter("password", theEncryptedPassword)
        				.getSingleResult();
        	} else {
        		user = (User)em.createNamedQuery("User.getByPhoneNumber")
        				.setParameter("phoneNumber", thePhoneNumber)
        				.getSingleResult();
        	}
    		log.info("user with phoneNumber = " + thePhoneNumber + " found");
		} catch (NoResultException e) {
			// not an error is user not found
			log.info("user with phoneNumber = " + thePhoneNumber + " NOT found");
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same phoneNumber (and maybe password)");
		}
        
        return user;
	}

}
