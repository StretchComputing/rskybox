package com.stretchcom.rskybox.server;

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
import org.restlet.Request;
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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.stretchcom.rskybox.models.Application;
import com.stretchcom.rskybox.models.MobileCarrier;
import com.stretchcom.rskybox.models.User;
import com.stretchcom.rskybox.server.TF;

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
    // Handles 'Request Confirmation Code API'
    @Post("json")
    public JsonRepresentation post(Representation entity) {
//    	UserService userService = UserServiceFactory.getUserService();
//    	com.google.appengine.api.users.User currentGoogleUser = userService.getCurrentUser();
        log.info("in post");
    	if(this.id != null && this.id.equalsIgnoreCase("requestConfirmation")) {
    		// Request Confirmation Code API
    		return send_confirmation_code(entity);
    	} else {
    		// Create User API
            return save_user(entity);
    	}
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

    // Handles 'Delete User API'
    @Delete("json")
    public JsonRepresentation delete() {
        log.info("in delete");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
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
        
        try {
			json.put("apiStatus", apiStatus);
		} catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
        	e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
        return new JsonRepresentation(json);
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
        Boolean isSuperAdmin = null;

		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
		User user = null;
		try {
			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(ApiStatusCode.USER_ID_REQUIRED);
			}
			
			if(this.id.equalsIgnoreCase(User.CURRENT)) {
				// special case: id = "current" so return info on currently logged in user
				User currentUser = Utility.getCurrentUser(getRequest());
	        	if(currentUser == null) {
	        		return Utility.apiError(ApiStatusCode.USER_NOT_FOUND);
	        	}
	        	isSuperAdmin = currentUser.getIsSuperAdmin();
	        	
	        	String emailAddress = currentUser.getEmailAddress().toLowerCase();
	    		try {
	    			// TODO - once authentication Filter sets currentUser from User entity, this user lookup won't be needed
					user = (User)em.createNamedQuery("User.getByEmailAddress")
						.setParameter("emailAddress", emailAddress)
						.getSingleResult();
				} catch (NoResultException e) {
					// TODO this user auto create needs to be moved elsewhere
//					// if user is Admin, create a user object on the fly.  This allows admins of the app to just start using rskybox without
//					// any configuration necessary. A slick little feature.
//					if(isSuperAdmin) {
//						user = User.createUser(emailAddress, currentUser.getNickname());
//						if(user != null) {
//							log.info("new user created on the fly for the admin. New user email address = " + emailAddress);
//						} else {
//							log.info("create new user failed");
//							apiStatus = ApiStatusCode.USER_NOT_FOUND;
//						}
//					} else {
//						log.info("User not found");
//						apiStatus = ApiStatusCode.USER_NOT_FOUND;
//					}
				}
			} else {
				// id of user specified
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
			}
		} catch (NoResultException e) {
			log.info("User not found");
			apiStatus = ApiStatusCode.USER_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} 
        
        return new JsonRepresentation(getUserJson(user, apiStatus, isSuperAdmin));
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
            String token = null;
            String authHeader = null;
            if (id != null) {
            	// this is an Update User API call
                Key key = KeyFactory.stringToKey(this.id);
                user = (User) em.createNamedQuery("User.getByKey").setParameter("key", key).getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            } else {
            	// this is a Create User API call            	
                String confirmationCode = null;
                String emailAddress = null;
                String phoneNumber = null;
                
            	if(json.has("confirmationCode")) {
                    confirmationCode = json.getString("confirmationCode");
            	} else {
            		return Utility.apiError(ApiStatusCode.CONFIRMATION_CODE_IS_REQUIRED);
            	}
                
                // TODO add email validation
                if (json.has("emailAddress")) {
                	emailAddress = json.getString("emailAddress").toLowerCase();
                }
                
                if (json.has("phoneNumber")) {
                	phoneNumber = json.getString("phoneNumber");
                	phoneNumber = Utility.extractAllDigits(phoneNumber);
                }
                
                if(emailAddress != null && phoneNumber != null) {
            		return Utility.apiError(ApiStatusCode.EMAIL_ADDRESS_PHONE_NUMBER_MUTUALLY_EXCLUSIVE);
                }
                
                if(emailAddress == null && phoneNumber == null) {
            		return Utility.apiError(ApiStatusCode.EMAIL_ADDRESS_OR_PHONE_NUMBER_IS_REQUIRED);
                }
                
                String storedConfirmationCode = null;
                if(emailAddress != null) {
                	user = User.getUser(em, emailAddress);
                	if(user == null) {
                		return Utility.apiError(ApiStatusCode.USER_NOT_SENT_EMAIL_ADDRESS_CONFIRMATION);
                	}
                	if(user.getEmailConfirmationCode() == null) {
                		return Utility.apiError(ApiStatusCode.USER_NOT_SENT_EMAIL_ADDRESS_CONFIRMATION);
                	} else {
                		storedConfirmationCode = user.getEmailConfirmationCode();
                		user.setIsEmailConfirmed(true);
                	}
                 } else {
                	 user = User.getUserWithPhoneNumber(em, phoneNumber);
                	if(user == null) {
                		return Utility.apiError(ApiStatusCode.USER_NOT_SENT_PHONE_NUMBER_CONFIRMATION);
                	}
                	if(user.getSmsConfirmationCode() == null) {
                		return Utility.apiError(ApiStatusCode.USER_NOT_SENT_PHONE_NUMBER_CONFIRMATION);
                	} else {
                		storedConfirmationCode = user.getSmsConfirmationCode();
                		user.setIsSmsConfirmed(true);
                	}
                }
                
                if(!storedConfirmationCode.equals(confirmationCode)) {
            		return Utility.apiError(ApiStatusCode.INVALID_CONFIRMATION_CODE);
                }

            	token = TF.get();
            	user.setToken(token);

                // format: Basic rSkyboxLogin:<token_value> where rSkyboxLogin:<token_value> portion is base64 encoded
            	String phrase = "rSkyboxLogin:" + token;
            	String phraseBase64 = Base64.encode(phrase.getBytes("ISO-8859-1"));
            	authHeader = "Basic " + phraseBase64;
            	user.setAuthHeader(authHeader);
            }
            
            if (json.has("firstName")) {
                user.setFirstName(json.getString("firstName"));
            }
            
            if (json.has("lastName")) {
                user.setLastName(json.getString("lastName"));
            }
            
            // mobileCarrier only relevant if phoneNumber has been provided -- otherwise ignore.
            if (json.has("mobileCarrierId") && user.getPhoneNumber() != null && user.getPhoneNumber().length() > 0) {
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
    
    private Boolean isEmailAddressAlreadyUsed(User theUserBeingUpdated, String theEmailAddress, Boolean theIsUpdate) {
    	Boolean isEmailAddressAlreadyUsed = false;
    	List<User> usersWithEmailAddress = User.getUsersWithEmailAddress(theEmailAddress);
    	
    	if(usersWithEmailAddress != null && usersWithEmailAddress.size() > 0) {
        	if(!theIsUpdate) {
        		// a new user is being created. If anyone using this email address, then it is used.
        		log.info("new user being created and another user is already using email address = " + theEmailAddress);
        		isEmailAddressAlreadyUsed = true;
        	} else {
        		// an existing user is being updated. Only one user can have the email address -- the user being updated
        		if(usersWithEmailAddress.size() > 1) {
        			// this should not happen, but clearly the email address in in use
            		log.info("user being updated, but at least 2 other users are already using email address = " + theEmailAddress);
        			isEmailAddressAlreadyUsed = true;
        		} else {
        			// if we are here, there is exactly one user using this email address. If it is not the user being modified then the address is in use
        			Key userBeingUpdatedKey = theUserBeingUpdated.getKey();
        			Key userWithMatchingEmailKey = usersWithEmailAddress.get(0).getKey();
        			if(!userBeingUpdatedKey.equals(userWithMatchingEmailKey)) {
                		log.info("user being updated, and a user other than the one being modified is already using email address = " + theEmailAddress);
        				isEmailAddressAlreadyUsed = true;
        			}
        		}
        	}
    	}
    	
    	return isEmailAddressAlreadyUsed;
    }
    
    private JSONObject getUserJson(User user) {
    	return getUserJson(user, null);
    }
    
    private JSONObject getUserJson(User user, String theApiStatus) {
    	return getUserJson(user, theApiStatus, null);
    }

    private JSONObject getUserJson(User user, String theApiStatus, Boolean isCurrentUserAdmin) {
        JSONObject json = new JSONObject();

        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(user != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
                json.put("id", KeyFactory.keyToString(user.getKey()));
                json.put("firstName", user.getFirstName());
                json.put("lastName", user.getLastName());
                json.put("phoneNumber", user.getPhoneNumber());
                json.put("emailAddress", user.getEmailAddress());
                json.put("sendEmailNotifications", user.getSendEmailNotifications());
                json.put("sendSmsNotifications", user.getSendSmsNotifications());
                json.put("token", user.getToken());
                json.put("authHeader", user.getAuthHeader());

                if(isCurrentUserAdmin != null) {
                	///////////////////////////////////////////////////////////////////
                	// must be Current user; otherwise isCurrentUserAdmin would be null
                	///////////////////////////////////////////////////////////////////
                	json.put("isSuperAdmin", isCurrentUserAdmin);
                	
    	        	UserService userService = UserServiceFactory.getUserService();
    	        	json.put("logoutUrl", userService.createLogoutURL(RskyboxApplication.APPLICATION_WELCOME_PAGE));
    	        	
    	        	List<Application> applications = user.getApplications();
    	        	if(applications != null && applications.size() > 0) {
    	                JSONArray ja = new JSONArray();
    	        		for(Application app : applications) {
    	        			JSONObject jo = new JSONObject();
    	        			jo.put("id", KeyFactory.keyToString(app.getKey()));
    	        			jo.put("name", app.getName());
    	        			ja.put(jo);
    	        		}
    	                json.put("applications", ja);
    	        	}
                }
                
                if(user.getSmsEmailAddress() != null && user.getSmsEmailAddress().length() > 0) {
                	String emailDomainName = Utility.getEmailDomainNameFromSmsEmailAddress(user.getSmsEmailAddress());
                	MobileCarrier mobileCarrier = MobileCarrier.findMobileCarrier(emailDomainName);
                	if(mobileCarrier != null) {
                        json.put("mobileCarrierName", mobileCarrier.getName());
                        json.put("mobileCarrierId", mobileCarrier.getCode());
                	}
                }
        	}
        } catch (JSONException e) {
        	log.severe("getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
    
    private JsonRepresentation send_confirmation_code(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();
    
        User user = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
        em.getTransaction().begin();
        try {
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            String emailAddress = null;
            String phoneNumber = null;
            String mobileCarrierId = null;
            String carrierDomainName = null;
            
            // TODO add email validation
            if (json.has("emailAddress")) {
            	emailAddress = json.getString("emailAddress").toLowerCase();
            }
            
            if (json.has("phoneNumber")) {
            	phoneNumber = json.getString("phoneNumber");
            	phoneNumber = Utility.extractAllDigits(phoneNumber);
            }
            
            // mobileCarrier only relevant if phoneNumber has been provided -- otherwise ignore.
            if (json.has("mobileCarrierId")) {
            	mobileCarrierId = json.getString("mobileCarrierId");
            	carrierDomainName = MobileCarrier.findEmailDomainName(mobileCarrierId);
            	if(carrierDomainName == null) {
            		return Utility.apiError(ApiStatusCode.INVALID_MOBILE_CARRIER_PARAMETER);
            	}
            }
            
            if(emailAddress == null && phoneNumber == null) {
            	return Utility.apiError(ApiStatusCode.EITHER_EMAIL_ADDRESS_OR_PHONE_NUMBER_IS_REQUIRED);
            }
            
            if(phoneNumber != null && mobileCarrierId == null) {
            	return Utility.apiError(ApiStatusCode.PHONE_NUMBER_AND_MOBILE_CARRIER_ID_MUST_BE_SPECIFIED_TOGETHER);
            }
            
            String confirmationCode = TF.getConfirmationCode();
            String confirmationMessage = "your confirmation code is " + confirmationCode;
            String subject = "rSkybox confirmation code";
            if(emailAddress != null) {
            	// check if user with this email address already exists
            	user = User.getUser(em, emailAddress);
            	if(user != null) {
            		log.info("user found with emailAddress = " + emailAddress);
            		if(user.getIsEmailConfirmed()) {
                    	return Utility.apiError(ApiStatusCode.USER_ALREADY_HAS_CONFIRMED_EMAIL_ADDRESS);
            		}
            	} else {
                    // if there is no existing user, create one now
            		log.info("creating new user");
                    user = new User();
                    user.setEmailAddress(emailAddress);
            	}
                user.setEmailConfirmationCode(confirmationCode);
            	log.info("sending email confirmation code = " + confirmationCode + " to " + user.getEmailAddress());
            	Emailer.send(user.getEmailAddress(), subject, confirmationMessage, Emailer.NO_REPLY);
            } else {
            	// check if user with this phone number already exists
            	user = User.getUserWithPhoneNumber(em, phoneNumber);
            	if(user != null) {
            		log.info("user found with phoneNumber = " + phoneNumber);
            		if(user.getIsSmsConfirmed()) {
                    	return Utility.apiError(ApiStatusCode.USER_ALREADY_HAS_CONFIRMED_PHONE_NUMBER);
            		}
            	} else {
                    // if there is no existing user, create one now
            		log.info("creating new user");
                    user = new User();
                    user.setPhoneNumber(phoneNumber);
                	String smsEmailAddress = user.getPhoneNumber() + carrierDomainName;
                	user.setSmsEmailAddress(smsEmailAddress);
            	}
            	user.setSmsConfirmationCode(confirmationCode);
            	log.info("sending SMS confirmation code = " + confirmationCode + " to " + user.getSmsEmailAddress());
                Emailer.send(user.getSmsEmailAddress(), subject, confirmationMessage, Emailer.NO_REPLY);
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
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        
        return new JsonRepresentation(getUserJson(user, apiStatus));
    }
}
