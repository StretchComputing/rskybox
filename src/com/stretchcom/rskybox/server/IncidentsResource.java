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
import org.json.JSONString;
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
import com.stretchcom.rskybox.models.Incident;
import com.stretchcom.rskybox.models.Notification;
import com.stretchcom.rskybox.models.User;

public class IncidentsResource extends ServerResource {
	private static final Logger log = Logger.getLogger(IncidentsResource.class.getName());
	private String id;
	private String tag;
	private String applicationId;
    private String incidentStatus;
    private String remoteControl;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        this.id = (String) getRequest().getAttributes().get("id");
        this.applicationId = (String) getRequest().getAttributes().get("applicationId");
        this.remoteControl = (String) getRequest().getAttributes().get("remoteControl");
        
		Form form = getRequest().getResourceRef().getQueryAsForm();
		for (Parameter parameter : form) {
			log.info("parameter " + parameter.getName() + " = " + parameter.getValue());
			if(parameter.getName().equals("status"))  {
				this.incidentStatus = (String)parameter.getValue().toLowerCase();
				this.incidentStatus = Reference.decode(this.incidentStatus);
				log.info("IncidentResource() - decoded status = " + this.incidentStatus);
			} else if(parameter.getName().equals("tag"))  {
				this.tag = (String)parameter.getValue().toLowerCase();
				this.tag = Reference.decode(this.tag);
				log.info("IncidentResource() - decoded tag = " + this.tag);
			}
		}
    }

    // Handles 'Get Incident Info API'
    // Handles 'Get List of Incidents API
    @Get("json")
    public JsonRepresentation get(Variant variant) {
    	String appIdStatus = Application.verifyApplicationId(this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(this, appIdStatus);
    	}
    	
         JSONObject jsonReturn;

        log.info("in get for Incident resource");
        if (this.id != null) {
            // Get Incident Info API
        	log.info("in Get Incidents Info API");
        	return show();
        } else {
            // Get List of Incidents API
        	log.info("Get List of Incidents API");
        	return index();
        }
    }

    // Handles 'Create Incident API'
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("in post");
		if(this.applicationId == null) {return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);}
		Application application = Application.getApplicationWithId(this.applicationId);
		if(application == null) {
			return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
		}
    	
        return save_incident(entity, application);
    }

    // Handles 'Update Incident API'
    // Handles 'Remote Control Incident API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
    	String appIdStatus = Application.verifyApplicationId(this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(this, appIdStatus);
    	}
    	
		// incident ID is required
    	if (this.id == null || this.id.length() == 0) {
			return Utility.apiError(this, ApiStatusCode.INCIDENT_ID_REQUIRED);
		}
    	
    	if(this.remoteControl == null) {
    		// Update Incident API
            return save_incident(entity, null);
    	} else {
    		// Remote Control Incident API
    		return remote_control(entity);
    	}
    }
    
    private JsonRepresentation index() {
        log.info("in index");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
		List<Incident> incidents = null;
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
            
            /////////////////////////
            // Valid input parameters
            /////////////////////////
            if(this.incidentStatus != null) {
				if(Incident.isStatusValid(this.incidentStatus)) {
			    	return Utility.apiError(this, ApiStatusCode.INVALID_STATUS_PARAMETER);
				}
			} else {
				// default is to retrieve 
				this.incidentStatus = Incident.OPEN_STATUS;
			}
            if(this.tag != null) {
				if(Incident.isWellKnownTagValid(this.tag)) {
			    	return Utility.apiError(this, ApiStatusCode.INVALID_TAGS_PARAMETER);
				}
			}
            
			if(this.tag == null) {
			    if(this.incidentStatus.equalsIgnoreCase(Incident.ALL_STATUS)){
			    	incidents= (List<Incident>)em.createNamedQuery("Incident.getAllWithApplicationId")
			    			.setParameter("applicationId", this.applicationId)
			    			.getResultList();
			    } else {
			    	incidents= (List<Incident>)em.createNamedQuery("Incident.getByStatusAndApplicationId")
							.setParameter("status", this.incidentStatus)
							.setParameter("applicationId", this.applicationId)
							.getResultList();
			    } 
			} else {
				// by default, only get 'open' incidents
			    if(this.incidentStatus.equalsIgnoreCase(Incident.ALL_STATUS)){
			    	incidents= (List<Incident>)em.createNamedQuery("Incident.getAllWithApplicationIdAndTag")
			    			.setParameter("applicationId", this.applicationId)
			    			.setParameter("tag", this.tag)
			    			.getResultList();
			    } else {
			    	incidents= (List<Incident>)em.createNamedQuery("Incident.getByStatusAndApplicationIdAndTag")
							.setParameter("status", this.incidentStatus)
							.setParameter("applicationId", this.applicationId)
							.setParameter("tag", this.tag)
							.getResultList();
			    } 
			}
            
            for (Incident i : incidents) {
                ja.put(getIncidentJson(i, true));
            }
            json.put("incidents", ja);
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
		Incident incident = null;
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
				return Utility.apiError(this, ApiStatusCode.INCIDENT_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(this, ApiStatusCode.INCIDENT_NOT_FOUND);
			}
    		incident = (Incident)em.createNamedQuery("Incident.getByKey")
				.setParameter("key", key)
				.getSingleResult();
		} catch (NoResultException e) {
			return Utility.apiError(this, ApiStatusCode.INCIDENT_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more incidents have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} 
        
        return new JsonRepresentation(getIncidentJson(incident, apiStatus, false));
    }

    private JsonRepresentation save_incident(Representation entity, Application theApplication) {
        EntityManager em = EMF.get().createEntityManager();
        JSONObject jsonReturn = new JSONObject();

        Incident incident = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
    	User currentUser = Utility.getCurrentUser(getRequest());
        em.getTransaction().begin();
        try {
            incident = new Incident();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            Boolean isUpdate = false;
            Boolean severityChanged = false;
            Integer oldSeverity = null;
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
    				log.info("Incident ID provided cannot be converted to a Key");
    				return Utility.apiError(this, ApiStatusCode.INCIDENT_NOT_FOUND);
    			}
                incident = (Incident)em.createNamedQuery("Incident.getByKey").setParameter("key", key).getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            }
			
			if(!json.has("tags")) {
				List<String> tags = new ArrayList<String>();
	        	JSONArray tagsJsonArray = json.getJSONArray("tags");
				int arraySize = tagsJsonArray.length();
				log.info("tags json array length = " + arraySize);
				for(int i=0; i<arraySize; i++) {
					String tag = tagsJsonArray.getString(i);
					tags.add(tag);
				}
				int wktCount = Incident.getWellKnownTagCount(tags);
				if(isUpdate) {
					// well known tags not allowed in an update API call
					if(wktCount > 0) {
						return Utility.apiError(this, ApiStatusCode.INVALID_TAGS_PARAMETER);
					}
					incident.addToTags(tags);
				} else {
					if(wktCount > 1) {
						return Utility.apiError(this, ApiStatusCode.INVALID_TAGS_PARAMETER);
					} else if(wktCount == 0) {
						return Utility.apiError(this, ApiStatusCode.WELL_KNOWN_TAG_REQUIRED);
					}
					incident.setTags(tags);
				}
			}

            if(!isUpdate && json.has("name")) {
            	incident.setEventName(json.getString("name"));
            } else {
            	return Utility.apiError(this, ApiStatusCode.NAME_REQUIRED);
            }
			
			if(!isUpdate && json.has("summary")) {
				incident.setSummary(json.getString("summary"));
			}
			
			if(!isUpdate && json.has("message")) {
				incident.setMessage(json.getString("message"));
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
				incident.setCreatedGmtDate(createdDate);
			}
			
			if(isUpdate) {
	            if(json.has("status")) {
	            	String status = json.getString("status").toLowerCase();
	            	if(Incident.isStatusValid(status)) {
	            		incident.setStatus(status);
	            	} else {
						return Utility.apiError(this, ApiStatusCode.INVALID_STATUS_PARAMETER);
	            	}
	            }
			
	            if(json.has("severity")) {
	            	String severityStr = json.getString("severity").toLowerCase();
	            	
	            	Integer severity;
					try {
						severity = new Integer(severityStr);
					} catch (NumberFormatException e) {
						return Utility.apiError(this, ApiStatusCode.INVALID_SEVERITY_PARAMETER);

					}
	            	
	            	if(Incident.isSeverityValid(severity)) {
            			oldSeverity = incident.getSeverity();
	            		if(!oldSeverity.equals(severity)) {
	            			severityChanged = true;
	            		}
	            		incident.setSeverity(severity);
	            	} else {
	            		apiStatus = ApiStatusCode.INVALID_SEVERITY_PARAMETER;
	            	}
	            }
			} else {
				incident.setApplicationId(this.applicationId);
            	
				// Default status to 'open'
				incident.setStatus(Incident.OPEN_STATUS);
				
				// Default severity
				incident.setSeverity(Incident.LOW_SEVERITY);
				
				// set the activeThruGmtDate for auto closing
				int daysUntilAutoArchive = theApplication.daysUntilAutoArchive();
				Date activeThruGmtDate = GMT.addDaysToDate(new Date(), daysUntilAutoArchive);
				incident.setActiveThruGmtDate(activeThruGmtDate);
			}
			
            em.persist(incident);
            em.getTransaction().commit();
            
            if(!isUpdate) {
            	String message = incident.getEventName();
            	if(incident.getSummary() != null && incident.getSummary().length() > 0) {
            		message += " " + incident.getSummary();
            	}
            } else {
            	if(severityChanged) {
                	String theItemId = KeyFactory.keyToString(incident.getKey());
                	String severityMsg = "Severity changed from " + oldSeverity.toString() + " to " + incident.getSeverity().toString();
                	User.sendNotifications(this.applicationId, Notification.SEVERITY, severityMsg, theItemId);
            	}
            }
        	// ** I AM HERE **
            // return full incident details for both create and update APIs
            // ***************
        } catch (IOException e) {
            log.severe("error extracting JSON object from Post. exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (NoResultException e) {
        	return Utility.apiError(this, ApiStatusCode.INCIDENT_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more incidents have same key");
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
            
            ClientLogRemoteControl.update(this.applicationId, this.tag, mode);
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
            	if(createdDate != null) {
            		json.put("date", GMT.convertToIsoDate(createdDate));
            	}
            	json.put("userName", clientLog.getUserName());
            	json.put("instanceUrl", clientLog.getInstanceUrl());
            	json.put("logLevel", clientLog.getLogLevel());
            	json.put("logName", clientLog.getLogName());
            	json.put("message", clientLog.getMessage());
            	json.put("summary", clientLog.getSummary());
            	
            	JSONArray stackBackTracesJsonArray = new JSONArray();
            	List<String> stackBackTraces = clientLog.getStackBackTraces();
            	
            	//////////////////////////////////////////////////
            	// TODO - remove support of stackBackTrace string
            	//////////////////////////////////////////////////
            	if(stackBackTraces == null || stackBackTraces.size() == 0) {
        			log.info("returning legacy stackBackTrace");
            		stackBackTraces = new ArrayList<String>();
            		String stackBackTrace = clientLog.getStackBackTrace();
            		if(stackBackTrace != null && stackBackTrace.length() > 0) {
                		stackBackTraces.add(stackBackTrace);
            		}
            	}
            	//////////////////////////////////////////////////

            	for(String sbt: stackBackTraces) {
            		stackBackTracesJsonArray.put(sbt);
            	}
            	log.info("stackBackTraces # of parts = " + stackBackTraces.size());
            	json.put("stackBackTrace", stackBackTracesJsonArray);
                	
                ClientLogRemoteControl clrc = ClientLogRemoteControl.getEntity(this.applicationId, clientLog.getLogName());
                // if there is no clientLogRemoteControl, then mode must defaults to ACTIVE
                String logMode = ClientLogRemoteControl.ACITVE_MODE;
                if(clrc != null) {logMode = clrc.getMode();}
                json.put("logMode", logMode);
            	
            	if(!isList) {
                	JSONArray appActionsJsonArray = new JSONArray();
                	List<AppAction> appActions = clientLog.getAppActions();
                	for(AppAction aa : appActions) {
                		JSONObject appActionJsonObj = new JSONObject();
                		appActionJsonObj.put("description", aa.getDescription());
                		appActionJsonObj.put("timestamp", GMT.convertToIsoDate(aa.getTimestamp()));
                		if(aa.getDuration() != null) appActionJsonObj.put("duration", aa.getDuration());
                		appActionsJsonArray.put(appActionJsonObj);
                	}
                	if(appActions.size() > 0) {json.put("appActions", appActionsJsonArray);}
            	}
            	
            	// TODO remove eventually, for backward compatibility before status field existed. If status not set, default to 'new'
            	String status = clientLog.getStatus();
            	if(status == null || status.length() == 0) {status = "new";}
            	json.put("status", status);
            	json.put("appId", clientLog.getApplicationId());
        	}
        } catch (JSONException e) {
        	log.severe("UsersResrouce::getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
}
