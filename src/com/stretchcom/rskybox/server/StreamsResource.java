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
import com.stretchcom.rskybox.models.ClientLogRemoteControl;
import com.stretchcom.rskybox.models.CrashDetect;
import com.stretchcom.rskybox.models.Incident;
import com.stretchcom.rskybox.models.Notification;
import com.stretchcom.rskybox.models.Stream;
import com.stretchcom.rskybox.models.User;

public class StreamsResource extends ServerResource {
	private static final Logger log = Logger.getLogger(StreamsResource.class.getName());
	private String id;
	private String applicationId;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        this.id = (String) getRequest().getAttributes().get("id");
        this.applicationId = (String) getRequest().getAttributes().get("applicationId");
        
		Form form = getRequest().getResourceRef().getQueryAsForm();
		for (Parameter parameter : form) {
			log.info("parameter " + parameter.getName() + " = " + parameter.getValue());
//			if(parameter.getName().equals("status"))  {
//				this.listStatus = (String)parameter.getValue().toLowerCase();
//				this.listStatus = Reference.decode(this.listStatus);
//				log.info("ClientLogResource() - decoded status = " + this.listStatus);
//			}
		}
    }

    // Handles 'Get Stream Info API'
    @Get("json")
    public JsonRepresentation get(Variant variant) {
    	String appIdStatus = Application.verifyApplicationId(this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(this, appIdStatus);
    	}

        log.info("in get for Stream resource");
        if (this.id != null) {
            // Get Stream Info API
        	log.info("in Get Stream Info API");
        	return show();
        } else {
            // Get List of Streams API
        	log.info("Get List of Streams API");
        	return index();
        }
    }

    // Handles 'Stream OPTIONS'
    @Options("json")
    public JsonRepresentation options(Representation entity) {
		Form headers = (Form) getResponseAttributes().get("org.restlet.http.headers");
		if (headers == null) {
			headers = new Form();
			getResponseAttributes().put("org.restlet.http.headers", headers);
		}
		headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Access-Control-Allow-Methods", "OPTIONS, POST");
		headers.add("Access-Control-Allow-Headers", "Authorization");
    		return new JsonRepresentation(new JSONObject());
    }

    // Handles 'Create Stream API'
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("in post");
		if(this.applicationId == null) {return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);}
		Application application = Application.getApplicationWithId(this.applicationId);
		if(application == null) {
			return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
		}
		
        return save_stream(entity, application);
    }

    // Handles 'Update Stream API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
    	String appIdStatus = Application.verifyApplicationId(this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(this, appIdStatus);
    	}
    	
		// can't be sure which @put API was called - let's just assume it was Update
    	if ( (this.id == null || this.id.length() == 0) ) {
			return Utility.apiError(this, ApiStatusCode.STREAM_ID_REQUIRED);
		}
    	
        return save_stream(entity, null);
    }
    
    // NOT IMPLEMENTED YET
    private JsonRepresentation index() {
        log.info("in index");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
		List<ClientLog> clientLogs = null;
    	User currentUser = Utility.getCurrentUser(getRequest());
        try {
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	AppMember appMember = AppMember.getAppMember(this.applicationId, KeyFactory.keyToString(currentUser.getKey()));
        	if(appMember == null) {
				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
        	}

            List<User> users = new ArrayList<User>();
            JSONArray ja = new JSONArray();
            
//            for (ClientLog cl : clientLogs) {
//            	JSONObject clientLogObj = ClientLog.getJson(cl, true);
//            	if(clientLogObj == null) {
//            		this.setStatus(Status.SERVER_ERROR_INTERNAL);
//            		break;
//            	}
//                ja.put(clientLogObj);
//            }
//            json.put("clientLogs", ja);
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

    private JsonRepresentation show() {
        log.info("StreamsResource in show()");
        EntityManager em = EMF.get().createEntityManager();

		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
		Stream stream = null;
		
		// currently, this API is only called from the application with the embedded rSkybox agent.
    	Application currentApp = Utility.getCurrentApp(getRequest());
		try {
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	if(!currentApp.getId().equals(this.applicationId)) {
				return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_AUTHORIZED);
        	}

			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(this, ApiStatusCode.STREAM_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(this, ApiStatusCode.STREAM_NOT_FOUND);
			}
    		stream = (Stream)em.createNamedQuery("Stream.getByKey")
				.setParameter("key", key)
				.getSingleResult();
		} catch (NoResultException e) {
			return Utility.apiError(this, ApiStatusCode.STREAM_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more streams have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
			em.close();
		} 
        
        JSONObject clientJsonObj = Stream.getJson(stream, apiStatus, false);
        if(clientJsonObj == null) {
        	this.setStatus(Status.SERVER_ERROR_INTERNAL);
        	clientJsonObj = new JSONObject();
        }
		return new JsonRepresentation(clientJsonObj);
    }

    private JsonRepresentation save_stream(Representation entity, Application theApplication) {
        EntityManager em = EMF.get().createEntityManager();
        JSONObject jsonReturn = new JSONObject();

        Stream stream = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
        
    	// can be called either from a member running the rSkybox client or from a application with embedded rSkybox agent
    	User currentUser = Utility.getCurrentUser(getRequest());
    	Application currentApp = Utility.getCurrentApp(getRequest());
    	
        em.getTransaction().begin();
		Incident owningIncident = null;
        try {
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	if(currentUser != null) {
            	AppMember currentUserMember = AppMember.getAppMember(this.applicationId, KeyFactory.keyToString(currentUser.getKey()));
            	if(currentUserMember == null) {
    				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
            	}
        	} else {
            	if(!currentApp.getId().equals(this.applicationId)) {
    				return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_AUTHORIZED);
            	}
        	}
            
            Boolean isUpdate = false;
            if(id != null) {  // this is an update

                Key key;
    			try {
    				key = KeyFactory.stringToKey(this.id);
    			} catch (Exception e) {
    				log.info("ID provided cannot be converted to a Key");
    				return Utility.apiError(this, ApiStatusCode.STREAM_NOT_FOUND);
    			}
                stream = (Stream) em.createNamedQuery("Stream.getByKey").setParameter("key", key).getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            } else {  // this is a create
                stream = new Stream();
				stream.setCreatedGmtDate(new Date());
				stream.setApplicationId(this.applicationId);
				stream.setStatus(Stream.INIT_STATUS);
            }
			
			if(!isUpdate && json.has("name")) {
				String name = json.getString("name");
				// make sure this name is not actively being used
				List<Stream> streams = Stream.getByName(name, this.applicationId);
				if(streams != null && streams.size() > 0) {
    				log.info("stream name = " + name + " already being used by " + streams.size() + " streams.");
    				return Utility.apiError(this, ApiStatusCode.STREAM_NAME_ALREADY_USED);
				}
				stream.setName(name);
			}
			
			if(isUpdate) {
	            if(json.has("status")) {
	            	String status = json.getString("status").toLowerCase();
	            	if(Stream.isUpdateStatusValid(status)) {
	            		stream.setStatus(status);
	            	} else {
						return Utility.apiError(this, ApiStatusCode.INVALID_STATUS);
	            	}
	            }
			}
			
			em.persist(stream);
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
        	return Utility.apiError(this, ApiStatusCode.CLIENT_LOG_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more streams have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        
	    try {
	    	jsonReturn.put("apiStatus", apiStatus);
	    	jsonReturn.put("incident", owningIncident.getJson());
	    } catch (JSONException e) {
	        log.severe("exception = " + e.getMessage());
	    	e.printStackTrace();
	        this.setStatus(Status.SERVER_ERROR_INTERNAL);
	    }
	    return new JsonRepresentation(jsonReturn);
    }
    
//    private JsonRepresentation remote_control(Representation entity) {
//        JSONObject jsonReturn = new JSONObject();
//        
//		String apiStatus = ApiStatusCode.SUCCESS;
//        this.setStatus(Status.SUCCESS_CREATED);
//        try {
//            JSONObject json = new JsonRepresentation(entity).getJsonObject();
//            
//            String mode = null;
//            if(json.has("mode")) {
//            	mode = json.getString("mode");
//            	if(!ClientLogRemoteControl.isModeValid(mode)) {
//            		return Utility.apiError(this, ApiStatusCode.INVALID_MODE);
//            	}
//            } else {
//            	return Utility.apiError(this, ApiStatusCode.MODE_IS_REQUIRED);
//            }
//            
//            ClientLogRemoteControl.update(this.applicationId, this.name, mode);
//        } catch (IOException e) {
//            log.severe("error extracting JSON object from Post. exception = " + e.getMessage());
//            e.printStackTrace();
//            this.setStatus(Status.SERVER_ERROR_INTERNAL);
//        } catch (JSONException e) {
//            log.severe("exception = " + e.getMessage());
//            e.printStackTrace();
//            this.setStatus(Status.SERVER_ERROR_INTERNAL);
//        }
//        
//	    try {
//	    	jsonReturn.put("apiStatus", apiStatus);
//	    } catch (JSONException e) {
//	        log.severe("exception = " + e.getMessage());
//	    	e.printStackTrace();
//	        this.setStatus(Status.SERVER_ERROR_INTERNAL);
//	    }
//	    return new JsonRepresentation(jsonReturn);
//    }
    
}
