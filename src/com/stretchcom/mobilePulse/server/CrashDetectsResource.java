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
import com.stretchcom.mobilePulse.models.CrashDetect;
import com.stretchcom.mobilePulse.models.User;

public class CrashDetectsResource extends ServerResource {
	private static final Logger log = Logger.getLogger(CrashDetectsResource.class.getName());
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
				log.info("CrashDetectResource() - decoded status = " + this.listStatus);
			} 
		}
    }
    // Handles 'Get Crash Detect Info API'
    // Handles 'Get List of Crash Detects API
    @Get("json")
    public JsonRepresentation get(Variant variant) {
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
        return save_crash_detect(entity);
    }

    // Handles 'Update Crash Detect API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
		if (this.id == null || this.id.length() == 0) {
			return Utility.apiError(ApiStatusCode.CRASH_DETECT_ID_REQUIRED);
		}
        return save_crash_detect(entity);
    }
    
    private JsonRepresentation index() {
        log.info("in index");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
        try {
            List<CrashDetect> crashDetects = new ArrayList<CrashDetect>();
            JSONArray ja = new JSONArray();
            
			if(this.listStatus != null) {
			    if(this.listStatus.equalsIgnoreCase(CrashDetect.NEW_STATUS) || this.listStatus.equalsIgnoreCase(CrashDetect.ARCHIVED_STATUS)){
			    	crashDetects= (List<CrashDetect>)em.createNamedQuery("CrashDetect.getByStatus")
							.setParameter("status", this.listStatus)
							.getResultList();
			    } else if(this.listStatus.equalsIgnoreCase(CrashDetect.ALL_STATUS)) {
			    	crashDetects= (List<CrashDetect>)em.createNamedQuery("CrashDetect.getAll").getResultList();
			    } else {
			    	return Utility.apiError(ApiStatusCode.INVALID_STATUS_PARAMETER);
			    }
			} else {
				// by default, only get 'new' feedback
				crashDetects= (List<CrashDetect>)em.createNamedQuery("CrashDetect.getByStatus")
						.setParameter("status", CrashDetect.NEW_STATUS)
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
		try {
			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(ApiStatusCode.CRASH_DETECT_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(ApiStatusCode.CRASH_DETECT_NOT_FOUND);
			}
    		crashDetect = (CrashDetect)em.createNamedQuery("CrashDetect.getByKey")
				.setParameter("key", key)
				.getSingleResult();
		} catch (NoResultException e) {
			log.info("Crash Detect not found");
			apiStatus = ApiStatusCode.CRASH_DETECT_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} 
        
        return new JsonRepresentation(getCrashDetectJson(crashDetect, apiStatus, false));
    }

    private JsonRepresentation save_crash_detect(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();

        CrashDetect crashDetect = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
        em.getTransaction().begin();
        try {
            crashDetect = new CrashDetect();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            Boolean isUpdate = false;
            if (id != null) {
                Key key;
    			try {
    				key = KeyFactory.stringToKey(this.id);
    			} catch (Exception e) {
    				log.info("ID provided cannot be converted to a Key");
    				return Utility.apiError(ApiStatusCode.CRASH_DETECT_NOT_FOUND);
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
			
			// TODO support a time zone passed in
			if(!isUpdate && json.has("date")) {
				String detectedDateStr = json.getString("date");
				
				if(detectedDateStr != null || detectedDateStr.trim().length() != 0) {
					TimeZone tz = GMT.getTimeZone(MobilePulseApplication.DEFAULT_LOCAL_TIME_ZONE);
					Date gmtDetectedDate = GMT.convertToGmtDate(detectedDateStr, true, tz);
					if(gmtDetectedDate == null) {
						log.info("invalid detected date format passed in");
						return Utility.apiError(ApiStatusCode.INVALID_DETECTED_DATE_PARAMETER);
					}
					crashDetect.setDetectedGmtDate(gmtDetectedDate);
				}
			}
			
			if(!isUpdate && json.has("instanceUrl")) {
				crashDetect.setInstanceUrl(json.getString("instanceUrl"));
			}
			
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
				// Default status to 'new'
				crashDetect.setStatus(CrashDetect.NEW_STATUS);
			}
            
            em.persist(crashDetect);
            em.getTransaction().commit();
            
            if(!isUpdate) User.sendNotifications("new crash detected");
        } catch (IOException e) {
            log.severe("error extracting JSON object from Post. exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (NoResultException e) {
			log.info("Crash Detect not found");
			apiStatus = ApiStatusCode.CRASH_DETECT_NOT_FOUND;
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
            	// TODO support time zones
            	if(detectedDate != null) {
            		TimeZone tz = GMT.getTimeZone(MobilePulseApplication.DEFAULT_LOCAL_TIME_ZONE);
            		String dateFormat = MobilePulseApplication.INFO_DATE_FORMAT;
            		if(isList) {dateFormat = MobilePulseApplication.LIST_DATE_FORMAT;}
            		json.put("date", GMT.convertToLocalDate(detectedDate, tz, dateFormat));
            	}
            	
            	json.put("userName", crashDetect.getUserName());
            	json.put("instanceUrl", crashDetect.getInstanceUrl());
            	
            	// TODO remove eventually, for backward compatibility before status field existed. If status not set, default to 'new'
            	String status = crashDetect.getStatus();
            	if(status == null || status.length() == 0) {status = "new";}
            	json.put("status", status);
            	
            	log.info("Crash Detect JSON object = " + crashDetect.toString());
        	}
        } catch (JSONException e) {
        	log.severe("UsersResrouce::getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
}
