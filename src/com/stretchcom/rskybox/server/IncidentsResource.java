package com.stretchcom.rskybox.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import com.stretchcom.rskybox.models.AppMember;
import com.stretchcom.rskybox.models.Application;
import com.stretchcom.rskybox.models.ClientLog;
import com.stretchcom.rskybox.models.CrashDetect;
import com.stretchcom.rskybox.models.EndpointFilter;
import com.stretchcom.rskybox.models.Feedback;
import com.stretchcom.rskybox.models.Incident;
import com.stretchcom.rskybox.models.Notification;
import com.stretchcom.rskybox.models.User;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;

public class IncidentsResource extends ServerResource {
	private static final Logger log = Logger.getLogger(IncidentsResource.class.getName());
	private String id;
	private String tag;
	private String applicationId;
    private String incidentStatus;
    private String remoteControl;
    private String includeEvents;
    private String pageSizeStr;
    private String cursor;

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
			} else if(parameter.getName().equals("includeEvents"))  {
				this.includeEvents = (String)parameter.getValue().toLowerCase();
				this.includeEvents = Reference.decode(this.includeEvents);
				log.info("IncidentResource() - decoded includeEvents = " + this.includeEvents);
			} else if(parameter.getName().equals("pageSize"))  {
				this.pageSizeStr = (String)parameter.getValue();
				this.pageSizeStr = Reference.decode(this.pageSizeStr);
				log.info("IncidentResource() - decoded pageSizeStr = " + this.pageSizeStr);
			} else if(parameter.getName().equals("cursor"))  {
				this.cursor = (String)parameter.getValue();
				log.info("IncidentResource() - cursor = " + this.cursor);
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
		if(this.applicationId == null) {return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);}
		Application application = Application.getApplicationWithId(this.applicationId);
		if(application == null) {
			return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
		}
    	
		// incident ID is required
    	if (this.id == null || this.id.length() == 0) {
			return Utility.apiError(this, ApiStatusCode.INCIDENT_ID_REQUIRED);
		}
    	
    	if(this.remoteControl == null) {
    		// Update Incident API6
            return save_incident(entity, application);
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
    	User currentUser = Utility.getCurrentUser(getRequest());
        try {
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	AppMember appMember = AppMember.getAppMember(this.applicationId, KeyFactory.keyToString(currentUser.getKey()));
        	if(appMember == null) {
				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
        	}
            
            /////////////////////////
            // Valid input parameters
            /////////////////////////
            if(this.incidentStatus != null) {
				if(!Incident.isStatusParameterValid(this.incidentStatus)) {
			    	return Utility.apiError(this, ApiStatusCode.INVALID_STATUS_PARAMETER);
				}
			} else {
				// default is to retrieve open incidents only
				this.incidentStatus = Incident.OPEN_STATUS;
			}
            if(this.tag != null) {
				if(!Incident.isWellKnownTagValid(this.tag)) {
			    	return Utility.apiError(this, ApiStatusCode.INVALID_TAGS_PARAMETER);
				}
			}
            
            int pageSize = Incident.DEFAULT_PAGE_SIZE;
            if(this.pageSizeStr != null) {
            	try {
            		pageSize = new Integer(this.pageSizeStr);
            		if(pageSize > Incident.MAX_PAGE_SIZE) {
                		log.info("pageSizeStr exceeds maximum, value = " + pageSize);
    			    	return Utility.apiError(this, ApiStatusCode.INVALID_PAGE_SIZE_PARAMETER);
            		}
            	} catch(NumberFormatException e) {
            		log.info("pageSizeStr is not an integer, value = " + this.pageSizeStr);
			    	return Utility.apiError(this, ApiStatusCode.INVALID_PAGE_SIZE_PARAMETER);
            	}
            }
            
//			if(this.tag == null) {
//		    if(this.incidentStatus.equalsIgnoreCase(Incident.ALL_STATUS)){
//		    	incidents= (List<Incident>)em.createNamedQuery("Incident.getAllWithApplicationId")
//		    			.setParameter("applicationId", this.applicationId)
//		    			.getResultList();
//		    } else {
//		    	incidents= (List<Incident>)em.createNamedQuery("Incident.getByStatusAndApplicationId")
//						.setParameter("status", this.incidentStatus)
//						.setParameter("applicationId", this.applicationId)
//						.getResultList();
//		    } 
//		} else {
//		    if(this.incidentStatus.equalsIgnoreCase(Incident.ALL_STATUS)){
//		    	incidents= (List<Incident>)em.createNamedQuery("Incident.getAllWithApplicationIdAndTag")
//		    			.setParameter("applicationId", this.applicationId)
//		    			.setParameter("tag", this.tag)
//		    			.getResultList();
//		    } else {
//		    	incidents= (List<Incident>)em.createNamedQuery("Incident.getByStatusAndApplicationIdAndTag")
//						.setParameter("status", this.incidentStatus)
//						.setParameter("applicationId", this.applicationId)
//						.setParameter("tag", this.tag)
//						.getResultList();
//		    } 
//		}

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Query q = new Query("Incident");
            Filter appIdFilter = new FilterPredicate("applicationId",FilterOperator.EQUAL, this.applicationId);
            Filter statusFilter = null;
            Filter tagsFilter = null;
            Filter completeFilter = null;
            q.addSort("lastUpdatedGmtDate", SortDirection.DESCENDING);
			if(this.tag == null) {
			    if(this.incidentStatus.equalsIgnoreCase(Incident.ALL_STATUS)){
			    	// no additional filter need here
			    	q.setFilter(appIdFilter);
			    	log.info("query conditions 1");
			    } else {
		            statusFilter = new FilterPredicate("status",FilterOperator.EQUAL,this.incidentStatus);
		            completeFilter = CompositeFilterOperator.and(appIdFilter, statusFilter);
		            q.setFilter(completeFilter);
			    	log.info("query conditions 2");
			    } 
			} else {
			    if(this.incidentStatus.equalsIgnoreCase(Incident.ALL_STATUS)){
		            tagsFilter = new FilterPredicate("tags",FilterOperator.EQUAL, this.tag);
		            completeFilter = CompositeFilterOperator.and(appIdFilter, tagsFilter);
		            q.setFilter(completeFilter);
			    	log.info("query conditions 3");
			    } else {
		            statusFilter = new FilterPredicate("status",FilterOperator.EQUAL, this.incidentStatus);
		            tagsFilter = new FilterPredicate("tags",FilterOperator.EQUAL, this.tag);
		            completeFilter = CompositeFilterOperator.and(appIdFilter, statusFilter, tagsFilter);
		            q.setFilter(completeFilter);
			    	log.info("query conditions 4. status = " + this.incidentStatus + " tag = " + this.tag);
			    }
			}
            PreparedQuery pq = datastore.prepare(q);
            FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);
            log.info("setting page size to " + pageSize);
            
            if (this.cursor != null) {
                fetchOptions.startCursor(Cursor.fromWebSafeString(this.cursor));
                log.info("setting cursor to " + this.cursor);
              }
            QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
            log.info("number of incidents from query = " + results.size());
            
            List<EndpointFilter> endpointFilters = User.getActiveUserEndpointFilters(this.applicationId, currentUser.getId());
            JSONArray ja = new JSONArray();
            for (Entity entity : results) {
            	Incident i = Incident.build(entity);
                // endpoint filters are used to filter OUT incidents, so only return incident if no endpoint filters match
                if(!User.anyFilterMatches(endpointFilters, i.getLocalEndpoint(), i.getRemoteEndpoint())) {
                    ja.put(i.getJson(null, this.includeEvents));
                }
            }
            json.put("incidents", ja);
            json.put("apiStatus", apiStatus);
            json.put("cursor", results.getCursor().toWebSafeString());
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
        log.info("IncidentsResource in show()");
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
		} finally {
			em.close();
		}
        
        return new JsonRepresentation(incident.getJson(apiStatus, this.includeEvents));
    }

    private JsonRepresentation save_incident(Representation entity, Application theApplication) {
        EntityManager em = EMF.get().createEntityManager();
        JSONObject jsonReturn = new JSONObject();

        Incident incident = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
    	User currentUser = Utility.getCurrentUser(getRequest());
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
			
			if(json.has("tags")) {
				List<String> tags = new ArrayList<String>();
	        	JSONArray tagsJsonArray = json.getJSONArray("tags");
				int arraySize = tagsJsonArray.length();
				log.info("tags json array length = " + arraySize);
				for(int i=0; i<arraySize; i++) {
					String tag = tagsJsonArray.getString(i);
					tags.add(tag);
				}
				
				int wktCount = Incident.getWellKnownTagCount(tags);
				// ::BusinessRule:: tag list must have exactly one well known tag
				if(wktCount > 1) {
					return Utility.apiError(this, ApiStatusCode.INVALID_TAGS_PARAMETER);
				} else if(wktCount == 0) {
					return Utility.apiError(this, ApiStatusCode.WELL_KNOWN_TAG_REQUIRED);
				}

				if(isUpdate) {
					// for an update, the tag list is replaced with the new tags
					// ::BusinessRule:: the new tag list must have the same well known tag as the original list
					String originalWellKnownTag = incident.getWellKnownTag();
					incident.setTags(tags);
					String newWellKnownTag = incident.getWellKnownTag();
					if(!newWellKnownTag.equalsIgnoreCase(originalWellKnownTag)) {
						return Utility.apiError(this, ApiStatusCode.INVALID_TAGS_PARAMETER);
					}
				} else {
					incident.setTags(tags);
				}
			} else if(!isUpdate) {
				return Utility.apiError(this, ApiStatusCode.WELL_KNOWN_TAG_REQUIRED);
			}

            if(!isUpdate) {
            	if(json.has("name")) {
                	incident.setEventName(json.getString("name"));
            	} else {
                	return Utility.apiError(this, ApiStatusCode.NAME_REQUIRED);
                }
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
	            		// changeStatus calls a method that uses a transaction
	            		incident.changeStatus(incident.getWellKnownTag(), status, theApplication);
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
	            
	            if(json.has("issueTracking")) {
	            	String action = json.getString("issueTracking").toLowerCase();
	            	if(Incident.isActionValid(action)) {
	            		// TODO only allow this issue creation -- right now -- for rTeam and Arc
	            		if(!incident.isIssueTrackingSupported()) {
	            			return Utility.apiError(this, ApiStatusCode.ISSUE_TRACKING_NOT_SUPPORTED_FOR_THIS_APPLICATION);
	            		}
	            		
	            		// only allow create issue
	            		// TODO support update issue
	            		if(incident.getGithubUrl() != null) {
	            			return Utility.apiError(this, ApiStatusCode.ISSUE_TRACKING_ALREADY_CREATED);
	            		}
	            		
	            		// TODO for now, owner and repo hardcode
	            		String owner = "StretchComputing";
	            		String repo = "arc";
	            		
	            		String title = "[rSkybox #" + incident.getNumber().toString()  + "]" + " " + incident.getEventName();
	            		String body = incident.buildBodyInMarkDown(this.applicationId);
	            		String response = GitHubClient.createIssue(owner, repo, title, body);
	            		if(response != null) {
	            			// TODO pull just the url from the response !!!!!!!!!
	            			log.info("response = " + response);
		            		String githubUrl = response;
		            		incident.updateGithubUrl(githubUrl);
	            		} else {
	            			log.info("response from GithubClient was null");
	            		}
	            	} else {
						return Utility.apiError(this, ApiStatusCode.INVALID_STATUS_PARAMETER);
	            	}
	            }
			} else {
				incident.setApplicationId(this.applicationId);
            	
				// Default status to 'open'
				incident.changeStatus(incident.getWellKnownTag(), Incident.OPEN_STATUS, theApplication);
				
				// Default severity
				incident.setSeverity(Incident.LOW_SEVERITY);
				
				// Assign application unique incident number
				incident.setNumber(Application.getAndIncrementIncidentNumber(this.applicationId));
				
				// set the activeThruGmtDate for auto closing
				int daysUntilAutoArchive = theApplication.daysUntilAutoArchive();
				Date activeThruGmtDate = GMT.addDaysToDate(new Date(), daysUntilAutoArchive);
				incident.setActiveThruGmtDate(activeThruGmtDate);
				
				// created and last updated is now
				incident.setCreatedGmtDate(new Date());
				incident.setLastUpdatedGmtDate(new Date());
			}
			
            em.persist(incident);
            
            if(!isUpdate) {
            	String message = incident.getEventName();
            	if(incident.getSummary() != null && incident.getSummary().length() > 0) {
            		message += " " + incident.getSummary();
            	}
            } else {
            	if(severityChanged) {
                	String severityMsg = "Severity changed from " + oldSeverity.toString() + " to " + incident.getSeverity().toString();
                	User.sendNotifications(this.applicationId, incident, severityMsg);
            	}
            }
        	
            jsonReturn.put("incident", incident.getJson(apiStatus, this.includeEvents));
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
        EntityManager em = EMF.get().createEntityManager();
        JSONObject jsonReturn = new JSONObject();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
    	User currentUser = Utility.getCurrentUser(getRequest());
        try {
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	AppMember currentUserMember = AppMember.getAppMember(this.applicationId, KeyFactory.keyToString(currentUser.getKey()));
        	if(currentUserMember == null) {
				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
        	}

        	String mode = null;
            if(json.has("mode")) {
            	mode = json.getString("mode");
            	if(!Incident.isModeValid(mode)) {
            		return Utility.apiError(this, ApiStatusCode.INVALID_MODE);
            	}
            } else {
            	return Utility.apiError(this, ApiStatusCode.MODE_IS_REQUIRED);
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
    		Incident incident = (Incident)em.createNamedQuery("Incident.getByKey")
				.setParameter("key", key)
				.getSingleResult();
    		incident.setRemoteControlMode(mode);
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
    
//    private JSONObject getIncidentJson(Incident incident) {
//    	return getIncidentJson(incident, null, isList);
//    }
//
//    private JSONObject getIncidentJson(Incident incident, String theApiStatus) {
//        JSONObject json = new JSONObject();
//
//        try {
//        	if(theApiStatus != null) {
//        		json.put("apiStatus", theApiStatus);
//        	}
//        	if(incident != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
//        		json.put("id", KeyFactory.keyToString(incident.getKey()));
//        		json.put("number", incident.getNumber());
//            	json.put("status", incident.getStatus());
//            	json.put("severity", incident.getSeverity());
//            	json.put("name", incident.getEventName());
//    			
//            	Date createdDate = incident.getCreatedGmtDate();
//            	if(createdDate != null) {
//            		json.put("createdDate", GMT.convertToIsoDate(createdDate));
//            	}
//            	Date lastUpdatedDate = incident.getLastUpdatedGmtDate();
//            	if(lastUpdatedDate != null) {
//            		json.put("lastUpdatedDate", GMT.convertToIsoDate(lastUpdatedDate));
//            	}
//            	
//            	JSONArray tagsJsonArray = new JSONArray();
//            	List<String> tags = incident.getTags();
//            	for(String tag: tags) {
//            		tagsJsonArray.put(tag);
//            	}
//            	log.info("# of tags = " + tags.size());
//            	json.put("tags", tagsJsonArray);
//            	
//            	json.put("eventCount", incident.getEventCount());
//            	json.put("message", incident.getMessage());
//            	json.put("summary", incident.getSummary());
//            	json.put("appId", incident.getApplicationId());
//            	json.put("mode", incident.getRemoteControlMode());
//    			json.put("githubUrl", incident.getGithubUrl());
//            	
//            	if(this.includeEvents != null && this.includeEvents.equalsIgnoreCase("true")) {
//            		json.put("events", getEventsJsonObj(incident));
//            	}
//        	}
//        } catch (JSONException e) {
//        	log.severe("IncidentsResrouce::getIncidentJson() error creating JSON return object. Exception = " + e.getMessage());
//            this.setStatus(Status.SERVER_ERROR_INTERNAL);
//        }
//        return json;
//    }
}
