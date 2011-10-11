package com.stretchcom.mobilePulse.models;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.restlet.data.Status;

import com.google.appengine.api.datastore.Key;
import com.stretchcom.mobilePulse.server.ApiStatusCode;
import com.stretchcom.mobilePulse.server.EMF;
import com.stretchcom.mobilePulse.server.UsersResource;
import com.stretchcom.mobilePulse.server.Emailer;
import com.stretchcom.mobilePulse.server.Utility;

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
})
public class User {
    private static final Logger log = Logger.getLogger(User.class.getName());
	public static final String CURRENT = "current";
	
	private String firstName;
	private String lastName;
	private String emailAddress;
	private String phoneNumber;
	private String smsEmailAddress;
	private Boolean sendEmailNotifications;
	private Boolean sendSmsNotifications;
	private Boolean isAdmin = false;

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
	
	public Boolean getIsAdmin() {
		return isAdmin;
	}
	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
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
	
	public static void sendNotifications(String theMessage) {
        EntityManager em = EMF.get().createEntityManager();
        
        try {
            List<User> users = new ArrayList<User>();
            JSONArray ja = new JSONArray();
            users = (List<User>) em.createNamedQuery("User.getAll").getResultList();
            
            if(users.size() > 0) {
            	log.info("email/SMS message to be sent = " + theMessage);
            }
            
            String subject = "notification";
            for (User user : users) {
                if(user.getSendEmailNotifications()) {
                	log.info("sending email to " + user.getEmailAddress());
                	Emailer.send(user.getEmailAddress(), subject, theMessage, Emailer.NO_REPLY);
                }
                if(user.getSendSmsNotifications()) {
                	log.info("sending SMS to " + user.getSmsEmailAddress());
                	Emailer.send(user.getSmsEmailAddress(), subject, theMessage, Emailer.NO_REPLY);
                }
            }
        } catch (Exception e) {
            log.severe("exception = " + e.getMessage());
        	e.printStackTrace();
        }
	}
}
