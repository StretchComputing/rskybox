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
import com.google.appengine.api.datastore.Text;
import com.stretchcom.rskybox.models.AppAction;
import com.stretchcom.rskybox.models.AppMember;
import com.stretchcom.rskybox.models.Application;
import com.stretchcom.rskybox.models.ClientLog;
import com.stretchcom.rskybox.models.ClientLogRemoteControl;
import com.stretchcom.rskybox.models.CrashDetect;
import com.stretchcom.rskybox.models.Incident;
import com.stretchcom.rskybox.models.Notification;
import com.stretchcom.rskybox.models.User;

public class ClientLogsResource extends ServerResource {
	private static final Logger log = Logger.getLogger(ClientLogsResource.class.getName());
	private String id;
	private String name;
	private String applicationId;
    private String listStatus;
    private String incidentId;

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
			} else if(parameter.getName().equals("incidentId"))  {
				this.incidentId = (String)parameter.getValue();
				this.incidentId = Reference.decode(this.incidentId);
				log.info("ClientLogResource() - incident ID = " + this.incidentId);
			} 
		}
    }

    // Handles 'Get Client Log Info API'
    // Handles 'Get List of Client Logs API
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

    // Handles 'Client Log OPTIONS'
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

    // Handles 'Create Client Log API'
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("in post");
		if(this.applicationId == null) {return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);}
		Application application = Application.getApplicationWithId(this.applicationId);
		if(application == null) {
			return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
		}
		
        return save_client_log(entity, application);
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
    	
    	// TODO remote control is deprecated at the log level. Now handled by incident. Hold the code for a little while
    	//if(this.id != null) {
    		// Update Client Log API
            return save_client_log(entity, null);
    	//}
