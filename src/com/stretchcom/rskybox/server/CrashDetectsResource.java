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
import com.stretchcom.rskybox.models.CrashDetect;
import com.stretchcom.rskybox.models.Incident;
import com.stretchcom.rskybox.models.Notification;
import com.stretchcom.rskybox.models.User;

public class CrashDetectsResource extends ServerResource {
	private static final Logger log = Logger.getLogger(CrashDetectsResource.class.getName());
	private String id;
	private String applicationId;
    private String listStatus;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        this.id = (String) getRequest().getAttributes().get("id");
        this.applicationId = (String) getRequest().getAttributes().get("applicationId");
        
		Form form = getRequest().getResourceRef().getQueryAsForm();
		for (Parameter parameter : form) {
			log.info("parameter " + parameter.getName() + " = " + parameter.getValue());
			if(parameter.getName().equals("status"))  {
				this.listStatus = (String)parameter.getValue().toLowerCase();
				this.listStatus = Reference.decode(this.listStatus);
				log.info("CrashDetectResource() - decoded status = " + this.listStatus);
			} 
		}
    }
    // Handles 'Get Crash Detect Info API'
    // Handles 'Get List of Crash Detects API
    @Get("json")
    public JsonRepresentation get(Variant variant) {
    	String appIdStatus = Application.verifyApplicationId(this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(this, appIdStatus);
    	}
    	
        if (id != null) {
            // Get Crash Detect Info API
        	log.info("in Get User Info API");
        	return show();
        } else {
            // Get List of Crash Detects API
        	log.info("Get List of Users API");
        	return index();
        }
    }

    // Handles 'Create Crash Detect API'
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("in post");
		if(this.applicationId == null) {return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);}
		Application application = Application.getApplicationWithId(this.applicationId);
		if(application == null) {
			return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
		}
    	
        return save_crash_detect(entity, application);
    }

    // Handles 'Update Crash Detect API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
    	String appIdStatus = Application.verifyApplicationId(this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(this, appIdStatus);
    	}
    	
		if (this.id == null || this.id.length() == 0) {
			return Utility.apiError(this, ApiStatusCode.CRASH_DETECT_ID_REQUIRED);
		}
        return save_crash_detect(entity, null);
    }
    
    private JsonRepresentation index() {
        log.info("in index");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
    	User currentUser = Utility.getCurrentUser(getRequest());
        try {
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	AppMember appMember = AppMember.getAppMember(this.applicationId, KeyFactory.keyToString(currentUser.getKey()));
        	if(appMember == null) {
				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
        	}

        	List<CrashDetect> crashDetects = new ArrayList<CrashDetect>();
            JSONArray ja = new JSONArray();
            
			if(this.listStatus != null) {
			    if(this.listStatus.equalsIgnoreCase(CrashDetect.NEW_STATUS) || this.listStatus.equalsIgnoreCase(CrashDetect.ARCHIVED_STATUS)){
			    	crashDetects= (List<CrashDetect>)em.createNamedQuery("CrashDetect.getByStatusAndApplicationId")
							.setParameter("status", this.listStatus)
							.setParameter("applicationId", this.applicationId)
							.getResultList();
			    } else if(this.listStatus.equalsIgnoreCase(CrashDetect.ALL_STATUS)) {
			    	crashDetects= (List<CrashDetect>)em.createNamedQuery("CrashDetect.getAllWithApplicationId")
			    			.setParameter("applicationId", this.applicationId)
			    			.getResultList();
			    } else {
			    	return Utility.apiError(this, ApiStatusCode.INVALID_STATUS_PARAMETER);
			    }
			} else {
				// by default, only get 'new' crashDetects
				crashDetects= (List<CrashDetect>)em.createNamedQuery("CrashDetect.getByStatusAndApplicationId")
						.setParameter("status", CrashDetect.NEW_STATUS)
						.setParameter("applicationId", this.applicationId)
						.getResultList();
			}
            
            for (CrashDetect cd : crashDetects) {
                ja.put(getCrashDetectJson(cd, true));
            }
            json.put("crashDetects", ja);
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
		CrashDetect crashDetect = null;
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
				return Utility.apiError(this, ApiStatusCode.CRASH_DETECT_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(this, ApiStatusCode.CRASH_DETECT_NOT_FOUND);
			}
    		crashDetect = (CrashDetect)em.createNamedQuery("CrashDetect.getByKey")
				.setParameter("key", key)
				.getSingleResult();
		} catch (NoResultException e) {
			return Utility.apiError(this, ApiStatusCode.CRASH_DETECT_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} 
        
        return new JsonRepresentation(getCrashDetectJson(crashDetect, apiStatus, false));
    }

    private JsonRepresentation save_crash_detect(Representation entity, Application theApplication) {
        EntityManager em = EMF.get().createEntityManager();

        CrashDetect crashDetect = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
    	User currentUser = Utility.getCurrentUser(getRequest());
        em.getTransaction().begin();
        try {
            crashDetect = new CrashDetect();
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
    				return Utility.apiError(this, ApiStatusCode.CRASH_DETECT_NOT_FOUND);
    			}
                crashDetect = (CrashDetect) em.createNamedQuery("CrashDetect.getByKey").setParameter("key", key).getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            }
			
			if(!isUpdate && json.has("summary")) {
				crashDetect.setSummary(json.getString("summary"));
			}
			
			if(!isUpdate && json.has("userName")) {
				crashDetect.setUserName(json.getString("userName"));
			}
			
			if(json.has("stackData")) {
				crashDetect.setStackDataBase64(json.getString("stackData"));
			}
			
			Date gmtDetectedDate = null;
			if(!isUpdate && json.has("date")) {
				String detectedDateStr = json.getString("date");
				if(detectedDateStr != null && detectedDateStr.trim().length() != 0) {
					// for rTeam backward compatibility
					// TODO: can remove this code after the rTeam 3.1 release
					if(!detectedDateStr.endsWith("Z")) {
						// this is the old format, not ISO 8601
						TimeZone tz = GMT.getTimeZone(RskyboxApplication.DEFAULT_LOCAL_TIME_ZONE);
						gmtDetectedDate = GMT.convertToGmtDate(detectedDateStr, true, tz);
					} else {
						gmtDetectedDate = GMT.stringToIsoDate(detectedDateStr);
					}
					if(gmtDetectedDate == null) {
						return Utility.apiError(this, ApiStatusCode.INVALID_DETECTED_DATE_PARAMETER);
					}
				}
			} else {
				// default date/time is right now 
				gmtDetectedDate = new Date();
			}
			crashDetect.setDetectedGmtDate(gmtDetectedDate);
			
			if(!isUpdate && json.has("instanceUrl")) {
				crashDetect.setInstanceUrl(json.getString("instanceUrl"));
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
				crashDetect.createAppActions(appActions);
			}
			
			String incidentId = null;
			if(!isUpdate && json.has("incidentId")) {
				incidentId = json.getString("incidentId");
			}
			
			Incident owningIncident = null;
			if(isUpdate) {
	            if(json.has("status")) {
	            	String status = json.getString("status").toLowerCase();
	            	if(crashDetect.isStatusValid(status)) {
	            		crashDetect.setStatus(status);
	            	} else {
	            		apiStatus = ApiStatusCode.INVALID_STATUS;
	            	}
	            }
			} else {
				crashDetect.setApplicationId(this.applicationId);

				// Default status to 'new'
				crashDetect.setStatus(CrashDetect.NEW_STATUS);
				crashDetect.setApplicationId(this.applicationId);
				
				// set the activeThruGmtDate for auto archiving
				int daysUntilAutoArchive = theApplication.daysUntilAutoArchive();
				Date activeThruGmtDate = GMT.addDaysToDate(new Date(), daysUntilAutoArchive);
				crashDetect.setActiveThruGmtDate(activeThruGmtDate);
				
				// find or create an incident that will 'own' this new crashDetect
				// TODO something better for an eventName than the current date
				Date now = new Date();
				
				owningIncident = Incident.fetchIncidentIncrementCount(now.toString(), Incident.CRASH_TAG, incidentId, theApplication, "new Crash Detect");
				crashDetect.setIncidentId(owningIncident.getId());
			}
            
            em.persist(crashDetect);
            em.getTransaction().commit();
            
            if(!isUpdate) {
            	User.sendNotifications(this.applicationId, Notification.CRASH, crashDetect.getSummary(), owningIncident.getId());
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
        	return Utility.apiError(this, ApiStatusCode.CRASH_DETECT_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        
        return new JsonRepresentation(getCrashDetectJson(crashDetect, apiStatus, false));
    }
    
    private JSONObject getCrashDetectJson(CrashDetect crashDetect, Boolean isList) {
    	return getCrashDetectJson(crashDetect, null, isList);
    }

    private JSONObject getCrashDetectJson(CrashDetect crashDetect, String theApiStatus, Boolean isList) {
        JSONObject json = new JSONObject();

        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(crashDetect != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
        		json.put("id", KeyFactory.keyToString(crashDetect.getKey()));
        		json.put("summary", crashDetect.getSummary());
    			
            	Date detectedDate = crashDetect.getDetectedGmtDate();
            	if(detectedDate != null) {
            		json.put("date", GMT.convertToIsoDate(detectedDate));
            	}
            	
            	json.put("userName", crashDetect.getUserName());
            	json.put("instanceUrl", crashDetect.getInstanceUrl());
            	
            	if(!isList) {
                	JSONArray appActionsJsonArray = new JSONArray();
                	List<AppAction> appActions = crashDetect.getAppActions();
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
            	String status = crashDetect.getStatus();
            	if(status == null || status.length() == 0) {status = "new";}
            	json.put("status", status);
            	json.put("appId", crashDetect.getApplicationId());
        	}
        } catch (JSONException e) {
        	log.severe("getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
}
