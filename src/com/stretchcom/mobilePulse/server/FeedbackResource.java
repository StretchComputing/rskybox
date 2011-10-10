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

public class FeedbackResource extends ServerResource {
	private static final Logger log = Logger.getLogger(FeedbackResource.class.getName());
    private String id;
    private String listStatus;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        id = (String) getRequest().getAttributes().get("id");
        
		Form form = getRequest().getResourceRef().getQueryAsForm();
		for (Parameter parameter : form) {
			log.info("parameter " + parameter.getName() + " = " + parameter.getValue());
			if(parameter.getName().equals("status"))  {
				this.listStatus = (String)parameter.getValue().toLowerCase();
				this.listStatus = Reference.decode(this.listStatus);
				log.info("FeedbackResource() - decoded status = " + this.listStatus);
			} 
		}
    }

    // Handles 'Get Feedback Info API'
    // Handles 'Get List of Feedback API
    @Get("json")
    public JsonRepresentation get(Variant variant) {
        if (id != null) {
            // Get Feedback Info API
        	log.info("in Get Feedback Info API");
        	return show(id);
        } else {
            // Get List of Feedback API
        	log.info("Get List of Feedbacks API");
        	return index();
        }
    }
    
    // Handles 'Create a new feedback' API
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("UserResource in post");
        return save_feedback(entity);
    }

    // Handles 'Update Feedback API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("UserResource in put");
		if (this.id == null || this.id.length() == 0) {
			return Utility.apiError(ApiStatusCode.FEEDBACK_ID_REQUIRED);
		}
        return save_feedback(entity);
    }

    private JsonRepresentation save_feedback(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();

        Feedback feedback = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
        em.getTransaction().begin();
        try {
            feedback = new Feedback();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            Boolean isUpdate = false;
            if (id != null) {
                Key key = KeyFactory.stringToKey(id);
                feedback = (Feedback)em.createNamedQuery("Feedback.getByKey")
                    	.setParameter("key", key)
                    	.getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            }
            
			if(!isUpdate && json.has("voice")) {
				feedback.setVoiceBase64(json.getString("voice"));
				log.info("stored voice value = " + feedback.getVoiceBase64());
			} else {
				log.info("no JSON voice field found");
			}
			
			if(!isUpdate && json.has("userName")) {
				feedback.setUserName(json.getString("userName"));
			}
            
			// TODO support a time zone passed in
			if(!isUpdate && json.has("date")) {
				String recordedDateStr = json.getString("date");
				
				if(recordedDateStr != null || recordedDateStr.trim().length() != 0) {
					TimeZone tz = GMT.getTimeZone(MobilePulseApplication.DEFAULT_LOCAL_TIME_ZONE);
					Date gmtRecordedDate = GMT.convertToGmtDate(recordedDateStr, true, tz);
					if(gmtRecordedDate == null) {
						log.info("invalid recorded date format passed in");
						return Utility.apiError(ApiStatusCode.INVALID_RECORDED_DATE_PARAMETER);
					}
					feedback.setRecordedGmtDate(gmtRecordedDate);
				}
			}
			
			if(!isUpdate && json.has("instanceUrl")) {
				feedback.setInstanceUrl(json.getString("instanceUrl"));
			}
			
			if(isUpdate) {
	            if(json.has("status")) {
	            	String status = json.getString("status").toLowerCase();
	            	if(feedback.isStatusValid(status)) {
	                    feedback.setStatus(status);
	            	} else {
						log.info("invalid status = " + status);
						return Utility.apiError(ApiStatusCode.INVALID_STATUS);
	            	}
	            }
			} else {
				// creating a feedback so default status to 'new'
				feedback.setStatus(Feedback.NEW_STATUS);
			}

            em.persist(feedback);
            em.getTransaction().commit();
        } catch (IOException e) {
            log.severe("error extracting JSON object from Post. exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        
        return new JsonRepresentation(getFeedbackJson(feedback, apiStatus));
    }

    private JsonRepresentation show(String id) {
        log.info("UserResource in show()");
        EntityManager em = EMF.get().createEntityManager();

		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
		Feedback feedback = null;
		try {
			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(ApiStatusCode.FEEDBACK_ID_REQUIRED);
			}
			
			Key feedbackKey = KeyFactory.stringToKey(this.id);
    		feedback = (Feedback)em.createNamedQuery("Feedback.getByKey")
				.setParameter("key", feedbackKey)
				.getSingleResult();
		} catch (NoResultException e) {
			log.info("Feedback not found");
			apiStatus = ApiStatusCode.FEEDBACK_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} 
        
        return new JsonRepresentation(getFeedbackJson(feedback, apiStatus));
    }
    
    private JsonRepresentation index() {
        log.info("UserResource in index");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
        try {
			List<Feedback> feedbacks = null;
            JSONArray ja = new JSONArray();
            
			if(this.listStatus != null) {
			    if(this.listStatus.equalsIgnoreCase(Feedback.NEW_STATUS) || this.listStatus.equalsIgnoreCase(Feedback.ARCHIVED_STATUS)){
					feedbacks= (List<Feedback>)em.createNamedQuery("Feedback.getByStatus")
							.setParameter("status", this.listStatus)
							.getResultList();
			    } else if(this.listStatus.equalsIgnoreCase(Feedback.ALL_STATUS)) {
					feedbacks= (List<Feedback>)em.createNamedQuery("Feedback.getAll").getResultList();
			    } else {
			    	return Utility.apiError(ApiStatusCode.INVALID_STATUS_PARAMETER);
			    }
			} else {
				// by default, only get 'new' feedback
				feedbacks= (List<Feedback>)em.createNamedQuery("Feedback.getByStatus")
						.setParameter("status", Feedback.NEW_STATUS)
						.getResultList();
			}
            
            for (Feedback fb : feedbacks) {
                ja.put(getFeedbackJson(fb));
            }
            json.put("feedback", ja);
            json.put("apiStatus", apiStatus);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
        	e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return new JsonRepresentation(json);
    }
    
    private JSONObject getFeedbackJson(Feedback feedback) {
    	return getFeedbackJson(feedback, null);
    }

    private JSONObject getFeedbackJson(Feedback feedback, String theApiStatus) {
        JSONObject json = new JSONObject();

        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS))) {
        		json.put("id", KeyFactory.keyToString(feedback.getKey()));
    			
            	Date recordedDate = feedback.getRecordedGmtDate();
            	// TODO support time zones
            	if(recordedDate != null) {
            		TimeZone tz = GMT.getTimeZone(MobilePulseApplication.DEFAULT_LOCAL_TIME_ZONE);
            		json.put("date", GMT.convertToLocalDate(recordedDate, tz, MobilePulseApplication.INFO_DATE_FORMAT));
            	}
            	
            	json.put("userName", feedback.getUserName());
            	json.put("instanceUrl", feedback.getInstanceUrl());
        	}
        	log.info("Feedback JSON object = " + feedback.toString());
        } catch (JSONException e) {
        	log.severe("UsersResrouce::getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
}
