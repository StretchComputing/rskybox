package com.stretchcom.mobilePulse.server;

import java.io.IOException;
import java.util.ArrayList;
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
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.stretchcom.mobilePulse.models.AppMember;
import com.stretchcom.mobilePulse.models.Feedback;
import com.stretchcom.mobilePulse.models.User;

public class AppMembersResource extends ServerResource {
	private static final Logger log = Logger.getLogger(AppMembersResource.class.getName());
    private String id;
	private String applicationId;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        id = (String) getRequest().getAttributes().get("id");
        this.applicationId = (String) getRequest().getAttributes().get("applicationId");
    }

    // Handles 'Get AppMember Info API'
    // Handles 'Get List of AppMembers API
    @Get("json")
    public JsonRepresentation get(Variant variant) {
    	String appIdStatus = Utility.verifyUserAuthorizedForApplication(getRequest(), this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(appIdStatus);
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
    	String appIdStatus = Utility.verifyUserAuthorizedForApplication(getRequest(), this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(appIdStatus);
    	}
    	
        return save_appMember(entity);
    }

    // Handles 'Update AppMember API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
    	String appIdStatus = Utility.verifyUserAuthorizedForApplication(getRequest(), this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(appIdStatus);
    	}
    	
		if (this.id == null || this.id.length() == 0) {
			return Utility.apiError(ApiStatusCode.APP_MEMBER_ID_REQUIRED);
		}
        return save_appMember(entity);
    }

    private JsonRepresentation save_appMember(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();

        AppMember appMember = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
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
					return Utility.apiError(ApiStatusCode.APP_MEMBER_NOT_FOUND);
				}
                appMember = (AppMember)em.createNamedQuery("AppMember.getByKey")
                    	.setParameter("key", key)
                    	.getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            }
            
			if(!isUpdate && json.has("emailAddress")) {
				appMember.setEmailAddress(json.getString("emailAddress"));
				log.info("stored email address value = " + appMember.getEmailAddress());
			}
			
			if(json.has("role")) {
            	String role = json.getString("role").toLowerCase();
            	if(appMember.isRoleValid(role)) {
                    appMember.setRole(role);
            	} else {
					log.info("invalid status = " + role);
					return Utility.apiError(ApiStatusCode.INVALID_ROLE);
            	}
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
						return Utility.apiError(ApiStatusCode.INVALID_STATUS);
	            	}
	            }
			} else {
				appMember.setApplicationId(this.applicationId);
				
				// creating an appMember so default status to 'pending'
				appMember.setStatus(AppMember.PENDING_STATUS);
				
				// default the created date to right now
				appMember.setCreatedGmtDate(new Date());
			}

            em.persist(appMember);
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
		AppMember appMember = null;
		try {
			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(ApiStatusCode.APP_MEMBER_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(ApiStatusCode.APP_MEMBER_NOT_FOUND);
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
        try {
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
                		TimeZone tz = GMT.getTimeZone(MobilePulseApplication.DEFAULT_LOCAL_TIME_ZONE);
                		String dateFormat = MobilePulseApplication.INFO_DATE_FORMAT;
                		json.put("date", GMT.convertToLocalDate(createdDate, tz, dateFormat));
                	}
        		}
            	
            	json.put("emailAddress", appMember.getEmailAddress());
            	json.put("role", appMember.getRole());
            	json.put("status", appMember.getStatus());
        	}
        } catch (JSONException e) {
        	log.severe("getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
}
