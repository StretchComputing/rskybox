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
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.stretchcom.rskybox.models.AppAction;
import com.stretchcom.rskybox.models.AppMember;
import com.stretchcom.rskybox.models.Application;
import com.stretchcom.rskybox.models.ClientLog;
import com.stretchcom.rskybox.models.CrashDetect;
import com.stretchcom.rskybox.models.EndpointFilter;
import com.stretchcom.rskybox.models.Incident;
import com.stretchcom.rskybox.models.Stream;
import com.stretchcom.rskybox.models.User;

public class EndpointFiltersResource extends ServerResource {
	private static final Logger log = Logger.getLogger(EndpointFiltersResource.class.getName());
	private String id;
	private String applicationId;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        this.id = (String) getRequest().getAttributes().get("id");
        this.applicationId = (String) getRequest().getAttributes().get("applicationId");
    }

    // Handles 'List Endpoint Filter API'    // Handles 'Get List of Client Logs API
    @Get("json")
    public JsonRepresentation get(Variant variant) {
    	String appIdStatus = Application.verifyApplicationId(this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(this, appIdStatus);
    	}

        // List Endpoint Filter API
    	log.info("List Endpoint Filter API");
    	return index();
    }

    // Handles 'Endpoint Filter OPTIONS'
    // Needed for CORs protocol
    @Options("json")
    public JsonRepresentation options(Representation entity) {
		Form headers = (Form) getResponseAttributes().get("org.restlet.http.headers");
		if (headers == null) {
			headers = new Form();
			getResponseAttributes().put("org.restlet.http.headers", headers);
		}
		headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Access-Control-Allow-Methods", "OPTIONS, POST");
		headers.add("Access-Control-Allow-Headers", "Authorization,Content-Type");
		return new JsonRepresentation(new JSONObject());
    }

    // Handles 'Create Endpoint Filter API'
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("in post");
		if(this.applicationId == null) {return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);}
		Application application = Application.getApplicationWithId(this.applicationId);
		if(application == null) {
			return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
		}
		
        return save_endpoint_filter(entity, application);
    }

    // Handles 'Update Endpoint Filter API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
		if(this.applicationId == null) {return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);}
		Application application = Application.getApplicationWithId(this.applicationId);
		if(application == null) {
			return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
		}
    	
		// endpoint filter ID is required
    	if (this.id == null || this.id.length() == 0) {
			return Utility.apiError(this, ApiStatusCode.ENDPOINT_FILTER_ID_REQUIRED);
		}
    	
        return save_endpoint_filter(entity, application);
    }

    // Handles 'Delete Endpoint Filter API'
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
				return Utility.apiError(this, ApiStatusCode.ENDPOINT_FILTER_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(this, ApiStatusCode.ENDPOINT_FILTER_NOT_FOUND);
			}

    		EndpointFilter endpointFilter = (EndpointFilter)em.createNamedQuery("EndpointFilter.getByKey")
				.setParameter("key", key)
				.getSingleResult();
            
            em.remove(endpointFilter);
            em.getTransaction().commit();
        } catch (NoResultException e) {
    		return Utility.apiError(this, ApiStatusCode.ENDPOINT_FILTER_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more endpoint filters have same key");
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
		List<EndpointFilter> endpointFilters = null;
    	User currentUser = Utility.getCurrentUser(getRequest());
        try {
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	AppMember appMember = AppMember.getAppMember(this.applicationId, KeyFactory.keyToString(currentUser.getKey()));
        	if(appMember == null) {
				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
        	}
        	
        	// Create an All filter for this user if it doesn't already exist.
        	EndpointFilter.createAllFilter(currentUser.getId(), this.applicationId);

        	List<EndpointFilter> allFilters = new ArrayList<EndpointFilter>();
            JSONArray ja = new JSONArray();
			endpointFilters= (List<EndpointFilter>)em.createNamedQuery("EndpointFilter.getByUserIdAndApplicationId")
					.setParameter("applicationId", this.applicationId)
					.setParameter("userId", currentUser.getId())
					.getResultList();
			log.info("endpointFilters: applicationId result set count = " + endpointFilters.size());
			allFilters.addAll(endpointFilters);
			
            Incident.mergePotentialEndpointFilters(this.applicationId, currentUser, allFilters);
            
            for (EndpointFilter ef : allFilters) {
            	JSONObject endpointFilterObj = EndpointFilter.getJson(ef, true);
            	if(endpointFilterObj == null) {
            		this.setStatus(Status.SERVER_ERROR_INTERNAL);
            		break;
            	}
                ja.put(endpointFilterObj);
            }
            json.put("endpointFilters", ja);
            json.put("apiStatus", apiStatus);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
        	e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } finally {
			em.close();
		}
        return new JsonRepresentation(json);
    }

    private JsonRepresentation save_endpoint_filter(Representation entity, Application theApplication) {
        EntityManager em = EMF.get().createEntityManager();
        JSONObject jsonReturn = new JSONObject();

        EndpointFilter endpointFilter = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
    	User currentUser = Utility.getCurrentUser(getRequest());
        em.getTransaction().begin();
        try {
            endpointFilter = new EndpointFilter();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            Boolean isUpdate = false;
            if (id != null) {
            	//////////////////////
            	// Authorization Rules
            	//////////////////////
            	AppMember currentUserMember = AppMember.getAppMember(this.applicationId, KeyFactory.keyToString(currentUser.getKey()));
            	if(currentUserMember == null) {
    				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
            	}

                Key key;
    			try {
    				key = KeyFactory.stringToKey(this.id);
    			} catch (Exception e) {
    				log.info("ID provided cannot be converted to a Key");
    				return Utility.apiError(this, ApiStatusCode.CLIENT_LOG_NOT_FOUND);
    			}
                endpointFilter = (EndpointFilter) em.createNamedQuery("EndpointFilter.getByKey").setParameter("key", key).getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            }

			if(!isUpdate && json.has("localEndpoint")) {
            	endpointFilter.setLocalEndpoint(json.getString("localEndpoint"));
            	if(endpointFilter.getLocalEndpoint() == null || endpointFilter.getLocalEndpoint().length() == 0) {
            		return Utility.apiError(this, ApiStatusCode.LOCAL_ENDPOINT_REQUIRED);
            	}
			}

			if(!isUpdate && json.has("remoteEndpoint")) {
            	endpointFilter.setRemoteEndpoint(json.getString("remoteEndpoint"));
            	if(endpointFilter.getRemoteEndpoint() == null || endpointFilter.getRemoteEndpoint().length() == 0) {
            		return Utility.apiError(this, ApiStatusCode.REMOTE_ENDPOINT_REQUIRED);
            	}
			}

			if(isUpdate) {
				// for now, only 'active' can be updated
	            if(json.has("active")) {
	            	try {
	            		Boolean isActive = json.getBoolean("active");
	            		endpointFilter.setIsActive(isActive);
	            	} catch (JSONException e) {
	                    return Utility.apiError(this, ApiStatusCode.INVALID_ACTIVE_PARAMETER);
	                }	            	
	            }
			} else {
				// set the defaults for create
				endpointFilter.setUserId(currentUser.getId());
				endpointFilter.setApplicationId(this.applicationId);
				endpointFilter.setCreatedGmtDate(new Date());
				endpointFilter.setIsActive(true);
			}
			
			em.persist(endpointFilter);
            em.getTransaction().commit();
            
            id = KeyFactory.keyToString(endpointFilter.getKey());
            jsonReturn.put("id", id);
        } catch (IOException e) {
            log.severe("error extracting JSON object from Post. exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (NoResultException e) {
        	return Utility.apiError(this, ApiStatusCode.ENDPOINT_FILTER_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more end point filters have same key");
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
	        log.severe("exception = " + e.getMessage());
	    	e.printStackTrace();
	        this.setStatus(Status.SERVER_ERROR_INTERNAL);
	    }
	    return new JsonRepresentation(jsonReturn);
    }
}
