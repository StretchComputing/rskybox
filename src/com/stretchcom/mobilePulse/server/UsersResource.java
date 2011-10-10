package com.stretchcom.mobilePulse.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.stretchcom.mobilePulse.models.MobileCarrier;
import com.stretchcom.mobilePulse.models.User;

public class UsersResource extends ServerResource {
    private static final Logger log = Logger.getLogger(UsersResource.class.getName());
    private String id;

    @Override
    protected void doInit() throws ResourceException {
        log.info("UserResource in doInit");
        id = (String) getRequest().getAttributes().get("id");
    }

    // Handles 'Get User Info API'
    // Handles 'Get List of Users API
    @Get("json")
    public JsonRepresentation get(Variant variant) {
        if (id != null) {
            // Get User Info API
        	log.info("in Get User Info API");
        	return show();
        } else {
            // Get List of Feedback API
        	log.info("Get List of Users API");
        	return index();
        }
    }

    // Handles 'Create User API'
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("in post");
        return save_user(entity);
    }

    // Handles 'Update User API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
		if (this.id == null || this.id.length() == 0) {
			return Utility.apiError(ApiStatusCode.USER_ID_REQUIRED);
		}
        return save_user(entity);
    }

    @Delete("json")
    public JsonRepresentation delete() {
        log.info("in delete");
        EntityManager em = EMF.get().createEntityManager();
        try {
			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(ApiStatusCode.USER_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(ApiStatusCode.USER_NOT_FOUND);
			}
            em.getTransaction().begin();
            User user = (User) em.createNamedQuery("User.getByKey").setParameter("key", key).getSingleResult();
            em.remove(user);
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        return null;
    }
    
    private JsonRepresentation index() {
        log.info("in index");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
        try {
            List<User> users = new ArrayList<User>();
            JSONArray ja = new JSONArray();
            users = (List<User>) em.createNamedQuery("User.getAll").getResultList();
            for (User user : users) {
                ja.put(getUserJson(user));
            }
            json.put("users", ja);
            json.put("apiStatus", apiStatus);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
        	e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return new JsonRepresentation(json);
    }

    private JsonRepresentation show() {
        log.info("in show()");
        EntityManager em = EMF.get().createEntityManager();

		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
		User user = null;
		try {
			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(ApiStatusCode.USER_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(ApiStatusCode.USER_NOT_FOUND);
			}
    		user = (User)em.createNamedQuery("User.getByKey")
				.setParameter("key", key)
				.getSingleResult();
		} catch (NoResultException e) {
			log.info("User not found");
			apiStatus = ApiStatusCode.USER_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} 
        
        return new JsonRepresentation(getUserJson(user, apiStatus));
    }

    private JsonRepresentation save_user(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();

        User user = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
        em.getTransaction().begin();
        try {
            user = new User();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            Boolean isUpdate = false;
            if (id != null) {
                Key key = KeyFactory.stringToKey(this.id);
                user = (User) em.createNamedQuery("User.getByKey").setParameter("key", key).getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            }
            
            // firstName can only be set on create
            if (!isUpdate && json.has("firstName")) {
                user.setFirstName(json.getString("firstName"));
            }
            
            // lastName can only be set on create
            if (!isUpdate && json.has("lastName")) {
                user.setLastName(json.getString("lastName"));
            }
            
            // emailAddress can only be set on create
            // TODO add email validation
            if (!isUpdate && json.has("emailAddress")) {
                user.setEmailAddress(json.getString("emailAddress"));
            }
            
            // TODO strip special characters out of phone number
            // phoneNumber can only be set on create
            if (!isUpdate && json.has("phoneNumber")) {
                user.setPhoneNumber(json.getString("phoneNumber"));
            }
            
            // mobileCarrier only relevant if phoneNumber has been provided -- otherwise ignore.
            // mobileCarrier can only be set on create
            if (!isUpdate && json.has("mobileCarrierId") && user.getPhoneNumber() != null && user.getPhoneNumber().length() > 0) {
            	String carrierDomainName = MobileCarrier.findEmailDomainName(json.getString("mobileCarrierId"));
            	if(carrierDomainName == null) {
            		return Utility.apiError(ApiStatusCode.INVALID_MOBILE_CARRIER_PARAMETER);
            	}
            	String smsEmailAddress = user.getPhoneNumber() + carrierDomainName;
            	user.setSmsEmailAddress(smsEmailAddress);
            }
            
            // sendEmailNotifications can be set on create or update
            if (json.has("sendEmailNotifications")) {
                user.setSendEmailNotifications(json.getBoolean("sendEmailNotifications"));
            }
            
            // sendSmsNotifications can be set on create or update
            if (json.has("sendSmsNotifications")) {
                user.setSendSmsNotifications(json.getBoolean("sendSmsNotifications"));
            }
            
            em.persist(user);
            em.getTransaction().commit();
        } catch (IOException e) {
            log.severe("error extracting JSON object from Post. exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (NoResultException e) {
			log.info("User not found");
			apiStatus = ApiStatusCode.USER_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        
        return new JsonRepresentation(getUserJson(user, apiStatus));
    }
    
    private JSONObject getUserJson(User user) {
    	return getUserJson(user, null);
    }

    private JSONObject getUserJson(User user, String theApiStatus) {
        JSONObject json = new JSONObject();

        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(user != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
                json.put("id", KeyFactory.keyToString(user.getKey()));
                json.put("firstName", user.getFirstName());
                json.put("lastName", user.getLastName());
                json.put("emailAddress", user.getEmailAddress());
                json.put("phoneNumber", user.getPhoneNumber());
                json.put("sendEmailNotifications", user.getSendEmailNotifications());
                json.put("sendSmsNotifications", user.getSendSmsNotifications());
                
            	log.info("User JSON object = " + user.toString());
        	}
        } catch (JSONException e) {
        	log.severe("UsersResrouce::getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
}