//    	else {
//    		// Remote Control Client Log API
//    		return remote_control(entity);
//    	}
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
				if(!ClientLog.isStatusParameterValid(this.listStatus)) {
			    	return Utility.apiError(this, ApiStatusCode.INVALID_STATUS_PARAMETER);
				}
			} else {
				// by default, get only the new status incidents
				this.listStatus = ClientLog.NEW_STATUS;
			}
			
			if(this.incidentId == null) {
				if(this.listStatus.equalsIgnoreCase(ClientLog.ALL_STATUS)) {
			    	clientLogs= (List<ClientLog>)em.createNamedQuery("ClientLog.getAllWithApplicationId")
			    			.setParameter("applicationId", this.applicationId)
			    			.getResultList();
					log.info("clientLogs query 1: applicationId result set count = " + clientLogs.size());
				} else {
			    	clientLogs= (List<ClientLog>)em.createNamedQuery("ClientLog.getByStatusAndApplicationId")
							.setParameter("status", this.listStatus)
							.setParameter("applicationId", this.applicationId)
							.getResultList();
					log.info("clientLogs query 2: status/applicationId result set count = " + clientLogs.size());
				}
			} else {
				if(this.listStatus.equalsIgnoreCase(ClientLog.ALL_STATUS)) {
			    	clientLogs= (List<ClientLog>)em.createNamedQuery("ClientLog.getAllWithApplicationIdAndIncidentId")
			    			.setParameter("applicationId", this.applicationId)
			    			.setParameter("incidentId", this.incidentId)
			    			.getResultList();
					log.info("clientLogs query 3: applicationId/incidentId result set count = " + clientLogs.size());
				} else {
			    	clientLogs= (List<ClientLog>)em.createNamedQuery("ClientLog.getByStatusAndApplicationIdAndIncidentId")
							.setParameter("status", this.listStatus)
							.setParameter("applicationId", this.applicationId)
			    			.setParameter("incidentId", this.incidentId)
							.getResultList();
					log.info("clientLogs query 4: status/applicationId/incidentId result set count = " + clientLogs.size());
				}
			}
            
            for (ClientLog cl : clientLogs) {
            	JSONObject clientLogObj = ClientLog.getJson(cl, true);
            	if(clientLogObj == null) {
            		this.setStatus(Status.SERVER_ERROR_INTERNAL);
            		break;
            	}
                ja.put(clientLogObj);
            }
            json.put("clientLogs", ja);
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
			return Utility.apiError(this, ApiStatusCode.CLIENT_LOG_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more client logs have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
			em.close();
		} 
        
        JSONObject clientJsonObj = ClientLog.getJson(clientLog, apiStatus, false);
        if(clientJsonObj == null) {
        	this.setStatus(Status.SERVER_ERROR_INTERNAL);
        	clientJsonObj = new JSONObject();
        }
		return new JsonRepresentation(clientJsonObj);
    }

    private JsonRepresentation save_client_log(Representation entity, Application theApplication) {
        EntityManager em = EMF.get().createEntityManager();
        JSONObject jsonReturn = new JSONObject();

        ClientLog clientLog = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
    	User currentUser = Utility.getCurrentUser(getRequest());
		Incident owningIncident = null;
        try {
            clientLog = new ClientLog();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            Boolean isUpdate = false;
            String logName = null;
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
	            	logName = json.getString("logName");
	            	clientLog.setLogName(logName);
	            } else {
	            	return Utility.apiError(this, ApiStatusCode.LOG_NAME_IS_REQUIRED);
	            }
			}
			
			String message = null;
			if(!isUpdate && json.has("message")) {
				message = json.getString("message");
				clientLog.setMessage(message);
			}
			
			if(!isUpdate && json.has("stackBackTrace")) {
				List<Text> stackBackTraces = new ArrayList<Text>();
	        	JSONArray stackBackTracesJsonArray = json.getJSONArray("stackBackTrace");
				int arraySize = stackBackTracesJsonArray.length();
				log.info("stackBackTraces json array length = " + arraySize);
				for(int i=0; i<arraySize; i++) {
					Text stackBackTrace = new Text(stackBackTracesJsonArray.getString(i));
					stackBackTraces.add(stackBackTrace);
				}
				clientLog.setStackBackTraces(stackBackTraces);
			}
			
			if(!isUpdate && json.has("userId")) {
				clientLog.setUserId(json.getString("userId"));
			}
			
			if(!isUpdate && json.has("userName")) {
				clientLog.setUserName(json.getString("userName"));
			}
			
			if(!isUpdate) {
				if(json.has("localEndpoint")) {
					clientLog.setLocalEndpoint(json.getString("localEndpoint"));
				}
				if(json.has("remoteEndpoint")) {
					clientLog.setRemoteEndpoint(json.getString("remoteEndpoint"));
				}
				if((clientLog.getLocalEndpoint() != null && clientLog.getRemoteEndpoint() == null) ||
				   (clientLog.getLocalEndpoint() == null && clientLog.getRemoteEndpoint() != null)) {
					return Utility.apiError(this, ApiStatusCode.LOCAL_AND_REMOTE_ENDPOINTS_MUST_BE_SPECIFIED_TOGETHER);
				}
				log.info("Create Client Log: localEndpoint = " + clientLog.getLocalEndpoint());
				log.info("Create Client Log: remoteEndpoint = " + clientLog.getRemoteEndpoint());
			}
			
			String summary = null;
			if(!isUpdate && json.has("summary")) {
				summary = json.getString("summary");
				clientLog.setSummary(summary);
			}
			
			if(!isUpdate) {
				Date createdDate = null;
				if(json.has("date")) {
					String createdDateStr = json.getString("date");
					createdDate = GMT.stringToIsoDate(createdDateStr);
					if(createdDate == null) {
						return Utility.apiError(this, ApiStatusCode.INVALID_CREATED_DATE_PARAMETER);
					}
				} else {
					// default date/time is right now 
					createdDate = new Date();
				}
				clientLog.setCreatedGmtDate(createdDate);
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
						Date timestamp = null;
						String timestampStr = appActionJsonObj.getString("timestamp");
						
						// for rTeam backward compatibility
						// TODO: can remove this code after the rTeam 3.1 release
						if(!timestampStr.endsWith("Z")) {
							// this is the old format, not ISO 8601
							TimeZone tz = GMT.getTimeZone(RskyboxApplication.DEFAULT_LOCAL_TIME_ZONE);
							timestamp = GMT.convertToGmtDate(timestampStr, true, tz, RskyboxApplication.APP_ACTION_DATE_FORMAT);
						} else {
							timestamp = GMT.stringToIsoDate(timestampStr);
						}
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
			
			String incidentId = null;
			if(!isUpdate && json.has("incidentId")) {
				incidentId = json.getString("incidentId");
			}

			if(isUpdate) {
	            if(json.has("status")) {
	            	String status = json.getString("status").toLowerCase();
	            	if(ClientLog.isStatusValid(status)) {
	            		clientLog.setStatus(status);
	            	} else {
						return Utility.apiError(this, ApiStatusCode.INVALID_STATUS);
	            	}
	            }
			} else {
				clientLog.setApplicationId(this.applicationId);
            	
				// Default status to 'new'
				clientLog.setStatus(CrashDetect.NEW_STATUS);
				
				// set the activeThruGmtDate for auto archiving
				int daysUntilAutoArchive = theApplication.daysUntilAutoArchive();
				Date activeThruGmtDate = GMT.addDaysToDate(new Date(), daysUntilAutoArchive);
				clientLog.setActiveThruGmtDate(activeThruGmtDate);
				
				// find or create an incident that will 'own' this new clientLog
				String lep = clientLog.getLocalEndpoint() == null ? Incident.DEFAULT_ENDPOINT : clientLog.getLocalEndpoint();
				String rep = clientLog.getRemoteEndpoint() == null ? Incident.DEFAULT_ENDPOINT : clientLog.getRemoteEndpoint();
				owningIncident = Incident.fetchIncidentIncrementCount(logName, lep, rep, Incident.LOG_TAG, incidentId, theApplication, message, summary);
				if(owningIncident == null) {
					// assume problem was incident ID specified was not valid
					return Utility.apiError(this, ApiStatusCode.INCIDENT_NOT_FOUND);
				}
				clientLog.setIncidentId(owningIncident.getId());
			}
			
			if(owningIncident.getMaxEventCountReached()) {
				// log queue strategy: hold the first incident log forever. Once max cnt reached, replace the second oldest
				// So over time, the log queue will contain the first log and then the most recent X logs where X is max incident log cnt
				// TODO make this statistically based. When incident total cnt gets very large, only occasionally should new logs be inserted into queue
				log.info("log reached maximum event count -- deleting second oldest log");
				ClientLog.deleteSecondOldest(this.applicationId, owningIncident.getId());
			}
			em.persist(clientLog);
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
			log.severe("should never happen - two or more client logs have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
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
