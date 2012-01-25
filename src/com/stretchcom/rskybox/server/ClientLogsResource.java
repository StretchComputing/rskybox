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
import com.stretchcom.rskybox.models.AppAction;
import com.stretchcom.rskybox.models.AppMember;
import com.stretchcom.rskybox.models.Application;
import com.stretchcom.rskybox.models.ClientLog;
import com.stretchcom.rskybox.models.ClientLogRemoteControl;
import com.stretchcom.rskybox.models.CrashDetect;
import com.stretchcom.rskybox.models.Notification;
import com.stretchcom.rskybox.models.User;

public class ClientLogsResource extends ServerResource {
	private static final Logger log = Logger.getLogger(ClientLogsResource.class.getName());
	private String id;
	private String name;
	private String applicationId;
    private String listStatus;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        this.id = (String) getRequest().getAttributes().get("id");
        this.name = (String) getRequest().getAttributes().get("name");
        this.name = Reference.decode(this.name);
        this.applicationId = (String) getRequest().getAttributes().get("applicationId");
        
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
    	String appIdStatus = Application.verifyApplicationId(this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(this, appIdStatus);
    	}
    	
         JSONObject jsonReturn;

        log.info("in get for Crash Detect resource");
        if (this.id != null) {
            // Get Client Log Info API
        	log.info("in Get ClientLog Info API");
        	return show();
        } else {
            // Get List of Client Logs API
        	log.info("Get List of ClientLogs API");
        	return index();
        }
    }

    // Handles 'Create Client Log API'
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("in post");
    	String appIdStatus = Application.verifyApplicationId(this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(this, appIdStatus);
    	}
    	
        return save_client_log(entity);
    }

    // Handles 'Update Client Log API'
    // Handles 'Remote Control Client Log API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
    	String appIdStatus = Application.verifyApplicationId(this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(this, appIdStatus);
    	}
    	
		// can't be sure which @put API was called - let's just assume it was Update
    	if ( (this.name == null || this.name.length() == 0) && (this.id == null || this.id.length() == 0) ) {
			return Utility.apiError(this, ApiStatusCode.CLIENT_LOG_ID_REQUIRED);
		}
    	
    	if(this.id != null) {
    		// Update Client Log API
            return save_client_log(entity);
    	} else {
    		// Remote Control Client Log API
    		return remote_control(entity);
    	}
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

            List<User> users = new ArrayList<User>();
            JSONArray ja = new JSONArray();
            
			if(this.listStatus != null) {
			    if(this.listStatus.equalsIgnoreCase(ClientLog.NEW_STATUS) || this.listStatus.equalsIgnoreCase(ClientLog.ARCHIVED_STATUS)){
			    	clientLogs= (List<ClientLog>)em.createNamedQuery("ClientLog.getByStatusAndApplicationId")
							.setParameter("status", this.listStatus)
							.setParameter("applicationId", this.applicationId)
							.getResultList();
			    } else if(this.listStatus.equalsIgnoreCase(ClientLog.ALL_STATUS)) {
			    	clientLogs= (List<ClientLog>)em.createNamedQuery("ClientLog.getAllWithApplicationId")
			    			.setParameter("applicationId", this.applicationId)
			    			.getResultList();
			    } else {
			    	return Utility.apiError(this, ApiStatusCode.INVALID_STATUS_PARAMETER);
			    }
			} else {
				// by default, only get 'new' clientLogs
				clientLogs= (List<ClientLog>)em.createNamedQuery("ClientLog.getByStatusAndApplicationId")
						.setParameter("status", ClientLog.NEW_STATUS)
						.setParameter("applicationId", this.applicationId)
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
    	User currentUser = Utility.getCurrentUser(getRequest());
		try {
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	AppMember currentUserMember = AppMember.getAppMember(this.applicationId, KeyFactory.keyToString(currentUser.getKey()));
        	if(currentUserMember == null) {
				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
        	}

			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(this, ApiStatusCode.CLIENT_LOG_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(this, ApiStatusCode.CLIENT_LOG_NOT_FOUND);
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
        JSONObject jsonReturn = new JSONObject();

        ClientLog clientLog = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
    	User currentUser = Utility.getCurrentUser(getRequest());
        em.getTransaction().begin();
        try {
            clientLog = new ClientLog();
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
                clientLog = (ClientLog) em.createNamedQuery("ClientLog.getByKey").setParameter("key", key).getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            }

			if(!isUpdate) {
	            if(json.has("logLevel")) {
					String logLevel = json.getString("logLevel").toLowerCase();
					clientLog.setLogLevel(logLevel);
					if(!clientLog.isLogLevelValid(logLevel)) {
						return Utility.apiError(this, ApiStatusCode.INVALID_LOG_LEVEL);
					}
				} else {
					// default to error
					clientLog.setLogLevel(ClientLog.ERROR_LOG_LEVEL);
				}
	            
	            if(json.has("logName")) {
	            	clientLog.setLogName(json.getString("logName"));
	            } else {
	            	return Utility.apiError(this, ApiStatusCode.LOG_NAME_IS_REQUIRED);
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
			
			if(!isUpdate && json.has("appActions")) {
				List<AppAction> appActions = new ArrayList<AppAction>();
	        	JSONArray appActionsJsonArray = json.getJSONArray("appActions");
				int arraySize = appActionsJsonArray.length();
				log.info("appAction json array length = " + arraySize);
				for(int i=0; i<arraySize; i++) {
					JSONObject appActionJsonObj = appActionsJsonArray.getJSONObject(i);
					AppAction aa = new AppAction();
					if(appActionJsonObj.has("description")) {
						aa.setDescription(appActionJsonObj.getString("description"));
					}
					// TODO support time zone passed in from client
					if(appActionJsonObj.has("timestamp")) {
						String timestampStr = appActionJsonObj.getString("timestamp");
						TimeZone tz = GMT.getTimeZone(RskyboxApplication.DEFAULT_LOCAL_TIME_ZONE);
						// timestamp format includes seconds and milli-seconds
						Date timestamp = GMT.convertToGmtDate(timestampStr, true, tz, RskyboxApplication.APP_ACTION_DATE_FORMAT);
						if(timestamp == null) {
							return Utility.apiError(this, ApiStatusCode.INVALID_TIMESTAMP_PARAMETER);
						}
						aa.setTimestamp(timestamp);
					}
					if(appActionJsonObj.has("duration")) {
						String durationStr = appActionJsonObj.getString("duration");
						Integer duration = null;
						try {
							duration = new Integer(durationStr);
						} catch(NumberFormatException e) {
							log.info("non-integer durations = " + durationStr);
							return Utility.apiError(this, ApiStatusCode.INVALID_DURATION_PARAMETER);
						}
						aa.setDuration(duration);
					}
					appActions.add(aa);
				}
				clientLog.createAppActions(appActions);
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
				clientLog.setApplicationId(this.applicationId);
            	
				// Default status to 'new'
				clientLog.setStatus(CrashDetect.NEW_STATUS);
				
				// Default created date is today
				clientLog.setCreatedGmtDate(new Date());
			}
			
            em.persist(clientLog);
            em.getTransaction().commit();
            
            if(!isUpdate) {
            	User.sendNotifications(this.applicationId, Notification.CLIENT_LOG);
            	
            	ClientLogRemoteControl clrc = ClientLogRemoteControl.getEntity(this.applicationId, clientLog.getLogName());
            	// if there is no clientLogRemoteControl, then mode must defaults to ACTIVE
            	String logStatus = ClientLogRemoteControl.ACITVE_MODE;
            	if(clrc != null) {
            		logStatus = clrc.getMode();
            	}
				jsonReturn.put("logStatus", logStatus);
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
        
	    try {
	    	jsonReturn.put("apiStatus", apiStatus);
	    } catch (JSONException e) {
	        log.severe("exception = " + e.getMessage());
	    	e.printStackTrace();
	        this.setStatus(Status.SERVER_ERROR_INTERNAL);
	    }
	    return new JsonRepresentation(jsonReturn);
    }
    
    private JsonRepresentation remote_control(Representation entity) {
        JSONObject jsonReturn = new JSONObject();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
        try {
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            
            String mode = null;
            if(json.has("mode")) {
            	mode = json.getString("mode");
            	if(!ClientLogRemoteControl.isModeValid(mode)) {
            		return Utility.apiError(this, ApiStatusCode.INVALID_MODE);
            	}
            } else {
            	return Utility.apiError(this, ApiStatusCode.MODE_IS_REQUIRED);
            }
            
            ClientLogRemoteControl.update(this.applicationId, this.name, mode);
        } catch (IOException e) {
            log.severe("error extracting JSON object from Post. exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
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
            		TimeZone tz = GMT.getTimeZone(RskyboxApplication.DEFAULT_LOCAL_TIME_ZONE);
            		String dateFormat = RskyboxApplication.INFO_DATE_FORMAT;
            		if(isList) {dateFormat = RskyboxApplication.LIST_DATE_FORMAT;}
            		json.put("date", GMT.convertToLocalDate(createdDate, tz, dateFormat));
            	}
            	json.put("userName", clientLog.getUserName());
            	json.put("instanceUrl", clientLog.getInstanceUrl());
            	json.put("logLevel", clientLog.getLogLevel());
            	json.put("logName", clientLog.getLogName());
            	json.put("message", clientLog.getMessage());
            	json.put("stackBackTrace", clientLog.getStackBackTrace());
            	
            	if(!isList) {
                	JSONArray appActionsJsonArray = new JSONArray();
                	List<AppAction> appActions = clientLog.getAppActions();
                	TimeZone tz = GMT.getTimeZone(RskyboxApplication.DEFAULT_LOCAL_TIME_ZONE);
                	for(AppAction aa : appActions) {
                		JSONObject appActionJsonObj = new JSONObject();
                		appActionJsonObj.put("description", aa.getDescription());
                		appActionJsonObj.put("timestamp", GMT.convertToLocalDate(aa.getTimestamp(), tz, RskyboxApplication.APP_ACTION_DATE_FORMAT));
                		if(aa.getDuration() != null) appActionJsonObj.put("duration", aa.getDuration());
                		appActionsJsonArray.put(appActionJsonObj);
                	}
                	if(appActions.size() > 0) {json.put("appActions", appActionsJsonArray);}
                	
                	ClientLogRemoteControl clrc = ClientLogRemoteControl.getEntity(this.applicationId, clientLog.getLogName());
                	// if there is no clientLogRemoteControl, then mode must defaults to ACTIVE
                	String logMode = ClientLogRemoteControl.ACITVE_MODE;
                	if(clrc != null) {logMode = clrc.getMode();}
    				json.put("logMode", logMode);
            	}
            	
            	// TODO remove eventually, for backward compatibility before status field existed. If status not set, default to 'new'
            	String status = clientLog.getStatus();
            	if(status == null || status.length() == 0) {status = "new";}
            	json.put("status", status);
        	}
        } catch (JSONException e) {
        	log.severe("UsersResrouce::getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
}
