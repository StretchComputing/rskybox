package com.stretchcom.sandbox.server;

import java.io.IOException;
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
    private String feedbackId;
    private String listStatus;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        feedbackId = (String) getRequest().getAttributes().get("id");
        
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
         JSONObject jsonReturn;

        log.info("in get for Feedback resource");
        if (feedbackId != null) {
            // Get Feedback Info API
        	log.info("in Get Feedback Info API");
        	jsonReturn = getFeedbackInfoJson(feedbackId);
        } else {
            // Get List of Feedback API
        	log.info("Get List of Feedbacks API");
        	jsonReturn = getListOfFeedbacksJson();
        }
        
        return new JsonRepresentation(jsonReturn);
    }
    
    // Handles 'Update Feedback API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put for Feedback resource");
        return new JsonRepresentation(updateFeedback(entity));
    }

    // Handles 'Create a new feedback' API
    @Post("json")
    public JsonRepresentation createFeedback(Representation entity) {
    	log.info("createFeedback(@Post) entered ..... ");
        JSONObject jsonReturn = new JSONObject();
		EntityManager em = EMF.get().createEntityManager();
		
		String apiStatus = ApiStatusCode.SUCCESS;
		Feedback feedback = new Feedback();
		this.setStatus(Status.SUCCESS_CREATED);
		em.getTransaction().begin();
        try {
			JsonRepresentation jsonRep = new JsonRepresentation(entity);
			log.info("jsonRep = " + jsonRep.toString());
			JSONObject json = jsonRep.getJsonObject();
			
			if(json.has("voice")) {
				feedback.setVoiceBase64(json.getString("voice"));
				//feedback.setVoiceBase64("this is not voice data");
				log.info("stored voice value = " + feedback.getVoiceBase64());
			} else {
				log.info("no JSON voice field found");
			}
			
			if(json.has("userName")) {
				feedback.setUserName(json.getString("userName"));
			}
			
			// TODO support a time zone passed in
			if(json.has("date")) {
				String recordedDateStr = json.getString("date");
				
				if(recordedDateStr != null || recordedDateStr.trim().length() != 0) {
					TimeZone tz = GMT.getTimeZone(MobilePulseApplication.DEFAULT_LOCAL_TIME_ZONE);
					Date gmtRecordedDate = GMT.convertToGmtDate(recordedDateStr, true, tz);
					if(gmtRecordedDate == null) {
						log.info("invalid recorded date format passed in");
						apiStatus = ApiStatusCode.INVALID_RECORDED_DATE_PARAMETER;
						jsonReturn.put("apiStatus", apiStatus);
						return new JsonRepresentation(jsonReturn);
					}
					feedback.setRecordedGmtDate(gmtRecordedDate);
				}
			}
			
			if(json.has("instanceUrl")) {
				feedback.setInstanceUrl(json.getString("instanceUrl"));
			}
			
			// Default status to 'new'
			feedback.setStatus(Feedback.NEW_STATUS);
		    
			em.persist(feedback);
			em.getTransaction().commit();
			
			String keyWebStr = KeyFactory.keyToString(feedback.getKey());
			log.info("feedback with key " + keyWebStr + " created successfully");

			// TODO URL should be filtered to have only legal characters
			String baseUri = this.getRequest().getHostRef().getIdentifier();
			this.getResponse().setLocationRef(baseUri + "/");

			jsonReturn.put("id", keyWebStr);
		} catch (IOException e) {
			log.severe("error extracting JSON object from Post");
			e.printStackTrace();
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} catch (JSONException e) {
			log.severe("error converting json representation into a JSON object");
			e.printStackTrace();
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
			log.severe("error creating JSON return object");
			e.printStackTrace();
		}
		return new JsonRepresentation(jsonReturn);
    }
    
    private JSONObject getFeedbackInfoJson(String theFeedbackId) {
       	EntityManager em = EMF.get().createEntityManager();
    	JSONObject jsonReturn = new JSONObject();
    	
		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
		try {
			if (this.feedbackId == null || this.feedbackId.length() == 0) {
				apiStatus = ApiStatusCode.FEEDBACK_ID_REQUIRED;
				jsonReturn.put("apiStatus", apiStatus);
				return jsonReturn;
			}
			
			Key feedbackKey = KeyFactory.stringToKey(this.feedbackId);
    		Feedback feedback = null;
    		feedback = (Feedback)em.createNamedQuery("Feedback.getByKey")
				.setParameter("key", feedbackKey)
				.getSingleResult();

    		jsonReturn.put("id", KeyFactory.keyToString(feedback.getKey()));
			
        	Date recordedDate = feedback.getRecordedGmtDate();
        	// TODO support time zones
        	if(recordedDate != null) {
        		TimeZone tz = GMT.getTimeZone(MobilePulseApplication.DEFAULT_LOCAL_TIME_ZONE);
        		jsonReturn.put("date", GMT.convertToLocalDate(recordedDate, tz, MobilePulseApplication.INFO_DATE_FORMAT));
        	}
        	
        	jsonReturn.put("userName", feedback.getUserName());
        	jsonReturn.put("instanceUrl", feedback.getInstanceUrl());
        	
        	// TODO remove eventually, for backward compatibility before status field existed. If status not set, default to 'new'
        	String status = feedback.getStatus();
        	if(status == null || status.length() == 0) {status = "new";}
        	jsonReturn.put("status", status);
        	
            log.info("JSON return object built successfully");	
		} catch (JSONException e) {
			log.severe("error converting json representation into a JSON object");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
			e.printStackTrace();
		} catch (NoResultException e) {
			// feedback ID passed in is not valid
			log.info("Feedback ID not found");
			apiStatus = ApiStatusCode.FEEDBACK_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more feedback have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} 
    	
		try {
			jsonReturn.put("apiStatus", apiStatus);
		} catch (JSONException e) {
			log.severe("error converting json representation into a JSON object");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
			e.printStackTrace();
		}

		return jsonReturn;
    }
    
    private JSONObject getListOfFeedbacksJson() {
       	EntityManager em = EMF.get().createEntityManager();
    	JSONObject jsonReturn = new JSONObject();
    	
		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
		try {
			List<Feedback> feedbacks = null;
			if(this.listStatus != null) {
			    if(this.listStatus.equalsIgnoreCase(Feedback.NEW_STATUS) || this.listStatus.equalsIgnoreCase(Feedback.ARCHIVED_STATUS)){
					feedbacks= (List<Feedback>)em.createNamedQuery("Feedback.getByStatus")
							.setParameter("status", this.listStatus)
							.getResultList();
			    } else if(this.listStatus.equalsIgnoreCase(Feedback.ALL_STATUS)) {
					feedbacks= (List<Feedback>)em.createNamedQuery("Feedback.getAll").getResultList();
			    } else {
					apiStatus = ApiStatusCode.INVALID_STATUS_PARAMETER;
					jsonReturn.put("apiStatus", apiStatus);
					return jsonReturn;
			    }
			} else {
				// by default, only get 'new' feedback
				feedbacks= (List<Feedback>)em.createNamedQuery("Feedback.getByStatus")
						.setParameter("status", Feedback.NEW_STATUS)
						.getResultList();
			}

			JSONArray feedbackJsonArray = new JSONArray();
			for (Feedback fb : feedbacks) {
				JSONObject feedbackJsonObj = new JSONObject();
				
				feedbackJsonObj.put("id", KeyFactory.keyToString(fb.getKey()));
				
            	Date recordedDate = fb.getRecordedGmtDate();
            	// TODO support time zones
            	if(recordedDate != null) {
            		TimeZone tz = GMT.getTimeZone(MobilePulseApplication.DEFAULT_LOCAL_TIME_ZONE);
            		feedbackJsonObj.put("date", GMT.convertToLocalDate(recordedDate, tz, MobilePulseApplication.LIST_DATE_FORMAT));
            	}
            	
            	feedbackJsonObj.put("userName", fb.getUserName());
            	feedbackJsonObj.put("instanceUrl", fb.getInstanceUrl());
            	
            	// TODO remove eventually, for backward compatibility before status field existed. If status not set, default to 'new'
            	String status = fb.getStatus();
            	if(status == null || status.length() == 0) {status = "new";}
            	feedbackJsonObj.put("status", status);
				
				feedbackJsonArray.put(feedbackJsonObj);
			}
			jsonReturn.put("feedback", feedbackJsonArray);
		} catch (JSONException e) {
			log.severe("error converting json representation into a JSON object");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
			e.printStackTrace();
		} catch (Exception e) {
			log.severe("getListOfFeedbacksJson(): exception = " + e.getMessage());
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
			e.printStackTrace();
		}
    	
		try {
			jsonReturn.put("apiStatus", apiStatus);
		} catch (JSONException e) {
			log.severe("error converting json representation into a JSON object");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
			e.printStackTrace();
		}

		return jsonReturn;
    }


    private JSONObject updateFeedback(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();
    	JSONObject jsonReturn = new JSONObject();
    	
		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);

		Feedback feedback = null;
        em.getTransaction().begin();
        try {
			if (this.feedbackId == null || this.feedbackId.length() == 0) {
				apiStatus = ApiStatusCode.FEEDBACK_ID_REQUIRED;
				jsonReturn.put("apiStatus", apiStatus);
				return jsonReturn;
			}
			
            feedback = new Feedback();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            if (this.feedbackId != null) {
                Key key = KeyFactory.stringToKey(this.feedbackId);
                feedback = (Feedback)em.createNamedQuery("Feedback.getByKey")
                	.setParameter("key", key)
                	.getSingleResult();
            }
            if(json.has("status")) {
            	String status = json.getString("status").toLowerCase();
            	if(feedback.isStatusValid(status)) {
                    feedback.setStatus(status);
            	} else {
            		apiStatus = ApiStatusCode.INVALID_STATUS;
            	}
            }
            em.persist(feedback);
            em.getTransaction().commit();
        } catch (IOException e) {
            log.severe("error extracting JSON object from Post");
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (NoResultException e) {
			// feedback ID passed in is not valid
			log.info("Feedback ID not found");
			apiStatus = ApiStatusCode.FEEDBACK_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more feedback have same key");
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
			log.severe("error converting json representation into a JSON object");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
			e.printStackTrace();
		}

		return jsonReturn;
    }

}
