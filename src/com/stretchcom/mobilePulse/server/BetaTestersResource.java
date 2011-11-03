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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.stretchcom.mobilePulse.models.BetaTester;
import com.stretchcom.mobilePulse.models.MobileCarrier;
import com.stretchcom.mobilePulse.models.User;

public class BetaTestersResource extends ServerResource {
    private static final Logger log = Logger.getLogger(BetaTestersResource.class.getName());
    private String id;
	private String applicationId;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        id = (String) getRequest().getAttributes().get("id");
        this.applicationId = (String) getRequest().getAttributes().get("applicationId");
    }

    // Handles 'Get Beta Tester API'
    // Handles 'Get List of Beta Testers API
    @Get("json")
    public JsonRepresentation get(Variant variant) {
    	String appIdStatus = Utility.verifyUserAuthorizedForApplication(getRequest(), this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(appIdStatus);
    	}
    	
        if (id != null) {
            // Get User Info API
        	log.info("in Get Beta Tester Info API");
        	return show();
        } else {
            // Get List of Feedback API
        	log.info("Get List of Beta Testers API");
        	return index();
        }
    }

    // Handles 'Create Beta Tester API'
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("in post");
    	String appIdStatus = Utility.verifyUserAuthorizedForApplication(getRequest(), this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(appIdStatus);
    	}
    	
        return save_beta_tester(entity);
    }

    // Handles 'Update Beta Tester API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
    	String appIdStatus = Utility.verifyUserAuthorizedForApplication(getRequest(), this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(appIdStatus);
    	}
    	
		if (this.id == null || this.id.length() == 0) {
			return Utility.apiError(ApiStatusCode.BETA_TESTER_ID_REQUIRED);
		}
        return save_beta_tester(entity);
    }

    // Handles 'Delete Beta Tester API'
    @Delete("json")
    public JsonRepresentation delete() {
        log.info("in delete");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
        try {
			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(ApiStatusCode.BETA_TESTER_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(ApiStatusCode.BETA_TESTER_NOT_FOUND);
			}
            em.getTransaction().begin();
            BetaTester betaTester = (BetaTester) em.createNamedQuery("BetaTester.getByKey").setParameter("key", key).getSingleResult();
            em.remove(betaTester);
            em.getTransaction().commit();
        } catch (NoResultException e) {
			log.info("Beta Tester not found");
			apiStatus = ApiStatusCode.BETA_TESTER_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more beta testers have same key");
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
            List<BetaTester> betaTesters = new ArrayList<BetaTester>();
            JSONArray ja = new JSONArray();
            betaTesters = (List<BetaTester>) em.createNamedQuery("BetaTester.getAllWithApplicationId")
            		.setParameter("applicationId", this.applicationId)
            		.getResultList();
            for (BetaTester betaTester : betaTesters) {
                ja.put(getBetaTesterJson(betaTester));
            }
            json.put("betaTesters", ja);
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
		BetaTester betaTester = null;
		try {
			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(ApiStatusCode.BETA_TESTER_ID_REQUIRED);
			}
			
			// id of beta tester specified
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(ApiStatusCode.BETA_TESTER_NOT_FOUND);
			}
			
    		betaTester = (BetaTester)em.createNamedQuery("BetaTester.getByKey")
				.setParameter("key", key)
				.getSingleResult();
		} catch (NoResultException e) {
			log.info("Beta Tester not found");
			apiStatus = ApiStatusCode.BETA_TESTER_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more beta testers have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} 
        
        return new JsonRepresentation(getBetaTesterJson(betaTester, apiStatus));
    }

    private JsonRepresentation save_beta_tester(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();

        BetaTester betaTester = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
        em.getTransaction().begin();
        try {
            betaTester = new BetaTester();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            Boolean isUpdate = false;
            if (id != null) {
                Key key = KeyFactory.stringToKey(this.id);
                betaTester = (BetaTester) em.createNamedQuery("BetaTester.getByKey").setParameter("key", key).getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            }
            
            if (!isUpdate && json.has("userName")) {
            	betaTester.setUserName(json.getString("userName"));
            }
            
            if (!isUpdate && json.has("application")) {
            	betaTester.setApplication(json.getString("application"));
            }
            
            if (json.has("version")) {
            	betaTester.setVersion(json.getString("version"));
            }
            
            if (!isUpdate && json.has("instanceUrl")) {
            	betaTester.setInstanceUrl(json.getString("instanceUrl"));
            }
            
            if(!isUpdate) {
            	betaTester.setApplicationId(this.applicationId);
            }
            
            em.persist(betaTester);
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
			log.info("Beta Tester not found");
			apiStatus = ApiStatusCode.BETA_TESTER_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more beta testers have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        
        return new JsonRepresentation(getBetaTesterJson(betaTester, apiStatus));
    }
    
    private JSONObject getBetaTesterJson(BetaTester betaTester) {
    	return getBetaTesterJson(betaTester, null);
    }

    private JSONObject getBetaTesterJson(BetaTester betaTester, String theApiStatus) {
        JSONObject json = new JSONObject();

        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(betaTester != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
                json.put("id", KeyFactory.keyToString(betaTester.getKey()));
                json.put("userName", betaTester.getUserName());
                json.put("application", betaTester.getApplication());
                json.put("version", betaTester.getVersion());
                json.put("instanceUrl", betaTester.getInstanceUrl());
        	}
        } catch (JSONException e) {
        	log.severe("getBetaTesterJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
}
