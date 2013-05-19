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

            JSONArray ja = new JSONArray();
	    	List<Stream> streams = (List<Stream>)em.createNamedQuery("Stream.getNotStatusAndApplicationId")
	    			.setParameter("applicationId", this.applicationId)
	    			.setParameter("status", Stream.CLOSED_STATUS)
	    			.getResultList();
			log.info("streams: not closed stream count = " + streams.size());
            
            for (Stream s : streams) {
            	JSONObject streamObj = Stream.getJson(s, true);
            	if(streamObj == null) {
            		this.setStatus(Status.SERVER_ERROR_INTERNAL);
            		break;
            	}
                ja.put(streamObj);
            }
            json.put("streams", ja);
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
		Boolean wasStreamCreated = false;
		String streamId = null;
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
            if(id != null) {  
            	////////////////////
            	// this is an update
            	////////////////////

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
                
	            if(json.has("status")) {
	            	String status = json.getString("status").toLowerCase();
	            	if(Stream.isUpdateStatusValid(status)) {
	            		stream.setStatus(status);
	            	} else {
						return Utility.apiError(this, ApiStatusCode.INVALID_STATUS);
	            	}
	            }
            } else { 
            	///////////////////
            	// this is a create
            	///////////////////
				String endUserId = null;
				String memberId = null;
				if(json.has("userId")) {
					endUserId = json.getString("userId");
				}
				if(json.has("memberId")) {
					memberId = json.getString("memberId");
				}
				if(endUserId == null && memberId == null) {
					return Utility.apiError(this, ApiStatusCode.EITHER_USER_ID_OR_MEMBER_ID_IS_REQUIRED);
				} else if(endUserId != null && memberId != null) {
					return Utility.apiError(this, ApiStatusCode.BOTH_USER_ID_AND_MEMBER_ID_SPECIFIED);
				}

				if(json.has("name")) {
					String name = json.getString("name");
					// make sure this name is not actively being used
					List<Stream> streams = Stream.getByNameAndNotClosed(name, this.applicationId, em);
					if(streams != null && streams.size() > 0) {
	    				log.info("stream name = " + name + " already being used by " + streams.size() + " streams.");
	    				if(streams.size() > 1) {
	    					log.severe("should never happen - there are two or more non-closed streams with the same name");
	    					this.setStatus(Status.SERVER_ERROR_INTERNAL);
	    					return new JsonRepresentation(jsonReturn);
	    				}
	    				
	    				stream = streams.get(0);
	    				if(stream.getStatus().equalsIgnoreCase(Stream.OPEN_STATUS)) {
	    					return Utility.apiError(this, ApiStatusCode.STREAM_NAME_ALREADY_USED);
	    				}
	    				
	    				///////////////////////////////
    					// stream must be in INIT state
	    				///////////////////////////////
	    				
    					if(endUserId != null) {
    						if(stream.getEndUserId() != null) {
        						return Utility.apiError(this, ApiStatusCode.STREAM_ALREADY_HAS_END_USER);
    						}
    						stream.setEndUserId(endUserId);
    					} else if(memberId != null) {
    						if(stream.getMemberId() != null) {
    							return Utility.apiError(this, ApiStatusCode.STREAM_ALREADY_HAS_MEMBER);
    						}
    						stream.setMemberId(memberId);
    					}
    					if(stream.getEndUserId() != null && stream.getMemberId() != null) {
    						stream.setStatus(Stream.OPEN_STATUS);
    					}
					} else {
						// non-closed stream does not exist. So create it ...
						wasStreamCreated = true;
						stream = new Stream();
						stream.setCreatedGmtDate(new Date());
						stream.setApplicationId(this.applicationId);
						stream.setStatus(Stream.INIT_STATUS);
						stream.setEndUserId(endUserId);
						stream.setMemberId(memberId);
					}
					stream.setName(name);
				} else {
					return Utility.apiError(this, ApiStatusCode.NAME_IS_REQUIRED);
				}
            }
			
			em.persist(stream);
            em.getTransaction().commit();
            
            streamId = KeyFactory.keyToString(stream.getKey());
            if(isUpdate) {
            	Stream.deleteMarkers(this.applicationId, streamId);
            } else {
    	    	jsonReturn.put("id", streamId);
            	jsonReturn.put("created", wasStreamCreated);
                Stream.createMarkers(this.applicationId, streamId);
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
