package com.stretchcom.rskybox.server;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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
import com.stretchcom.rskybox.models.AppMember;
import com.stretchcom.rskybox.models.Application;
import com.stretchcom.rskybox.models.User;

public class AppMembersResource extends ServerResource {
	private static final Logger log = Logger.getLogger(AppMembersResource.class.getName());
    private String id;
	private String applicationId;
	private Application application;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        this.id = (String) getRequest().getAttributes().get("id");
        this.applicationId = (String) getRequest().getAttributes().get("applicationId");
    }

    // Handles 'Get AppMember Info API'
    // Handles 'Get List of AppMembers API
    @Get("json")
    public JsonRepresentation get(Variant variant) {
    	if(this.applicationId == null) {
    		return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);
    	}
		this.application = Application.getApplicationWithId(this.applicationId);
		if(application == null) {
    		return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
		}
    	
        if (id != null) {
            // Get AppMember Info API
        	log.info("in Get AppMember Info API");
        	return show();
        } else {
            // Get List of AppMembers API
        	log.info("Get List of AppMembers API");
        	return index();
        }
    }
    
    // Handles 'Create a new AppMember' API
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("in post");
    	if(this.applicationId == null) {
    		return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);
    	}
		this.application = Application.getApplicationWithId(this.applicationId);
		if(application == null) {
    		return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
		}
    	
        return save_appMember(entity);
    }

    // Handles 'Update AppMember API'
    // Handles 'Confirm Member API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
		// TODO?? verify application ID is valid
    	if(this.applicationId == null) {
    		return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);
    	}
		this.application = Application.getApplicationWithId(this.applicationId);
		if(application == null) {
    		return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
		}
    	
    	if(this.id.equalsIgnoreCase("confirmation")) {
    		// Confirm Member API
    		return confirmMember(entity);
    	} else {
    		// Update AppMember API
    		if (this.id == null || this.id.length() == 0) {
    			return Utility.apiError(this, ApiStatusCode.APP_MEMBER_ID_REQUIRED);
    		}
            return save_appMember(entity);
    	}
    }

    // Handles 'Delete AppMember API'
    @Delete("json")
    public JsonRepresentation delete() {
        log.info("in delete");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
    	User currentUser = Utility.getCurrentUser(getRequest());
        em.getTransaction().begin();
        try {
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	AppMember currentUserMember = AppMember.getAppMember(this.applicationId, KeyFactory.keyToString(currentUser.getKey()));
        	if(currentUserMember == null) {
				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
        	}

        	if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(this, ApiStatusCode.APP_MEMBER_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(this, ApiStatusCode.APP_MEMBER_NOT_FOUND);
			}

    		AppMember appMember = (AppMember)em.createNamedQuery("AppMember.getByKey")
				.setParameter("key", key)
				.getSingleResult();
    		
        	///////////////////////////
        	// More Authorization Rules
        	///////////////////////////
    		// app owner can not be deleted
    		if(appMember.getRole().equalsIgnoreCase(AppMember.OWNER_ROLE)) {
				return Utility.apiError(this, ApiStatusCode.APP_OWNER_CANNOT_BE_DELETED);
    		}
    		// must be an owner to delete a manager
    		if(!currentUserMember.hasOwnerAuthority() && appMember.getRole().equalsIgnoreCase(AppMember.MANAGER_ROLE)) {
				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_TO_DELETE_MEMBER_WITH_SPECIFIED_ROLE);
    		}
    		// must be a manager to delete a member
    		if(!currentUserMember.hasManagerAuthority()) {
				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_TO_DELETE_MEMBER);
    		}
            
            em.remove(appMember);
            em.getTransaction().commit();
        } catch (NoResultException e) {
			log.info("User not found");
			apiStatus = ApiStatusCode.APP_MEMBER_NOT_FOUND;
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

    private JsonRepresentation save_appMember(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();

        AppMember appMember = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
    	User currentUser = Utility.getCurrentUser(getRequest());
        em.getTransaction().begin();
        try {
            appMember = new AppMember();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            Boolean isUpdate = false;
            if (id != null) {
                Key key;
				try {
					key = KeyFactory.stringToKey(this.id);
				} catch (Exception e) {
					log.info("ID provided cannot be converted to a Key");
					return Utility.apiError(this, ApiStatusCode.APP_MEMBER_NOT_FOUND);
				}
                appMember = (AppMember)em.createNamedQuery("AppMember.getByKey")
                    	.setParameter("key", key)
                    	.getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            }
            
			if(!isUpdate) {
				if(json.has("emailAddress")) {
					String emailAddress = json.getString("emailAddress");
					// verify that not already a member of this application
					AppMember appMemberWithEmail = AppMember.getAppMemberWithEmailAddress(this.applicationId, emailAddress);
					if(appMemberWithEmail != null) {
						return Utility.apiError(this, ApiStatusCode.USER_ALREADY_MEMBER);
					}
					
					appMember.setEmailAddress(emailAddress);
					log.info("stored email address value = " + appMember.getEmailAddress());
				} else {
					return Utility.apiError(this, ApiStatusCode.EMAIL_ADDRESS_IS_REQUIRED);
				}
			}
			
			String role = null;
			String originalRole = null;
			if(json.has("role")) {
				originalRole = appMember.getRole();
            	role = json.getString("role").toLowerCase();
            	if(appMember.isRoleValid(role)) {
                    appMember.setRole(role);
            	} else {
					log.info("invalid status = " + role);
					return Utility.apiError(this, ApiStatusCode.INVALID_ROLE);
            	}
			} else if(!isUpdate) {
				return Utility.apiError(this, ApiStatusCode.ROLE_IS_REQUIRED);
			}
			
			if(isUpdate) {
	            if(json.has("status")) {
	            	String newStatus = json.getString("status").toLowerCase();
	            	if(appMember.isStatusValid(newStatus)) {
	            		//////////////////////////////////////////////////////////////////////////////////////
	            		// if status is changing from 'pending' to 'active', the userId field must also be set
	            		//////////////////////////////////////////////////////////////////////////////////////
	            		String originalStatus = appMember.getStatus();
	            		if(originalStatus.equalsIgnoreCase(AppMember.PENDING_STATUS) && newStatus.equalsIgnoreCase(AppMember.ACTIVE_STATUS)) {
	            			appMember.setUserIdViaEmailAddress();
	            		}
	                    appMember.setStatus(newStatus);
	            	} else {
						log.info("invalid status = " + newStatus);
						return Utility.apiError(this, ApiStatusCode.INVALID_STATUS);
	            	}
	            }
			} else {
				appMember.setApplicationId(this.applicationId);
				appMember.setApplicationName(this.application.getName());
				
				// creating an appMember so default status to 'pending'
				appMember.setStatus(AppMember.PENDING_STATUS);
				
				// default the created date to right now
				appMember.setCreatedGmtDate(new Date());
				
				appMember.setEmailConfirmationCode(TF.getConfirmationCode());
			}
			
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	AppMember currentUserMember = AppMember.getAppMember(this.applicationId, KeyFactory.keyToString(currentUser.getKey()));
        	if(currentUserMember == null) {
				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
        	}
        	if(!currentUserMember.hasManagerAuthority()) {
        		if(isUpdate) {
    				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_TO_UPDATE_MEMBER);
        		} else {
    				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_TO_CREATE_MEMBER);
        		}
        	}
        	if(role != null) {
        		if(isUpdate) {
        			if(originalRole != null && !originalRole.equalsIgnoreCase(role)) {
        				if(role.equalsIgnoreCase(AppMember.OWNER_ROLE) || originalRole.equalsIgnoreCase(AppMember.OWNER_ROLE)) {
            				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_TO_UPDATE_MEMBER_WITH_SPECIFIED_ROLE);
        				}
              		}
        		} else {
            		if(role.equalsIgnoreCase(AppMember.OWNER_ROLE)) {
        				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_TO_CREATE_MEMBER_WITH_SPECIFIED_ROLE);
            		}
            		
        			if(role.equalsIgnoreCase(AppMember.MANAGER_ROLE) && !currentUserMember.hasOwnerAuthority()) {
        				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_TO_CREATE_MEMBER_WITH_SPECIFIED_ROLE);
        			}
        		}
        	}

            em.persist(appMember);
            em.getTransaction().commit();
            
            AppMember.sendMemberConfirmation(appMember, this.applicationId);
        } catch (IOException e) {
            log.severe("error extracting JSON object from Post. exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (NoResultException e) {
			log.info("AppMember not found");
			apiStatus = ApiStatusCode.APP_MEMBER_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        
        return new JsonRepresentation(getAppMemberJson(appMember, apiStatus, false));
    }

    private JsonRepresentation show() {
        log.info("in show()");
        EntityManager em = EMF.get().createEntityManager();

		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
    	User currentUser = Utility.getCurrentUser(getRequest());
    	AppMember appMember = null;
		try {
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	AppMember currentUserMember = AppMember.getAppMember(this.applicationId, KeyFactory.keyToString(currentUser.getKey()));
        	if(currentUserMember == null) {
				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
        	}

        	if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(this, ApiStatusCode.APP_MEMBER_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(this, ApiStatusCode.APP_MEMBER_NOT_FOUND);
			}
    		appMember = (AppMember)em.createNamedQuery("AppMember.getByKey")
				.setParameter("key", key)
				.getSingleResult();
		} catch (NoResultException e) {
			log.info("AppMember not found");
			apiStatus = ApiStatusCode.APP_MEMBER_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more AppMembers have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} 
        
        return new JsonRepresentation(getAppMemberJson(appMember, apiStatus, false));
    }
    
    private JsonRepresentation index() {
        log.info("UserResource in index");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
    	User currentUser = Utility.getCurrentUser(getRequest());
        try {
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	AppMember appMember = AppMember.getAppMember(this.applicationId, KeyFactory.keyToString(currentUser.getKey()));
        	if(appMember == null) {
				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
        	}

        	List<AppMember> appMembers = null;
            JSONArray ja = new JSONArray();
            
			appMembers= (List<AppMember>)em.createNamedQuery("AppMember.getAllWithApplicationId")
					.setParameter("applicationId", this.applicationId)
					.getResultList();
            for (AppMember am : appMembers) {
                ja.put(getAppMemberJson(am, true));
            }
            json.put("appMembers", ja);
            json.put("apiStatus", apiStatus);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
        	e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return new JsonRepresentation(json);
    }
    
    private JSONObject getAppMemberJson(AppMember appMember, Boolean isList) {
    	return getAppMemberJson(appMember, null, isList);
    }

    private JSONObject getAppMemberJson(AppMember appMember, String theApiStatus, Boolean isList) {
        JSONObject json = new JSONObject();

        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(appMember != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
        		json.put("id", KeyFactory.keyToString(appMember.getKey()));
        		
        		if(!isList) {
                	Date createdDate = appMember.getCreatedGmtDate();
                	// TODO support time zones
                	if(createdDate != null) {
                		TimeZone tz = GMT.getTimeZone(RskyboxApplication.DEFAULT_LOCAL_TIME_ZONE);
                		String dateFormat = RskyboxApplication.INFO_DATE_FORMAT;
                		json.put("date", GMT.convertToLocalDate(createdDate, tz, dateFormat));
                	}
        		}
            	
            	json.put("emailAddress", appMember.getEmailAddress());
            	json.put("phoneNumber", appMember.getPhoneNumber());
            	json.put("role", appMember.getRole());
            	json.put("status", appMember.getStatus());
            	json.put("appId", appMember.getApplicationId());
        	}
        } catch (JSONException e) {
        	log.severe("getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }

    private JsonRepresentation confirmMember(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();

		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
		AppMember appMember = null;
		em.getTransaction().begin();
        JSONObject jsonReturn = new JSONObject();
		try {
			JSONObject json = new JsonRepresentation(entity).getJsonObject();
			
			String emailAddress = null;
			if(json.has("emailAddress")) {
				emailAddress = json.getString("emailAddress");
			}
			
			String confirmationCode = null;
			if(json.has("confirmationCode")) {
				confirmationCode = json.getString("confirmationCode");
			}
			
			if(emailAddress == null || emailAddress.trim().length() == 0) {
				return Utility.apiError(this, ApiStatusCode.EMAIL_ADDRESS_IS_REQUIRED);
			}
				
			if(confirmationCode == null || confirmationCode.trim().length() == 0) {
				return Utility.apiError(this, ApiStatusCode.CONFIRMATION_CODE_IS_REQUIRED);
			}
			
			appMember = (AppMember)em.createNamedQuery("AppMember.getByApplicationIdAndEmailAddressAndEmailConfirmationCode")
				.setParameter("applicationId", this.applicationId)
				.setParameter("emailAddress", emailAddress)
				.setParameter("emailConfirmationCode", confirmationCode)
				.getSingleResult();
			
			if(!appMember.getStatus().equalsIgnoreCase(AppMember.PENDING_STATUS)) {
				return Utility.apiError(this, ApiStatusCode.MEMBER_NOT_PENDING_CONFIRMATION);
			}
			
			List<User> users = User.getUsersWithEmailAddress(emailAddress);
			Boolean createPendingUser = false;
			if(users != null) {
				if(users.size() > 1) {
					log.severe("should never happen - more than one user with the same email address");
					this.setStatus(Status.SERVER_ERROR_INTERNAL);
				} else {
					if(users.size() == 0) {
						appMember.setConfirmInitiated(true);
						apiStatus = ApiStatusCode.MEMBER_NOT_A_REGISTERED_USER;
						createPendingUser = true;
					} else {
						// ok, so a single user with the email address has been found ...
						User user = users.get(0);
						if(user.getIsEmailConfirmed()) {
							// confirmed user so we can confirm the membership right now
							appMember.setUserId(KeyFactory.keyToString(user.getKey()));
							appMember.setStatus(AppMember.ACTIVE_STATUS);
							appMember.setConfirmInitiated(false);
						} else {
							appMember.setConfirmInitiated(true);
							apiStatus = ApiStatusCode.MEMBER_NOT_A_REGISTERED_USER;
						}
					}
		            em.persist(appMember);
		            em.getTransaction().commit();
		            
					// we create a 'pending user' here because the user -- as part of responding to this new membership -- has already
					// confirmed their email address. So the User registration process that is about to be launched by the client does
					// not need to call Request Confirmation Code API.
		            if(createPendingUser) {
						User.createUser(emailAddress, confirmationCode);
		            }
		            
		            jsonReturn.put("emailAddress", emailAddress);
		            jsonReturn.put("confirmationCode", confirmationCode);
				}			
			}
		} catch (NoResultException e) {
			log.info("AppMember not found");
			apiStatus = ApiStatusCode.APP_MEMBER_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more AppMembers have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
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
			log.severe("error creating JSON return object");
			e.printStackTrace();
		}
		return new JsonRepresentation(jsonReturn);
    }
}
