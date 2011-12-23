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
			return Utility.apiError(ApiStatusCode.USER_ID_REQUIRED);
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
				user = Utility.getCurrentUser(getRequest());
	        	if(user == null) {
	        		return Utility.apiError(ApiStatusCode.USER_NOT_FOUND);
	        	}
	        	isSuperAdmin = user.getIsSuperAdmin();
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
            String phoneNumber = null;
            String emailAddress = null;
            String carrierDomainName = null;
            
            // TODO add email validation
            if (json.has("emailAddress")) {
            	emailAddress = json.getString("emailAddress").toLowerCase();
            }
            
            if (json.has("phoneNumber")) {
            	phoneNumber = json.getString("phoneNumber");
            	phoneNumber = Utility.extractAllDigits(phoneNumber);
            }

            if (id != null) {
            	// this is an Update User API call
                Key key = KeyFactory.stringToKey(this.id);
                user = (User) em.createNamedQuery("User.getByKey").setParameter("key", key).getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
                
                // ***** Maybe all the following code needs to be moved to the Request Authorization API
                // ***** Not sure if Update should allow these fields to changes.  If so, then a confirmation 
                
                ///////////////////////////////////////////////
                // mobileCarrierId/smsEmailAddress Update Rules
                ///////////////////////////////////////////////
                //  1. A phone number must have been passed in or already set in the user entity for the mobileCarrierId/smsEmailAddress to be updated
                if (json.has("mobileCarrierId")) {
                	if(phoneNumber != null || user.getPhoneNumber() != null) {
                    	carrierDomainName = MobileCarrier.findEmailDomainName(json.getString("mobileCarrierId"));
                    	if(carrierDomainName == null) {
                    		return Utility.apiError(ApiStatusCode.INVALID_MOBILE_CARRIER_PARAMETER);
                    	}
                    	String phoneNumberComponent = phoneNumber == null ? user.getPhoneNumber() : phoneNumber;
                    	String smsEmailAddress = phoneNumberComponent + carrierDomainName;
                    	user.setSmsEmailAddress(smsEmailAddress);
                	} else {
                		return Utility.apiError(ApiStatusCode.NO_PHONE_NUMBER_TO_ASSOCIATE_WITH_CARRIER_ID);
                	}
                }

                /////////////////////////////
                // Email Address Update Rules
                /////////////////////////////
                //  1. If the email address has been confirmed, it cannot be updated
                //  2. If a new email address is specified, it cannot already be used by another user
                if(emailAddress != null) {
                    if(user.getIsEmailConfirmed() == null || !user.getIsEmailConfirmed()) {
                    	// check if the email address is really being modified. If so, it can't be in use and confirmed by any other user
                    	if( (user.getEmailAddress() == null) || (user.getEmailAddress() != null && !emailAddress.equals(user.getEmailAddress()))  ) {
                    		User existingUser = User.getUser(em, emailAddress, null);
                    		if(existingUser == null) {
                            	user.setEmailAddress(emailAddress);
                    		} else {
                        		return Utility.apiError(ApiStatusCode.EMAIL_ADDRESS_ALREADY_USED);
                    		}
                    	}
                    } else {
                		return Utility.apiError(ApiStatusCode.EMAIL_ADDRESS_CAN_NO_LONGER_BE_MODIFIED);
                    }
                }

                ////////////////////////////
                // Phone Number Update Rules
                ////////////////////////////
                //  1. If the phone number has been confirmed, it cannot be updated
                //  2. To update, there has to be a carrier ID - either sent as part of the update or already in the user entity
                //  3. If a new phone number is specified, it cannot already be used by another user
                if(phoneNumber != null) {
                    if( (user.getIsSmsConfirmed() == null || !user.getIsSmsConfirmed()) &&
                    	(carrierDomainName != null || user.getSmsEmailAddress() != null)    ) {
                    	// check if the phone number is really being modified. If so, it can't be in use and confirmed by any other user
                    	if( (user.getPhoneNumber() == null) || (user.getPhoneNumber() != null && !phoneNumber.equals(user.getPhoneNumber()))  ) {
                    		User existingUser = User.getUserWithPhoneNumber(em, phoneNumber, null);
                    		if(existingUser == null) {
                            	user.setPhoneNumber(phoneNumber);
                    		} else {
                        		return Utility.apiError(ApiStatusCode.PHONE_NUMBER_ALREADY_USED);
                    		}
                    	}
                    } else {
                		return Utility.apiError(ApiStatusCode.PHONE_NUMBER_CAN_NO_LONGER_BE_MODIFIED);
                    }
                }
            } else {
            	// this is a Create User API call  
            	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            	// NOTE: on a create, email address, phone number and SMS email address (derived from the carrier ID cannot be set or modified.
            	//       Either the (email address) or (phone number and carrier ID) was used to send a confirmation code. The Create User is
            	//       the following to the confirmation code, so until the registration process completes, these fields cannot be changed.
            	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                String confirmationCode = null;
                
            	if(json.has("confirmationCode")) {
                    confirmationCode = json.getString("confirmationCode");
            	} else {
            		return Utility.apiError(ApiStatusCode.CONFIRMATION_CODE_IS_REQUIRED);
            	}
                
                if(emailAddress != null && phoneNumber != null) {
            		return Utility.apiError(ApiStatusCode.EMAIL_ADDRESS_PHONE_NUMBER_MUTUALLY_EXCLUSIVE);
                }
                
                if(emailAddress == null && phoneNumber == null) {
            		return Utility.apiError(ApiStatusCode.EITHER_EMAIL_ADDRESS_OR_PHONE_NUMBER_IS_REQUIRED);
                }
                
                String storedConfirmationCode = null;
                if(emailAddress != null) {
                	user = User.getUser(em, emailAddress, null);
                	if(user == null) {
                		return Utility.apiError(ApiStatusCode.EMAIL_ADDRESS_NOT_FOUND);
                	}
                	if(user.getEmailConfirmationCode() == null) {
                		return Utility.apiError(ApiStatusCode.USER_EMAIL_ADDRESS_NOT_PENDING_CONFIRMATION);
                	} else {
                		storedConfirmationCode = user.getEmailConfirmationCode();
                		user.setIsEmailConfirmed(true);
                	}
                 } else {
                	 user = User.getUserWithPhoneNumber(em, phoneNumber, null);
                	if(user == null) {
                		return Utility.apiError(ApiStatusCode.PHONE_NUMBER_NOT_FOUND);
                	}
                	if(user.getSmsConfirmationCode() == null) {
                		return Utility.apiError(ApiStatusCode.USER_PHONE_NUMBER_NOT_PENDING_CONFIRMATION);
                	} else {
                		storedConfirmationCode = user.getSmsConfirmationCode();
                		user.setIsSmsConfirmed(true);
                	}
                }
                
                if(confirmationCode != null && !storedConfirmationCode.equals(confirmationCode)) {
            		return Utility.apiError(ApiStatusCode.INVALID_CONFIRMATION_CODE);
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
	        		return Utility.apiError(ApiStatusCode.PASSWORD_TOO_SHORT);
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
	        		return Utility.apiError(ApiStatusCode.PASSWORD_IS_REQUIRED);
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
                json.put("memberConfirmed", user.getWasMembershipConfirmed());

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
        JSONObject jsonReturn = new JSONObject();
        boolean emailConfirmationSent = false;
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
            if (json.has("mobileCarrierId") && phoneNumber != null && phoneNumber.length() > 0) {
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
            String subject = "rSkybox confirmation code";
            if(emailAddress != null) {
            	// check if user with this email address already exists
            	user = User.getUser(em, emailAddress, null);
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
                    user.setSendEmailNotifications(true);
            	}
                user.setEmailConfirmationCode(confirmationCode);
            	log.info("sending email confirmation code = " + confirmationCode + " to " + user.getEmailAddress());
            	Emailer.send(user.getEmailAddress(), subject, buildEmailConfirmationMessage(user), Emailer.NO_REPLY);
            	emailConfirmationSent = true;
            	
            	// even though confirmation is through email, phone number field can be set
            	if(phoneNumber != null && carrierDomainName != null) {
                    user.setPhoneNumber(phoneNumber);
                	String smsEmailAddress = user.getPhoneNumber() + carrierDomainName;
                	user.setSmsEmailAddress(smsEmailAddress);
            	}
            } else {
            	// check if user with this phone number already exists
            	user = User.getUserWithPhoneNumber(em, phoneNumber, null);
            	if(user != null) {
            		log.info("user found with phoneNumber = " + phoneNumber);
            		if(user.getIsSmsConfirmed() != null && user.getIsSmsConfirmed()) {
                    	return Utility.apiError(ApiStatusCode.USER_ALREADY_HAS_CONFIRMED_PHONE_NUMBER);
            		}
            	} else {
                    // if there is no existing user, create one now
            		log.info("creating new user");
                    user = new User();
                    user.setPhoneNumber(phoneNumber);
                	String smsEmailAddress = user.getPhoneNumber() + carrierDomainName;
                	user.setSmsEmailAddress(smsEmailAddress);
                	user.setSendSmsNotifications(true);
            	}
            	user.setSmsConfirmationCode(confirmationCode);
            	log.info("sending SMS confirmation code = " + confirmationCode + " to " + user.getSmsEmailAddress());
                Emailer.send(user.getSmsEmailAddress(), subject, buildSmsConfirmationMessage(user), Emailer.NO_REPLY);
            }
            
            // emailAddress sent in JSON Return if email confirmation sent. phoneNumber sent in JSON Return if phoneNumber confirmation sent.
            if(emailConfirmationSent) {
            	jsonReturn.put("emailAddress", emailAddress);
            } else {
            	jsonReturn.put("phoneNumber", phoneNumber);
            }
            if (json.has("testApp") && json.getBoolean("testApp")) {
                jsonReturn.put("confirmationCode", confirmationCode);
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
    		return Utility.apiError(ApiStatusCode.USER_NAME_IS_REQUIRED);
        } else if(this.password == null) {
    		return Utility.apiError(ApiStatusCode.PASSWORD_IS_REQUIRED);
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
    		return Utility.apiError(ApiStatusCode.INVALID_USER_CREDENTIALS);
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
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
        em.getTransaction().begin();
        try {
            user = new User();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();

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
        		return Utility.apiError(ApiStatusCode.EITHER_EMAIL_ADDRESS_OR_PHONE_NUMBER_IS_REQUIRED);
            }
            
            String storedConfirmationCode = null;
            if(emailAddress != null) {
            	user = User.getUser(em, emailAddress, null);
            	if(user == null) {
            		return Utility.apiError(ApiStatusCode.EMAIL_ADDRESS_NOT_FOUND);
            	}
            	if(user.getEmailConfirmationCode() == null) {
            		return Utility.apiError(ApiStatusCode.USER_EMAIL_ADDRESS_NOT_PENDING_CONFIRMATION);
            	} else if(user.getIsEmailConfirmed() != null && user.getIsEmailConfirmed()) {
            		return Utility.apiError(ApiStatusCode.USER_ALREADY_HAS_CONFIRMED_EMAIL_ADDRESS);
            	} else {
            		storedConfirmationCode = user.getEmailConfirmationCode();
            		user.setIsEmailConfirmed(true);
            	}
             } else {
            	 user = User.getUserWithPhoneNumber(em, phoneNumber, null);
            	if(user == null) {
            		return Utility.apiError(ApiStatusCode.PHONE_NUMBER_NOT_FOUND);
            	}
            	if(user.getSmsConfirmationCode() == null) {
            		return Utility.apiError(ApiStatusCode.USER_PHONE_NUMBER_NOT_PENDING_CONFIRMATION);
            	} else if(user.getIsSmsConfirmed() != null && user.getIsSmsConfirmed()) {
            		return Utility.apiError(ApiStatusCode.USER_ALREADY_HAS_CONFIRMED_PHONE_NUMBER);
            	} else {
            		storedConfirmationCode = user.getSmsConfirmationCode();
            		user.setIsSmsConfirmed(true);
            	}
            }
            
            if(!storedConfirmationCode.equals(confirmationCode)) {
        		return Utility.apiError(ApiStatusCode.INVALID_CONFIRMATION_CODE);
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
        
        try {
        	jsonReturn.put("apiStatus", apiStatus);
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
    
    private String buildEmailConfirmationMessage(User theUser) {
    	StringBuffer sb = new StringBuffer("Your confirmation code is " + theUser.getEmailConfirmationCode());
    	sb.append(".<br><br>");
        sb.append(RskyboxApplication.USER_VERIFICATION_PAGE);
        sb.append("?");
        sb.append("emailAddress=");
        sb.append(Utility.urlEncode(theUser.getEmailAddress()));
        sb.append("&");
        sb.append("confirmationCode=");
        sb.append(Utility.urlEncode(theUser.getEmailConfirmationCode()));
        sb.append("&");
        sb.append("preregistration=");
        if(theUser.getToken() == null) {
            sb.append("true");
        } else {
            sb.append("false");
        }
        return sb.toString();
    }
    
    private String buildSmsConfirmationMessage(User theUser) {
    	StringBuffer sb = new StringBuffer("Your confirmation code is " + theUser.getSmsConfirmationCode());
        sb.append(".<br><br>");
        sb.append(RskyboxApplication.USER_VERIFICATION_PAGE);
        sb.append("?");
        sb.append("phoneNumber=");
        sb.append(Utility.urlEncode(theUser.getPhoneNumber()));
        sb.append("&");
        sb.append("confirmationCode=");
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
}
