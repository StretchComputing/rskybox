package com.stretchcom.rskybox.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
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
import org.restlet.util.Series;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.stretchcom.rskybox.models.AppMember;
import com.stretchcom.rskybox.models.Application;
import com.stretchcom.rskybox.models.MobileCarrier;
import com.stretchcom.rskybox.models.User;

public class UsersResource extends ServerResource {
    private static final Logger log = Logger.getLogger(UsersResource.class.getName());
    private String id;
    private String userName;
    private String password;

    @Override
    protected void doInit() throws ResourceException {
        log.info("UserResource in doInit");
        id = (String) getRequest().getAttributes().get("id");
        
		Form form = getRequest().getResourceRef().getQueryAsForm();
		for (Parameter parameter : form) {
			log.info("parameter " + parameter.getName() + " = " + parameter.getValue());
			if(parameter.getName().equals("userName"))  {
				this.userName = (String)parameter.getValue().toLowerCase();
				this.userName = Reference.decode(this.userName);
				log.info("UsersResource() - decoded userName = " + this.userName);
			} else if(parameter.getName().equals("password"))  {
				this.password = (String)parameter.getValue();
				this.password = Reference.decode(this.password);
				log.info("UsersResource() - decoded password = " + this.password);
			}
		}
    }

    // Handles 'Get User Info API'
    // Handles 'Get List of Users API
    // Handles 'Get Token API'
    @Get("json")
    public JsonRepresentation get(Variant variant) {
        if (id != null) {
        	if(this.id.equalsIgnoreCase("token")) {
        		// Get Token API
        		return getUserToken();
        	} else {
                // Get User Info API
            	log.info("in Get User Info API");
            	return show();
        	}
        } else {
            // Get List of Users API
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
    // Handles 'Confirm User API'
    // Handles 'Clear Cookie API'
   @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
		if (this.id == null || this.id.length() == 0) {
			return Utility.apiError(this, ApiStatusCode.USER_ID_REQUIRED);
		}
		
		if(id.equalsIgnoreCase("confirm")) {
			// Confirm User API
			return confirm_user(entity);
		} else if(id.equalsIgnoreCase("clearCookie")) {
			return clear_cookie(entity);
		} else {
			// Update User API
	        return save_user(entity);
		}
    }

    // Handles 'Delete User API'
    @Delete("json")
    public JsonRepresentation delete() {
        log.info("in delete");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
        em.getTransaction().begin();
        try {
			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(this, ApiStatusCode.USER_ID_REQUIRED);
			}
			
			User currentUser = Utility.getCurrentUser(getRequest());
            Key key;
			if(this.id.equalsIgnoreCase(User.CURRENT)) {
				// special case: id = "current" so return info on currently logged in user
	        	if(currentUser == null) {
	        		return Utility.apiError(this, ApiStatusCode.USER_NOT_FOUND);
	        	}
	        	key = currentUser.getKey();
			} else {
	        	//////////////////////
	        	// Authorization Rules
	        	//////////////////////
				// if not the current user, then must be the Super Admin
	        	if(currentUser == null || !currentUser.getIsSuperAdmin()) {
	            	return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED);
	        	}

	        	// id of user specified
				try {
					key = KeyFactory.stringToKey(this.id);
				} catch (Exception e) {
					log.info("ID provided cannot be converted to a Key");
					return Utility.apiError(this, ApiStatusCode.USER_NOT_FOUND);
				}
			}
    		User user = (User)em.createNamedQuery("User.getByKey")
				.setParameter("key", key)
				.getSingleResult();
            
            em.remove(user);
            em.getTransaction().commit();
        } catch (NoResultException e) {
        	return Utility.apiError(this, ApiStatusCode.USER_NOT_FOUND);
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
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	User currentUser = Utility.getCurrentUser(getRequest());
        	if(currentUser == null || !currentUser.getIsSuperAdmin()) {
            	return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED);
        	}
        	
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
        Boolean isCurrentUser = false;

		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
		User user = null;
		try {
			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(this, ApiStatusCode.USER_ID_REQUIRED);
			}
			
        	User currentUser = Utility.getCurrentUser(getRequest());
			if(this.id.equalsIgnoreCase(User.CURRENT)) {
				isCurrentUser = false;
				// special case: id = "current" so return info on currently logged in user
	        	if(currentUser == null) {
	        		return Utility.apiError(this, ApiStatusCode.USER_NOT_FOUND);
	        	}
	        	user = currentUser;
			} else {
	        	//////////////////////
	        	// Authorization Rules
	        	//////////////////////
	        	if(currentUser == null || !currentUser.getIsSuperAdmin()) {
	            	return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED);
	        	}

