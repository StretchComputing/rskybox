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
import com.google.appengine.api.datastore.Text;

public class ClientLogResource extends ServerResource {
	private static final Logger log = Logger.getLogger(ClientLogResource.class.getName());
	private String clientLogId;
    private String listStatus;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        clientLogId = (String) getRequest().getAttributes().get("id");
        
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

    @Get("json")
    public JsonRepresentation get(Variant variant) {
         JSONObject jsonReturn;

        log.info("in get for Crash Detect resource");
        if (this.clientLogId != null) {
            // Get Client Log Info API
        	log.info("in Get Feedback Info API");
        	jsonReturn = getClientLogInfoJson(this.clientLogId);
        } else {
            // Get List of Client Logs API
        	log.info("Get List of Feedbacks API");
        	jsonReturn = getListOfClientLogsJson();
        }
        
        return new JsonRepresentation(jsonReturn);
    }
    
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put for Client Log resource");
        return new JsonRepresentation(updateClientLog(entity));
    }

    // Handles 'Create a new crash detect' API
    @Post("json")
    public JsonRepresentation createClientLog(Representation entity) {
    	log.info("createClientLog(@Post) entered ..... ");
        JSONObject jsonReturn = new JSONObject();
		EntityManager em = EMF.get().createEntityManager();
		
		String apiStatus = ApiStatusCode.SUCCESS;
		ClientLog clientLog = new ClientLog();
		this.setStatus(Status.SUCCESS_CREATED);
		em.getTransaction().begin();
        try {
			JsonRepresentation jsonRep = new JsonRepresentation(entity);
			log.info("jsonRep = " + jsonRep.toString());
			JSONObject json = jsonRep.getJsonObject();

			if(json.has("logLevel")) {
				String logLevel = json.getString("logLevel").toLowerCase();
				clientLog.setLogLevel(logLevel);
				if(!clientLog.isLogLevelValid(logLevel)) {
					apiStatus = ApiStatusCode.INVALID_LOG_LEVEL;
					jsonReturn.put("apiStatus", apiStatus);
					return new JsonRepresentation(jsonReturn);
				}
			} else {
				// default to error
				clientLog.setLogLevel(ClientLog.ERROR_LOG_LEVEL);
			}
			
			if(json.has("message")) {
				clientLog.setMessage(json.getString("message"));
			}
			
			if(json.has("stackBackTrace")) {
				clientLog.setStackBackTrace(json.getString("stackBackTrace"));
			}
			
			if(json.has("userName")) {
				clientLog.setUserName(json.getString("userName"));
			}
			
			if(json.has("instanceUrl")) {
				clientLog.setInstanceUrl(json.getString("instanceUrl"));
			}
			
			// Default status to 'new'
			clientLog.setStatus(CrashDetect.NEW_STATUS);
			
			// Default created date is today
			clientLog.setCreatedGmtDate(new Date());

			em.persist(clientLog);
			em.getTransaction().commit();
			
			String keyWebStr = KeyFactory.keyToString(clientLog.getKey());
			log.info("client log with key " + keyWebStr + " created successfully");

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
    
    private JSONObject getClientLogInfoJson(String theClientLogId) {
       	EntityManager em = EMF.get().createEntityManager();
    	JSONObject jsonReturn = new JSONObject();
    	
		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
		try {
			if (theClientLogId == null || theClientLogId.length() == 0) {
				apiStatus = ApiStatusCode.CLIENT_LOG_ID_REQUIRED;
				jsonReturn.put("apiStatus", apiStatus);
				return jsonReturn;
			}
			
			Key clientLogKey = KeyFactory.stringToKey(theClientLogId);
    		ClientLog clientLog = null;
    		clientLog = (ClientLog)em.createNamedQuery("ClientLog.getByKey")
				.setParameter("key", clientLogKey)
				.getSingleResult();

    		jsonReturn.put("id", KeyFactory.keyToString(clientLog.getKey()));
			
        	Date createdDate = clientLog.getCreatedGmtDate();
        	// TODO support time zones
        	if(createdDate != null) {
        		TimeZone tz = GMT.getTimeZone(MobilePulseApplication.DEFAULT_LOCAL_TIME_ZONE);
        		jsonReturn.put("date", GMT.convertToLocalDate(createdDate, tz, MobilePulseApplication.INFO_DATE_FORMAT));
        	}
        	jsonReturn.put("userName", clientLog.getUserName());
        	jsonReturn.put("instanceUrl", clientLog.getInstanceUrl());
        	jsonReturn.put("logLevel", clientLog.getLogLevel());
        	jsonReturn.put("message", clientLog.getMessage());
        	jsonReturn.put("stackBackTrace", clientLog.getStackBackTrace());
        	
        	// TODO remove eventually, for backward compatibility before status field existed. If status not set, default to 'new'
        	String status = clientLog.getStatus();
        	if(status == null || status.length() == 0) {status = "new";}
        	jsonReturn.put("status", status);
        	
            log.info("JSON return object built successfully");	
		} catch (JSONException e) {
			log.severe("error converting json representation into a JSON object");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} catch (NoResultException e) {
			// feedback ID passed in is not valid
			log.info("Client Log not found");
			apiStatus = ApiStatusCode.CLIENT_LOG_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more client logs have same key");
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
    
    private JSONObject getListOfClientLogsJson() {
       	EntityManager em = EMF.get().createEntityManager();
    	JSONObject jsonReturn = new JSONObject();
    	
		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
		try {
			List<ClientLog> clientLogs = null;
			if(this.listStatus != null) {
			    if(this.listStatus.equalsIgnoreCase(ClientLog.NEW_STATUS) || this.listStatus.equalsIgnoreCase(ClientLog.ARCHIVED_STATUS)){
			    	clientLogs= (List<ClientLog>)em.createNamedQuery("ClientLog.getByStatus")
							.setParameter("status", this.listStatus)
							.getResultList();
			    } else if(this.listStatus.equalsIgnoreCase(ClientLog.ALL_STATUS)) {
			    	clientLogs= (List<ClientLog>)em.createNamedQuery("ClientLog.getAll").getResultList();
			    } else {
					apiStatus = ApiStatusCode.INVALID_STATUS_PARAMETER;
					jsonReturn.put("apiStatus", apiStatus);
					return jsonReturn;
			    }
			} else {
				// by default, only get 'new' feedback
				clientLogs= (List<ClientLog>)em.createNamedQuery("ClientLog.getByStatus")
						.setParameter("status", ClientLog.NEW_STATUS)
						.getResultList();
			}

			JSONArray clientLogJsonArray = new JSONArray();
			for (ClientLog cl : clientLogs) {
				JSONObject clientLogJsonObj = new JSONObject();
				
				clientLogJsonObj.put("id", KeyFactory.keyToString(cl.getKey()));
				
            	Date createdDate = cl.getCreatedGmtDate();
            	// TODO support time zones
            	if(createdDate != null) {
            		TimeZone tz = GMT.getTimeZone(MobilePulseApplication.DEFAULT_LOCAL_TIME_ZONE);
            		clientLogJsonObj.put("date", GMT.convertToLocalDate(createdDate, tz, MobilePulseApplication.LIST_DATE_FORMAT));
            	}
            	
            	clientLogJsonObj.put("userName", cl.getUserName());
            	clientLogJsonObj.put("instanceUrl", cl.getInstanceUrl());
            	clientLogJsonObj.put("logLevel", cl.getLogLevel());
            	clientLogJsonObj.put("message", cl.getMessage());
            	clientLogJsonObj.put("stackBackTrace", cl.getStackBackTrace());
            	
            	// TODO remove eventually, for backward compatibility before status field existed. If status not set, default to 'new'
            	String status = cl.getStatus();
            	if(status == null || status.length() == 0) {status = "new";}
            	clientLogJsonObj.put("status", status);
				
            	clientLogJsonArray.put(clientLogJsonObj);
			}
			jsonReturn.put("clientLogs", clientLogJsonArray);
		} catch (JSONException e) {
			log.severe("error converting json representation into a JSON object");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
			e.printStackTrace();
		} catch (Exception e) {
			log.severe("getListOfClientLogsJson(): exception = " + e.getMessage());
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


    private JSONObject updateClientLog(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();
    	JSONObject jsonReturn = new JSONObject();
    	
		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);

		ClientLog clientLog = null;
        em.getTransaction().begin();
        try {
			if (this.clientLogId == null || this.clientLogId.length() == 0) {
				apiStatus = ApiStatusCode.CLIENT_LOG_ID_REQUIRED;
				jsonReturn.put("apiStatus", apiStatus);
				return jsonReturn;
			}
			
			clientLog = new ClientLog();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            if (this.clientLogId != null) {
                Key key = KeyFactory.stringToKey(this.clientLogId);
                clientLog = (ClientLog)em.createNamedQuery("ClientLog.getByKey")
                	.setParameter("key", key)
                	.getSingleResult();
            }
            if(json.has("status")) {
            	String status = json.getString("status").toLowerCase();
            	if(clientLog.isStatusValid(status)) {
            		clientLog.setStatus(status);
            	} else {
            		apiStatus = ApiStatusCode.INVALID_STATUS;
            	}
            }
            em.persist(clientLog);
            em.getTransaction().commit();
        } catch (IOException e) {
            log.severe("error extracting JSON object from Post");
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (NoResultException e) {
			// clientLogId passed in is not valid
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
			log.severe("error converting json representation into a JSON object");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
			e.printStackTrace();
		}

		return jsonReturn;
    }
}
