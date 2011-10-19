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
import com.google.appengine.api.datastore.Text;
import com.stretchcom.mobilePulse.models.ClientLog;
import com.stretchcom.mobilePulse.models.CrashDetect;
import com.stretchcom.mobilePulse.models.User;

public class ClientLogsResource extends ServerResource {
	private static final Logger log = Logger.getLogger(ClientLogsResource.class.getName());
	private String id;
    private String listStatus;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        this.id = (String) getRequest().getAttributes().get("id");
        
		Form form = getRequest().getResourceRef().getQueryAsForm();
		for (Parameter parameter : form) {
			log.info("parameter " + parameter.getName() + " = " + parameter.getValue());
			if(parameter.getName().equals("status"))  {
				this.listStatus = (String)parameter.getValue().toLowerCase();
				this.listStatus = Reference.decode(this.listStatus);
				log.info("ClientLogResource() - decoded status = " + this.listStatus);
			} 
		}
    }

    // Handles 'Get Client Log Info API'
    // Handles 'Get Client Log of Users API
    @Get("json")
    public JsonRepresentation get(Variant variant) {
         JSONObject jsonReturn;

        log.info("in get for Crash Detect resource");
        if (this.id != null) {
            // Get Client Log Info API
        	log.info("in Get Feedback Info API");
        	return show();
        } else {
            // Get List of Client Logs API
        	log.info("Get List of Feedbacks API");
        	return index();
        }
    }

    // Handles 'Create Client Log API'
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("in post");
        return save_client_log(entity);
    }

    // Handles 'Update Client Log API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
		if (this.id == null || this.id.length() == 0) {
			return Utility.apiError(ApiStatusCode.CLIENT_LOG_ID_REQUIRED);
		}
        return save_client_log(entity);
    }
    
    private JsonRepresentation index() {
        log.info("in index");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
		List<ClientLog> clientLogs = null;
        try {
            List<User> users = new ArrayList<User>();
            JSONArray ja = new JSONArray();
            
			if(this.listStatus != null) {
			    if(this.listStatus.equalsIgnoreCase(ClientLog.NEW_STATUS) || this.listStatus.equalsIgnoreCase(ClientLog.ARCHIVED_STATUS)){
			    	clientLogs= (List<ClientLog>)em.createNamedQuery("ClientLog.getByStatus")
							.setParameter("status", this.listStatus)
							.getResultList();
			    } else if(this.listStatus.equalsIgnoreCase(ClientLog.ALL_STATUS)) {
			    	clientLogs= (List<ClientLog>)em.createNamedQuery("ClientLog.getAll").getResultList();
			    } else {
			    	return Utility.apiError(ApiStatusCode.INVALID_STATUS_PARAMETER);
			    }
			} else {
				// by default, only get 'new' feedback
				clientLogs= (List<ClientLog>)em.createNamedQuery("ClientLog.getByStatus")
						.setParameter("status", ClientLog.NEW_STATUS)
						.getResultList();
			}
            
            for (ClientLog cl : clientLogs) {
                ja.put(getClientLogJson(cl, true));
            }
            json.put("clientLogs", ja);
            json.put("apiStatus", apiStatus);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
        	e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return new JsonRepresentation(json);
    }

    private JsonRepresentation show() {
        log.info("UserResource in show()");
        EntityManager em = EMF.get().createEntityManager();

		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
		ClientLog clientLog = null;
		try {
			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(ApiStatusCode.CLIENT_LOG_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(ApiStatusCode.CLIENT_LOG_NOT_FOUND);
			}
    		clientLog = (ClientLog)em.createNamedQuery("ClientLog.getByKey")
				.setParameter("key", key)
				.getSingleResult();
		} catch (NoResultException e) {
			log.info("Client Log not found");
			apiStatus = ApiStatusCode.CLIENT_LOG_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more client logs have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} 
        
        return new JsonRepresentation(getClientLogJson(clientLog, apiStatus, false));
    }

    private JsonRepresentation save_client_log(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();

        ClientLog clientLog = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
        em.getTransaction().begin();
        try {
            clientLog = new ClientLog();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            Boolean isUpdate = false;
            if (id != null) {
                Key key;
    			try {
    				key = KeyFactory.stringToKey(this.id);
    			} catch (Exception e) {
    				log.info("ID provided cannot be converted to a Key");
    				return Utility.apiError(ApiStatusCode.CLIENT_LOG_NOT_FOUND);
    			}
                clientLog = (ClientLog) em.createNamedQuery("ClientLog.getByKey").setParameter("key", key).getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            }

			if(!isUpdate) {
	            if(json.has("logLevel")) {
					String logLevel = json.getString("logLevel").toLowerCase();
					clientLog.setLogLevel(logLevel);
					if(!clientLog.isLogLevelValid(logLevel)) {
						return Utility.apiError(ApiStatusCode.INVALID_LOG_LEVEL);
					}
				} else {
					// default to error
					clientLog.setLogLevel(ClientLog.ERROR_LOG_LEVEL);
				}
			}
			
			if(!isUpdate && json.has("message")) {
				clientLog.setMessage(json.getString("message"));
			}
			
			if(!isUpdate && json.has("stackBackTrace")) {
				clientLog.setStackBackTrace(json.getString("stackBackTrace"));
			}
			
			if(!isUpdate && json.has("userName")) {
				clientLog.setUserName(json.getString("userName"));
			}
			
			if(!isUpdate && json.has("instanceUrl")) {
				clientLog.setInstanceUrl(json.getString("instanceUrl"));
			}
			
			if(isUpdate) {
	            if(json.has("status")) {
	            	String status = json.getString("status").toLowerCase();
	            	if(clientLog.isStatusValid(status)) {
	            		clientLog.setStatus(status);
	            	} else {
	            		apiStatus = ApiStatusCode.INVALID_STATUS;
	            	}
	            }
			} else {
				// Default status to 'new'
				clientLog.setStatus(CrashDetect.NEW_STATUS);
			}
			
			// Default created date is today
			clientLog.setCreatedGmtDate(new Date());
            
            em.persist(clientLog);
            em.getTransaction().commit();
            
            if(!isUpdate) User.sendNotifications("new client log");
        } catch (IOException e) {
            log.severe("error extracting JSON object from Post. exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (NoResultException e) {
			log.info("Client Log not found");
			apiStatus = ApiStatusCode.CLIENT_LOG_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more client logs have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        
        return new JsonRepresentation(getClientLogJson(clientLog, apiStatus, false));
    }
    
    private JSONObject getClientLogJson(ClientLog clientLog, Boolean isList) {
    	return getClientLogJson(clientLog, null, isList);
    }

    private JSONObject getClientLogJson(ClientLog clientLog, String theApiStatus, Boolean isList) {
        JSONObject json = new JSONObject();

        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(clientLog != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
        		json.put("id", KeyFactory.keyToString(clientLog.getKey()));
    			
            	Date createdDate = clientLog.getCreatedGmtDate();
            	// TODO support time zones
            	if(createdDate != null) {
            		TimeZone tz = GMT.getTimeZone(MobilePulseApplication.DEFAULT_LOCAL_TIME_ZONE);
            		String dateFormat = MobilePulseApplication.INFO_DATE_FORMAT;
            		if(isList) {dateFormat = MobilePulseApplication.LIST_DATE_FORMAT;}
            		json.put("date", GMT.convertToLocalDate(createdDate, tz, dateFormat));
            	}
            	json.put("userName", clientLog.getUserName());
            	json.put("instanceUrl", clientLog.getInstanceUrl());
            	json.put("logLevel", clientLog.getLogLevel());
            	json.put("message", clientLog.getMessage());
            	json.put("stackBackTrace", clientLog.getStackBackTrace());
            	
            	// TODO remove eventually, for backward compatibility before status field existed. If status not set, default to 'new'
            	String status = clientLog.getStatus();
            	if(status == null || status.length() == 0) {status = "new";}
            	json.put("status", status);
            	
            	log.info("Client Log JSON object = " + clientLog.toString());
        	}
        } catch (JSONException e) {
        	log.severe("UsersResrouce::getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
}
