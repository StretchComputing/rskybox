package com.stretchcom.rskybox.server;

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
			return Utility.apiError(ApiStatusCode.APPLICATION_ID_REQUIRED);
		}
        return save_application(entity);
    }

    private JsonRepresentation save_application(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();

        Application application = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
        em.getTransaction().begin();
        try {
            application = new Application();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            Boolean isUpdate = false;
            if (id != null) {
                Key key;
				try {
					key = KeyFactory.stringToKey(this.id);
				} catch (Exception e) {
					log.info("ID provided cannot be converted to a Key");
					return Utility.apiError(ApiStatusCode.APPLICATION_NOT_FOUND);
				}
                application = (Application)em.createNamedQuery("Application.getByKey")
                    	.setParameter("key", key)
                    	.getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            }
            
			if(!isUpdate && json.has("name")) {
				application.setName(json.getString("name"));
				log.info("stored name value = " + application.getName());
			} else {
				log.info("no JSON name field found");
			}
			
			if(json.has("version")) {
				application.setVersion(json.getString("version"));
				// default date to right now
				application.setVersionUpdatedGmtDate(new Date());
			}
			
			if(!isUpdate) {
				// default the created date to right now
				application.setCreatedGmtDate(new Date());
			}

            em.persist(application);
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
			log.info("Application not found");
			apiStatus = ApiStatusCode.APPLICATION_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        
        return new JsonRepresentation(getApplicationJson(application, apiStatus, false));
    }

    private JsonRepresentation show() {
        log.info("in show()");
        EntityManager em = EMF.get().createEntityManager();

		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
		Application application = null;
		try {
			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(ApiStatusCode.APPLICATION_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(ApiStatusCode.APPLICATION_NOT_FOUND);
			}
    		application = (Application)em.createNamedQuery("Application.getByKey")
				.setParameter("key", key)
				.getSingleResult();
		} catch (NoResultException e) {
			log.info("Application not found");
			apiStatus = ApiStatusCode.APPLICATION_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more applications have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} 
        
		JSONObject jsonReturn = getApplicationJson(application, apiStatus, false);
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
            
			applications= (List<Application>)em.createNamedQuery("Application.getAll").getResultList();
            for (Application app : applications) {
                ja.put(getApplicationJson(app, true));
            }
            json.put("applications", ja);
            json.put("apiStatus", apiStatus);
            json.put("isAdmin", User.isAdmin());
        } catch (Exception e) {
            log.severe("exception = " + e.getMessage());
        	e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return new JsonRepresentation(json);
    }
    
    private JSONObject getApplicationJson(Application application, Boolean isList) {
    	return getApplicationJson(application, null, isList);
    }

    private JSONObject getApplicationJson(Application application, String theApiStatus, Boolean isList) {
        JSONObject json = new JSONObject();

        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(application != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
        		json.put("id", KeyFactory.keyToString(application.getKey()));
    			
            	Date createdDate = application.getCreatedGmtDate();
            	// TODO support time zones
            	if(createdDate != null) {
            		TimeZone tz = GMT.getTimeZone(RskyboxApplication.DEFAULT_LOCAL_TIME_ZONE);
            		String dateFormat = RskyboxApplication.INFO_DATE_FORMAT;
            		if(isList) {dateFormat = RskyboxApplication.LIST_DATE_FORMAT;}
            		json.put("date", GMT.convertToLocalDate(createdDate, tz, dateFormat));
            	}
            	
            	json.put("name", application.getName());
            	json.put("version", application.getVersion());
        	}
        } catch (JSONException e) {
        	log.severe("getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
}
