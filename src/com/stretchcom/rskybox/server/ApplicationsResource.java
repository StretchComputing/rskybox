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

public class ApplicationsResource extends ServerResource {
	private static final Logger log = Logger.getLogger(ApplicationsResource.class.getName());
    private String id;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        id = (String) getRequest().getAttributes().get("id");
    }

    // Handles 'Get Application Info API'
    // Handles 'Get List of Applications API
    @Get("json")
    public JsonRepresentation get(Variant variant) {
        if (id != null) {
            // Get Application Info API
        	log.info("in Get Application Info API");
        	return show();
        } else {
            // Get List of Application API
        	log.info("Get List of Applications API");
        	return index();
        }
    }
    
    // Handles 'Create a new Application' API
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("in post");
        return save_application(entity);
    }

    // Handles 'Update Application API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
		if (this.id == null || this.id.length() == 0) {
			return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);
		}
        return save_application(entity);
    }

    private JsonRepresentation save_application(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();
        JSONObject jsonReturn = null;

        Application application = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
    	User currentUser = Utility.getCurrentUser(getRequest());
        Boolean isUpdate = false;
        String memberRole = null;
        em.getTransaction().begin();
        try {
            application = new Application();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            
            if (id != null) {
            	//////////////////////
            	// Authorization Rules
            	//////////////////////
            	AppMember appMember = AppMember.getAppMember(id, KeyFactory.keyToString(currentUser.getKey()));
            	if(appMember == null) {
					return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
            	}
            	memberRole = appMember.getRole();
            	
                Key key;
				try {
					key = KeyFactory.stringToKey(this.id);
				} catch (Exception e) {
					log.info("ID provided cannot be converted to a Key");
					return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
				}
                application = (Application)em.createNamedQuery("Application.getByKey")
                    	.setParameter("key", key)
                    	.getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            }
            
			if(!isUpdate) {
				if(json.has("name")) {
					String appName = json.getString("name");
					application.setName(appName);
					log.info("stored name value = " + application.getName());
				
//					Application app = Application.getApplicationWithName(appName);
//					if(app != null) {
//						return Utility.apiError(this, ApiStatusCode.APPLICATION_NAME_ALREADY_USED);
//					}
				} else {
					log.info("no JSON name field found");
					return Utility.apiError(this, ApiStatusCode.APPLICATION_NAME_REQUIRED);
				}
			} 
			
			if(json.has("version")) {
				application.setVersion(json.getString("version"));
				// default date to right now
				application.setVersionUpdatedGmtDate(new Date());
			}
			
			if(!isUpdate) {
				// default the created date to right now
				application.setCreatedGmtDate(new Date());
				
				// create the application token
				application.setToken(TF.get());
			}

            em.persist(application);
            em.getTransaction().commit();
            
            if(!isUpdate) {
            	// Create User API so add currentUser creating this application as owner
            	String applicationId = KeyFactory.keyToString(application.getKey());
            	AppMember.addAppMember(applicationId, application, currentUser, AppMember.OWNER_ROLE);
            	
            	jsonReturn = new JSONObject();
            	jsonReturn.put("apiStatus", apiStatus); 
            	jsonReturn.put("applicationId", applicationId);
            	jsonReturn.put("token", application.getToken());
            	log.info("for Create Application API, jsonReturn has been initialized with applicationId = " + applicationId);
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
        	return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        
        if(isUpdate) {
        	jsonReturn = getApplicationJson(application, apiStatus, false, memberRole);
        }
        log.info("about to return");
        return new JsonRepresentation(jsonReturn);
    }

    private JsonRepresentation show() {
        log.info("in show()");
        EntityManager em = EMF.get().createEntityManager();

		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
		Application application = null;
    	User currentUser = Utility.getCurrentUser(getRequest());
		String memberRole = null;
		try {
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	AppMember appMember = AppMember.getAppMember(id, KeyFactory.keyToString(currentUser.getKey()));
        	if(appMember == null) {
				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
        	}
        	memberRole = appMember.getRole();

        	if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
			}
    		application = (Application)em.createNamedQuery("Application.getByKey")
				.setParameter("key", key)
				.getSingleResult();
		} catch (NoResultException e) {
			return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more applications have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} 
        
		JSONObject jsonReturn = getApplicationJson(application, apiStatus, false, memberRole);
		try {
			jsonReturn.put("isAdmin", User.isAdmin());
		} catch (JSONException e) {
			log.severe("JSONException = " + e.getMessage());
		}
		return new JsonRepresentation(jsonReturn);
    }
    
    private JsonRepresentation index() {
        log.info("in index");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
        try {
			List<Application> applications = null;
            JSONArray ja = new JSONArray();
            
        	User user = Utility.getCurrentUser(getRequest());
        	log.info("application list is being retrieved for currentUser email address = " + user.getEmailAddress());
        	if(user != null) {
        		String userId = KeyFactory.keyToString(user.getKey());
    			applications = user.getApplications();
                for (Application app : applications) {
                    ja.put(getApplicationJson(app, true, app.getMemberRole()));
                }
                json.put("applications", ja);
                json.put("apiStatus", apiStatus);
        	} else {
        		this.setStatus(Status.SERVER_ERROR_INTERNAL);
        	}
        } catch (Exception e) {
            log.severe("exception = " + e.getMessage());
        	e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return new JsonRepresentation(json);
    }
    
    private JSONObject getApplicationJson(Application application, Boolean isList, String memberRole) {
    	return getApplicationJson(application, null, isList, memberRole);
    }

    private JSONObject getApplicationJson(Application theApplication, String theApiStatus, Boolean theIsList, String theMemberRole) {
        JSONObject json = new JSONObject();

        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(theApplication != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
        		json.put("id", KeyFactory.keyToString(theApplication.getKey()));
    			
            	Date createdDate = theApplication.getCreatedGmtDate();
            	if(createdDate != null) {
            		json.put("date", GMT.convertToIsoDate(createdDate));
            	}
            	
            	json.put("name", theApplication.getName());
            	json.put("version", theApplication.getVersion());
            	json.put("token", theApplication.getToken());
            	
            	User user = Utility.getCurrentUser(getRequest());
            	if(user != null) {
            		String userId = KeyFactory.keyToString(user.getKey());
            		String applicationId = KeyFactory.keyToString(theApplication.getKey());
            		AppMember am = AppMember.getAppMember(applicationId, userId);
            		if(theMemberRole != null) {
                    	json.put("role", theMemberRole);
            		}
            	}
        	}
        } catch (JSONException e) {
        	log.severe("getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
}