	        	// id of user specified
	            Key key;
				try {
					key = KeyFactory.stringToKey(this.id);
				} catch (Exception e) {
					log.info("ID provided cannot be converted to a Key");
					return Utility.apiError(this, ApiStatusCode.USER_NOT_FOUND);
				}
				
	    		user = (User)em.createNamedQuery("User.getByKey")
					.setParameter("key", key)
					.getSingleResult();
			}
		} catch (NoResultException e) {
			return Utility.apiError(this, ApiStatusCode.USER_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} 
        
        return new JsonRepresentation(getUserJson(user, apiStatus, isCurrentUser));
    }

    private JsonRepresentation save_user(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();

        User user = null;
        User userCache = new User();
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
        em.getTransaction().begin();
        try {
            user = new User();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            Boolean isUpdate = false;
            String token = null;
            String authHeader = null;
            String carrierDomainName = null;
            
            extractUserInfoFromJson(userCache, json);

            if (id != null) {
            	//////////////////////////////////
            	// this is an Update User API call
            	//////////////////////////////////
                isUpdate = true;
        		this.setStatus(Status.SUCCESS_OK);
            	
	            Key key;
				User currentUser = Utility.getCurrentUser(getRequest());
    			if(this.id.equalsIgnoreCase(User.CURRENT) || this.id.equalsIgnoreCase(KeyFactory.keyToString(currentUser.getKey()))) {
    	        	key = currentUser.getKey();
    			} else {
    	        	//////////////////////
    	        	// Authorization Rules
    	        	//////////////////////
    				// if not the current user, then must be the Super Admin
    	        	if(currentUser == null || currentUser.getIsSuperAdmin() == null || !currentUser.getIsSuperAdmin()) {
    	            	return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED);
    	        	}

    	        	// id of user specified
    				try {
    					key = KeyFactory.stringToKey(this.id);
    				} catch (Exception e) {
    					log.info("ID provided cannot be converted to a Key");
    					return Utility.apiError(this, ApiStatusCode.USER_NOT_FOUND);
    				}
    			}
    			
    			// find the user entity specified by the user ID in the Update API call
	    		user = (User)em.createNamedQuery("User.getByKey")
					.setParameter("key", key)
					.getSingleResult();
                
	        	User owningUser = findUser(userCache, em);  // used only to verify that 'user' owns EA/PN if specified
	            updateValidation(user, userCache, owningUser);

                /////////////////////////////
                // Email Address Update Rules
                /////////////////////////////
                //  1. If the email address has been confirmed, it cannot be updated
                //  2. If a new email address is specified, it cannot already be used by another user (verified by validation method call above)
                //  3. If an email confirmation code is specified, a confirmation will be attempted.
	            //  4. If no email confirmation code is specified, a confirmation code will be sent.
                if(userCache.getEmailAddress() != null) {
                    if(user.getIsEmailConfirmed() == null || !user.getIsEmailConfirmed()) {
                    	if(userCache.getEmailConfirmationCode() != null) {
                        	confirmEmailAddress(userCache, user);
                    	} else {
                        	sendEmailAddressConfirmCode(userCache, user);
                    	}
                    } else {
                    	// email has already been confirmed
                		return Utility.apiError(this, ApiStatusCode.EMAIL_ADDRESS_CAN_NO_LONGER_BE_MODIFIED);
                    }
                }

                ////////////////////////////
                // Phone Number Update Rules
                ////////////////////////////
                //  1. If the phone number has been confirmed, it cannot be updated
                //  2. To update, there has to be a carrier ID - either sent as part of the update or already in the user entity (checked by validation method)
                //  3. If a new phone number is specified, it cannot already be used by another user (checked by validation method)
                //  4. If a phone number confirmation code is specified, a confirmation will be attempted.
                //  5. If no phone number confirmation code is specified, a confirmation code will be sent. 
                if(userCache.getPhoneNumber() != null) {
                    if(user.getIsSmsConfirmed() == null || !user.getIsSmsConfirmed()) {
                    	if(userCache.getSmsConfirmationCode() != null) {
                        	confirmPhoneNumber(userCache, user);
                    	} else {
                        	sendPhoneNumberConfirmCode(userCache, user);
                    	}
                    } else {
                    	// phone number has already been confirmed
                		return Utility.apiError(this, ApiStatusCode.PHONE_NUMBER_CAN_NO_LONGER_BE_MODIFIED);
                    }
                }
            } else {
            	/////////////////////////////////
            	// this is a Create User API call  
            	/////////////////////////////////
	        	user = findUser(userCache, em);  
	            createValidation(user, userCache);
                
                if(userCache.getEmailAddress() != null) {
                	if(userCache.getEmailConfirmationCode() != null) {
                    	confirmEmailAddress(userCache, user);
                	} else {
                    	sendEmailAddressConfirmCode(userCache, user);
                	}
                 }
                
                if(userCache.getPhoneNumber() != null) {
                	if(userCache.getSmsConfirmationCode() != null) {
                    	confirmPhoneNumber(userCache, user);
                	} else {
                    	sendPhoneNumberConfirmCode(userCache, user);
                	}
                }

            	token = TF.get();
            	user.setToken(token);

                // format: Basic rSkyboxLogin:<token_value> where rSkyboxLogin:<token_value> portion is base64 encoded
            	String phrase = "rSkyboxLogin:" + token;
            	String phraseBase64 = Base64.encodeBase64String(phrase.getBytes("ISO-8859-1"));
            	authHeader = "Basic " + phraseBase64;
            	user.setAuthHeader(authHeader);
            }
            
			if(json.has("password")) {
				String plainTextPassword = json.getString("password");
				if(plainTextPassword.length() < User.MINIMUM_PASSWORD_SIZE) {
	        		return Utility.apiError(this, ApiStatusCode.PASSWORD_TOO_SHORT);
				}
				String encryptedPassword = Utility.encrypt(plainTextPassword);
				log.info("encryptedPassword = " + encryptedPassword);
				if(encryptedPassword == null) {
					this.setStatus(Status.SERVER_ERROR_INTERNAL);
				} else {
					user.setPassword(encryptedPassword);
				}
			} else {
				if(!isUpdate) {
	        		return Utility.apiError(this, ApiStatusCode.PASSWORD_IS_REQUIRED);
				}
			}
           
            if (json.has("firstName")) {
                user.setFirstName(json.getString("firstName"));
            }
            
            if (json.has("lastName")) {
                user.setLastName(json.getString("lastName"));
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
            
            if(!isUpdate) {
            	// update pending membership if appropriate
            	Boolean membershipConfirmed = AppMember.confirmMember(user);
            	
            	// change being made to user is only for calling getUserJson() below - it's a transient field that never gets persisted anyway
            	user.setWasMembershipConfirmed(membershipConfirmed);
            }
        } catch (ApiException e) {
        	return Utility.apiError(this, e.getMessage());
        } catch (IOException e) {
            log.severe("error extracting JSON object from Post. exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (NoResultException e) {
        	return Utility.apiError(this, ApiStatusCode.USER_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    	
    	// need request to set cookie
        //setTokenCookie(user.getToken());
        
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

    private JSONObject getUserJson(User user, String theApiStatus, Boolean isCurrentUser) {
        JSONObject json = new JSONObject();
        if(isCurrentUser == null) {isCurrentUser= false;}

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
                json.put("memberConfirmed", user.getWasMembershipConfirmed());
                json.put("isEmailConfirmed", user.getIsEmailConfirmed());
                json.put("isSmsConfirmed", user.getIsSmsConfirmed());
            	json.put("isSuperAdmin", user.getIsSuperAdmin());
            	
                // token and authHeader only returned if user is either email or SMS confirmed
            	if(user.getIsEmailConfirmed() || user.getIsSmsConfirmed()) {
            		json.put("token", user.getToken());
                    json.put("authHeader", user.getAuthHeader());
            	}

                 if(isCurrentUser) {
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
        User userCache = new User();
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
        em.getTransaction().begin();
        JSONObject jsonReturn = new JSONObject();
        try {
        	JSONObject json = new JsonRepresentation(entity).getJsonObject();
        	
            extractUserInfoFromJson(userCache, json);
            sendConfirmCodeValidation(userCache);
        	user = findUser(userCache, em);
        	sendEmailAddressConfirmCode(userCache, user);
        	sendPhoneNumberConfirmCode(userCache, user);
            
            // emailAddress sent in JSON Return if email confirmation sent. phoneNumber sent in JSON Return if phoneNumber confirmation sent.
            if(user.getEmailAddressConfirmationSent()) {
            	jsonReturn.put("emailAddress", user.getEmailAddress());
            }
            if(user.getPhoneNumberConfirmationSent()) {
            	jsonReturn.put("phoneNumber", user.getPhoneNumber());
            }
            log.info("********** send_confirmation_code(): about to persist user with email = " + user.getEmailAddress() + " and phone = " +user.getPhoneNumber());
            em.persist(user);
            em.getTransaction().commit();
        } catch (ApiException e) {
        	return Utility.apiError(this, e.getMessage());
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
        
        try {
        	jsonReturn.put("apiStatus", apiStatus);
		} catch (JSONException e) {
			e.printStackTrace();
		}
        return new JsonRepresentation(jsonReturn);
    }
    
    public JsonRepresentation getUserToken() {
        JSONObject jsonReturn = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();

        if(this.userName == null) {
    		return Utility.apiError(this, ApiStatusCode.USER_NAME_IS_REQUIRED);
        } else if(this.password == null) {
    		return Utility.apiError(this, ApiStatusCode.PASSWORD_IS_REQUIRED);
        }
        
        // won't really be able to detect if the username is a bad phone number or not
        Boolean isUserNamePhoneNumber = Utility.isPhoneNumber(this.userName);
        User user = null;
        String encryptedPassword = Utility.encrypt(this.password);
        if(isUserNamePhoneNumber) {
        	user = User.getUserWithPhoneNumber(em, this.userName, encryptedPassword);
        } else {
        	user = User.getUser(em, this.userName, encryptedPassword);
        }
        
        if(user == null) {
    		return Utility.apiError(this, ApiStatusCode.INVALID_USER_CREDENTIALS);
        }
        
        try {
            jsonReturn.put("apiStatus", ApiStatusCode.SUCCESS);
            jsonReturn.put("token", user.getToken());
            jsonReturn.put("authHeader", user.getAuthHeader());
        } catch (JSONException e) {
        	log.severe("getUserToken() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    	
    	// need request to set cookie
        //setTokenCookie(user.getToken());
    	
        return new JsonRepresentation(jsonReturn);
    }
    
    private JsonRepresentation confirm_user(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();
        JSONObject jsonReturn = new JSONObject();

        User user = null;
        User userCache = new User();
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
        em.getTransaction().begin();
        try {
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            
            extractUserInfoFromJson(userCache, json);
            confirmValidation(userCache);
        	user = findUser(userCache, em);
        	confirmEmailAddress(userCache, user);
        	confirmPhoneNumber(userCache, user);
            
            em.persist(user);
            em.getTransaction().commit();
        } catch (ApiException e) {
        	return Utility.apiError(this, e.getMessage());
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
        
        try {
        	jsonReturn.put("apiStatus", apiStatus);
        	
        	// return the token if the user is confirmed
        	if(user.getIsEmailConfirmed() || user.getIsSmsConfirmed()) jsonReturn.put("token", user.getToken());
		} catch (JSONException e) {
			e.printStackTrace();
		}
        
        return new JsonRepresentation(jsonReturn);
    }
    
    private JsonRepresentation clear_cookie(Representation entity) {
		String apiStatus = ApiStatusCode.SUCCESS;
        JSONObject jsonReturn = new JSONObject();
        this.setStatus(Status.SUCCESS_OK);
        //setTokenCookie(null);
        try {
        	jsonReturn.put("apiStatus", apiStatus);
		} catch (JSONException e) {
			e.printStackTrace();
		}
        return new JsonRepresentation(jsonReturn);
    }
    
    // theTokenValue of null means to clear cookie immediately
    public void setTokenCookie(String theTokenValue){
    	String tokenValue = theTokenValue == null ? "" : theTokenValue;
        CookieSetting cs = new CookieSetting(1, "token", tokenValue, "/", "");

        if(theTokenValue == null) {
            // zero value times the cookie out immediately
            cs.setMaxAge(0);
        } else {
            // set cookie age to one year specified in seconds
            cs.setMaxAge(31557600);
        }

        Series<CookieSetting> cookieSettings = this.getResponse().getCookieSettings();
        cookieSettings.add(cs);
        
        // the last thing we do is log out all the current cookies for debugging purposes
        for(CookieSetting cookSet : cookieSettings) {
        	log.info("(cookie) name:"+cookSet.getName()+" value:"+cookSet.getValue()+" domain:"+cookSet.getDomain()+" path:"+cookSet.getPath()+
        			 " version:"+cookSet.getVersion()+" maxAge:"+cookSet.getMaxAge());
        }
    }
    
    private void sendUserConfirmationEmail(User theUser, String theSubject) {
        StringBuffer coreMsg = new StringBuffer();
        coreMsg.append("Your confirmation code is " + theUser.getEmailConfirmationCode());
    	
    	StringBuffer urlBuf = new StringBuffer();
    	urlBuf.append(RskyboxApplication.USER_VERIFICATION_PAGE);
    	urlBuf.append("?");
    	urlBuf.append("emailAddress=");
    	urlBuf.append(Utility.urlEncode(theUser.getEmailAddress()));
    	urlBuf.append("&");
    	urlBuf.append("emailConfirmationCode=");
    	urlBuf.append(Utility.urlEncode(theUser.getEmailConfirmationCode()));
    	urlBuf.append("&");
    	urlBuf.append("preregistration=");
        if(theUser.getToken() == null) {
        	urlBuf.append("true");
        } else {
        	urlBuf.append("false");
        }
        
        String body = Emailer.getUserConfirmationEmailBody(coreMsg.toString(), urlBuf.toString(), theUser.getEmailConfirmationCode());
    	Emailer.send(theUser.getEmailAddress(), theSubject, body, Emailer.NO_REPLY);
    }
    
    private String buildSmsConfirmationMessage(User theUser) {
    	StringBuffer sb = new StringBuffer("Your confirmation code is " + theUser.getSmsConfirmationCode());
        sb.append(".<br><br>");
        sb.append(RskyboxApplication.USER_VERIFICATION_PAGE);
        sb.append("?");
        sb.append("phoneNumber=");
        sb.append(Utility.urlEncode(theUser.getPhoneNumber()));
        sb.append("&");
        sb.append("phoneConfirmationCode=");
        sb.append(Utility.urlEncode(theUser.getSmsConfirmationCode()));
        sb.append("&");
        sb.append("preregistration=");
        if(theUser.getToken() == null) {
            sb.append("true");
        } else {
            sb.append("false");
        }
        return sb.toString();
    }
    
    private void extractUserInfoFromJson(User theUserCache, JSONObject theJson) {
        try {
            // TODO add email validation
            if(theJson.has("emailAddress")) {
            	String emailAddress = theJson.getString("emailAddress").toLowerCase();
            	if (emailAddress.length() <= 0) {
            		emailAddress = null;
            	}
            	theUserCache.setEmailAddress(emailAddress);
            }
            
            if(theJson.has("phoneNumber")) {
            	String phoneNumber = theJson.getString("phoneNumber");
            	phoneNumber = Utility.extractAllDigits(phoneNumber);
            	if (phoneNumber.length() <= 0) {
            		phoneNumber = null;
            	}
            	theUserCache.setPhoneNumber(phoneNumber);
            }
            
        	if(theJson.has("emailConfirmationCode")) {
        		theUserCache.setEmailConfirmationCode(theJson.getString("emailConfirmationCode"));
        	} 
        	
        	if(theJson.has("phoneConfirmationCode")) {
        		theUserCache.setSmsConfirmationCode(theJson.getString("phoneConfirmationCode"));
        	} 
        	
            if (theJson.has("mobileCarrierId")) {
            	theUserCache.setMobileCarrierId(theJson.getString("mobileCarrierId"));
            }
        } catch (JSONException e) {
            log.severe("userInfoFromJson:exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } finally {
        }
        return;
    }
    
    // Returns the single user that "owns" the email address/phone number. If no existing user, a new "owning" user is created and returned.
    // Throws ApiException exception if:
    //  * phone number and email address are associated with two different users
    //  * email address associated with more than one user
    //  * phone number associated with more than one user
    private User findUser(User theUserCache, EntityManager theEm) throws ApiException {
    	User user = null;
    	ApiException apiException = null;
    	
    	try {
            if(theUserCache.getEmailAddress() != null) {
            	user = User.getUser(theEm, theUserCache.getEmailAddress(), null);
            	if(user != null) {
            		log.info("user found with emailAddress = " + theUserCache.getEmailAddress());
                	// there is slight loop hole we have to close. We found the user using the email address, but it is possible there
            		// is a different user that is using the specified phone number (if one was specified).
            		if(theUserCache.getPhoneNumber() != null) {
                		User anotherUser = User.getUserWithPhoneNumber(theEm, theUserCache.getPhoneNumber(), null);
                		if(anotherUser != null && !anotherUser.getKey().equals(user.getKey())) {
                			log.info("user " + anotherUser.getLastName() + " is also using the phone number = " + theUserCache.getPhoneNumber());
                			apiException = new ApiException(ApiStatusCode.EMAIL_ADDRESS_AND_PHONE_NUMBER_MATCH_SEPARATE_USERS);
            				throw apiException;
                		}
            		}
            		return user;
            	}
            }
            
            if(theUserCache.getPhoneNumber() != null) {
            	user = User.getUserWithPhoneNumber(theEm, theUserCache.getPhoneNumber(), null);
            	if(user != null) {
            		log.info("user found with phoneNumber = " + theUserCache.getPhoneNumber());
                	// no loop holes to close here.  If we are here, we know even if an email address was specified, it was not associated with any user.
            		return user;
            	}
            }
            
    		log.info("creating new user");
            user = new User();
    	} catch (NonUniqueResultException e) {
			apiException = new ApiException(ApiStatusCode.SERVER_ERROR);
			throw apiException;
		}
        
        return user;
	}
    
    // Only does confirmation if both the email address/phone number and confirmation code are provided
    //::WHAT_IF:: On Create API, client code let's user change their email address (rksybox client doesn't allow this) while they
    //            are submitting their confirmation code. A EMAIL_ADDRESS_DOES_NOT_MATCH_ORIGINAL error will be returned.
    private void confirmEmailAddress(User theUserCache, User theUser) throws ApiException {
    	ApiException apiException = null;
    	
    	if(theUserCache.getEmailAddress() != null && theUserCache.getEmailConfirmationCode() != null) {
    		if(theUser.getEmailAddress() == null) {
    			apiException = new ApiException(ApiStatusCode.EMAIL_ADDRESS_NOT_FOUND);
				throw apiException;
    		}
    		
    		if(!theUser.getEmailAddress().equalsIgnoreCase(theUserCache.getEmailAddress())) {
    			apiException = new ApiException(ApiStatusCode.EMAIL_ADDRESS_DOES_NOT_MATCH_ORIGINAL);
				throw apiException;
    		}
    		
    		if(theUser.getIsEmailConfirmed() != null && theUser.getIsEmailConfirmed()) {
    			apiException = new ApiException(ApiStatusCode.USER_ALREADY_HAS_CONFIRMED_EMAIL_ADDRESS);
				throw apiException;
        	}
    		
            String storedEmailConfirmationCode = theUser.getEmailConfirmationCode();
    		if(storedEmailConfirmationCode == null) {
    			apiException = new ApiException(ApiStatusCode.USER_EMAIL_ADDRESS_NOT_PENDING_CONFIRMATION);
				throw apiException;
        	}
    		if(storedEmailConfirmationCode != null && !storedEmailConfirmationCode.equals(theUserCache.getEmailConfirmationCode())) {
    			apiException = new ApiException(ApiStatusCode.INVALID_EMAIL_ADDRESS_CONFIRMATION_CODE);
				throw apiException;
            }
    		theUser.setIsEmailConfirmed(true);
    	}
    }
    
    // Only does confirmation if both the email address/phone number and confirmation code are provided
    private void confirmPhoneNumber(User theUserCache, User theUser) throws ApiException {
    	ApiException apiException = null;
        
    	if(theUserCache.getPhoneNumber() != null && theUserCache.getSmsConfirmationCode() != null) {
    		if(theUser.getPhoneNumber() == null) {
    			apiException = new ApiException(ApiStatusCode.PHONE_NUMBER_NOT_FOUND);
				throw apiException;
    		}
    		
    		if(!theUser.getPhoneNumber().equalsIgnoreCase(theUserCache.getPhoneNumber())) {
    			apiException = new ApiException(ApiStatusCode.PHONE_NUMBER_DOES_NOT_MATCH_ORIGINAL);
				throw apiException;
    		}
    		
    		if(theUser.getIsSmsConfirmed() != null && theUser.getIsSmsConfirmed()) {
    			apiException = new ApiException(ApiStatusCode.USER_ALREADY_HAS_CONFIRMED_PHONE_NUMBER);
				throw apiException;
        	}
    		
            String storedSmsConfirmationCode = theUser.getSmsConfirmationCode();
    		if(storedSmsConfirmationCode == null) {
    			apiException = new ApiException(ApiStatusCode.USER_PHONE_NUMBER_NOT_PENDING_CONFIRMATION);
				throw apiException;
        	}
    		if(storedSmsConfirmationCode != null && !storedSmsConfirmationCode.equals(theUserCache.getSmsConfirmationCode())) {
    			apiException = new ApiException(ApiStatusCode.INVALID_PHONE_NUMBER_CONFIRMATION_CODE);
				throw apiException;
            }
    		theUser.setIsSmsConfirmed(true);
    	}
    }
   
    private void confirmValidation(User theUserCache) throws ApiException {
    	ApiException apiException = null;
    	
        // apply business rules to input parameters
    	if(theUserCache.getEmailAddress() == null && theUserCache.getPhoneNumber() == null) {
			apiException = new ApiException(ApiStatusCode.EITHER_EMAIL_ADDRESS_OR_PHONE_NUMBER_IS_REQUIRED);
			throw apiException;
        }
    	
    	if(theUserCache.getEmailAddress() != null && theUserCache.getEmailConfirmationCode() == null) {
			apiException = new ApiException(ApiStatusCode.EMAIL_ADDRESS_CONFIRMATION_CODE_IS_REQUIRED);
			throw apiException;
    	}
    	if(theUserCache.getPhoneNumber() != null && theUserCache.getSmsConfirmationCode() == null) {
			apiException = new ApiException(ApiStatusCode.PHONE_NUMBER_CONFIRMATION_CODE_IS_REQUIRED);
			throw apiException;
    	}
    }
    
    //::SIDE_EFFECT:: On create, user fat fingers email address and enters someone else's email. That someone else is in the process of
    //                signing up for rskybox, but has not confirmed yet. The someone else will receive a second confirmation email with
    //                the same confirmation code. This same side effect is not possible for update API since user entity validation done.
    private void sendEmailAddressConfirmCode(User theUserCache, User theUser) throws ApiException {
    	ApiException apiException = null;
    	
    	if(theUserCache.getEmailAddress() != null && theUserCache.getEmailConfirmationCode() == null) {
    		if(theUser.getIsEmailConfirmed() != null && theUser.getIsEmailConfirmed()) {
    			apiException = new ApiException(ApiStatusCode.USER_ALREADY_HAS_CONFIRMED_EMAIL_ADDRESS);
				throw apiException;
    		}
    		
    		String emailAddressConfirmationCode = null;
    		if(theUser.getEmailAddress() != null && theUser.getEmailAddress().equalsIgnoreCase(theUserCache.getEmailAddress())) {
    			// since email address the same, use the existing confirmation code if it exists
    			emailAddressConfirmationCode = theUser.getEmailConfirmationCode();
    		}
    		if(emailAddressConfirmationCode == null) {
                emailAddressConfirmationCode = TF.getConfirmationCode();
    		}
    		
            String subject = "rSkybox confirmation code";
            theUser.setEmailAddress(theUserCache.getEmailAddress());
            theUser.setSendEmailNotifications(true);
            theUser.setEmailConfirmationCode(emailAddressConfirmationCode);
        	log.info("sending email confirmation code = " + emailAddressConfirmationCode + " to " + theUser.getEmailAddress());
        	sendUserConfirmationEmail(theUser, subject);
        	theUser.setEmailAddressConfirmationSent(true);
    	}
    }
    
    
    private void sendPhoneNumberConfirmCode(User theUserCache, User theUser) throws ApiException {
    	ApiException apiException = null;
    	
    	if(theUserCache.getPhoneNumber() != null && theUserCache.getSmsConfirmationCode() == null) {
    		if(theUser.getIsSmsConfirmed() != null && theUser.getIsSmsConfirmed()) {
    			apiException = new ApiException(ApiStatusCode.USER_ALREADY_HAS_CONFIRMED_PHONE_NUMBER);
				throw apiException;
    		}
    		
    		String phoneNumberConfirmationCode = null;
    		if(theUser.getPhoneNumber() != null && theUser.getPhoneNumber().equalsIgnoreCase(theUserCache.getPhoneNumber())) {
    			// since phone number the same, use the existing confirmation code if it exists
    			phoneNumberConfirmationCode = theUser.getSmsConfirmationCode();
    		}
    		if(phoneNumberConfirmationCode == null) {
    			phoneNumberConfirmationCode = TF.getConfirmationCode();
    		}
    		
    		// for Update API, mobileCarrierId can be passed in or already stored in the user. Precedence given to 'passed in'
    		String mobileCarrierId = theUserCache.getMobileCarrierId();
    		if(mobileCarrierId == null) {
    			mobileCarrierId = theUser.getMobileCarrierId();
    		}
    		
        	String carrierDomainName = MobileCarrier.findEmailDomainName(mobileCarrierId);
        	if(carrierDomainName == null) {
    			apiException = new ApiException(ApiStatusCode.INVALID_MOBILE_CARRIER_PARAMETER);
				throw apiException;
        	}
    		
            String subject = "rSkybox confirmation code";
    		theUser.setPhoneNumber(theUserCache.getPhoneNumber());
        	String smsEmailAddress = theUserCache.getPhoneNumber() + carrierDomainName;
        	theUser.setSmsEmailAddress(smsEmailAddress);
        	theUser.setSendSmsNotifications(true);
            theUser.setSmsConfirmationCode(phoneNumberConfirmationCode);
        	log.info("sending SMS confirmation code = " + phoneNumberConfirmationCode + " to " + theUser.getSmsEmailAddress());
            Emailer.send(theUser.getSmsEmailAddress(), subject, buildSmsConfirmationMessage(theUser), Emailer.NO_REPLY);
            theUser.setPhoneNumberConfirmationSent(true);
    	}
    }
    
    private void sendConfirmCodeValidation(User theUserCache) throws ApiException {
    	ApiException apiException = null;
    	
        if(theUserCache.getEmailAddress() == null && theUserCache.getPhoneNumber() == null) {
			apiException = new ApiException(ApiStatusCode.EITHER_EMAIL_ADDRESS_OR_PHONE_NUMBER_IS_REQUIRED);
			throw apiException;
        }
        
        if(theUserCache.getPhoneNumber() != null && theUserCache.getMobileCarrierId() == null) {
			apiException = new ApiException(ApiStatusCode.PHONE_NUMBER_AND_MOBILE_CARRIER_ID_MUST_BE_SPECIFIED_TOGETHER);
			throw apiException;
        }
    }
    
    private void updateValidation(User theUser, User theUserCache, User theOwningUser) throws ApiException {
    	ApiException apiException = null;
    	
    	// Rule: If the user specified in update API has an email address, then it must match the non-empty 'owning' user retrieved using
    	//       email address. Owning user can be a new 'empty' user (i.e. no email address or phone number)
    	if( theUserCache.getEmailAddress() != null && theOwningUser.getEmailAddress() != null && !theUser.getKey().equals(theOwningUser.getKey())  ) {
			apiException = new ApiException(ApiStatusCode.EMAIL_ADDRESS_ALREADY_USED);
			throw apiException;
    	}
    	
    	// Rule: If the user specified in update API has an phone number, then it must match the non-empty 'owning' user retrieved using
    	//       phone number. Owning user can be a new 'empty' user (i.e. no key, email address or phone number)
    	if(theOwningUser.getKey() != null){
        	log.info("user key = " + theUser.getKey().toString() + " owning user key = " + theOwningUser.getKey().toString());
        	if( theUserCache.getPhoneNumber() != null && theOwningUser.getPhoneNumber() != null && !theUser.getKey().equals(theOwningUser.getKey())  ) {
    			apiException = new ApiException(ApiStatusCode.PHONE_NUMBER_ALREADY_USED);
    			throw apiException;
        	}
    	}
		
        // Rule: If the mobileCarrierId was specified in the update API, then a phone number must also be specified or already set in the user
        if(theUserCache.getMobileCarrierId() != null) {
        	if(theUserCache.getPhoneNumber() == null && theUser.getPhoneNumber() == null) {
    			apiException = new ApiException(ApiStatusCode.NO_PHONE_NUMBER_TO_ASSOCIATE_WITH_CARRIER_ID);
    			throw apiException;
        	}
        }
        
        // Rule: If a phoneNumber was specified in the update API, then a mobileCarrierId must also be specified or already in the user
        if(theUserCache.getPhoneNumber() != null) {
        	if(theUserCache.getMobileCarrierId() == null && theUser.getSmsEmailAddress() == null) {
    			apiException = new ApiException(ApiStatusCode.NO_CARRIER_ID_TO_ASSOCIATE_WITH_PHONE_NUMBER);
    			throw apiException;
        	}
        }
    }
    
    private void createValidation(User theUser, User theUserCache) throws ApiException {
    	ApiException apiException = null;
        if(theUserCache.getEmailAddress() == null && theUserCache.getPhoneNumber() == null) {
			apiException = new ApiException(ApiStatusCode.EITHER_EMAIL_ADDRESS_OR_PHONE_NUMBER_IS_REQUIRED);
			throw apiException;
        }
        
        // Rule: If a phoneNumber was specified in the update API, then a mobileCarrierId must also be specified or already in the user
        if(theUserCache.getPhoneNumber() != null) {
        	if(theUserCache.getMobileCarrierId() == null && theUser.getSmsEmailAddress() == null) {
    			apiException = new ApiException(ApiStatusCode.NO_CARRIER_ID_TO_ASSOCIATE_WITH_PHONE_NUMBER);
    			throw apiException;
        	}
        }
    }
}
