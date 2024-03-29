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
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.stretchcom.rskybox.models.AppMember;
import com.stretchcom.rskybox.models.Application;
import com.stretchcom.rskybox.models.EndUser;
import com.stretchcom.rskybox.models.User;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;


public class EndUsersResource extends ServerResource {
    private static final Logger log = Logger.getLogger(EndUsersResource.class.getName());
    private String id;
	private String applicationId;
    private String pageSizeStr;
    private String cursor;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        id = (String) getRequest().getAttributes().get("id");
        this.applicationId = (String) getRequest().getAttributes().get("applicationId");
        
		Form form = getRequest().getResourceRef().getQueryAsForm();
		for (Parameter parameter : form) {
			log.info("parameter " + parameter.getName() + " = " + parameter.getValue());
			if(parameter.getName().equals("pageSize"))  {
				this.pageSizeStr = (String)parameter.getValue();
				this.pageSizeStr = Reference.decode(this.pageSizeStr);
				log.info("EndUsersResource() - decoded pageSizeStr = " + this.pageSizeStr);
			} else if(parameter.getName().equals("cursor"))  {
				this.cursor = (String)parameter.getValue();
				log.info("EndUsersResource() - cursor = " + this.cursor);
			}
		}
    }

    // Handles 'Get End User API'
    // Handles 'Get List of End Users API
    @Get("json")
    public JsonRepresentation get(Variant variant) {
    	String appIdStatus = Application.verifyApplicationId(this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(this, appIdStatus);
    	}
    	
        if (id != null) {
            // Get User Info API
        	log.info("in Get End User Info API");
        	return show();
        } else {
            // Get List of Feedback API
        	log.info("Get List of End Users API");
        	return index();
        }
    }

    // Handles 'EndUser OPTIONS'
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

    // Handles 'Create End User API'
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("in post");
		if(this.applicationId == null) {return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);}
		Application application = Application.getApplicationWithId(this.applicationId);
		if(application == null) {
			return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
		}
    	
        return save_end_user(entity, application);
    }

    // Handles 'Update End User API'
    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
		if(this.applicationId == null) {return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);}
		Application application = Application.getApplicationWithId(this.applicationId);
		if(application == null) {
			return Utility.apiError(this, ApiStatusCode.APPLICATION_NOT_FOUND);
		}
    	
		if (this.id == null || this.id.length() == 0) {
			return Utility.apiError(this, ApiStatusCode.END_USER_ID_REQUIRED);
		}
        return save_end_user(entity, application);
    }

    // Handles 'Delete End User API'
    @Delete("json")
    public JsonRepresentation delete() {
        log.info("in delete");
    	String appIdStatus = Application.verifyApplicationId(this.applicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return Utility.apiError(this, appIdStatus);
    	}
    	
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
        try {
			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(this, ApiStatusCode.END_USER_ID_REQUIRED);
			}
			
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(this, ApiStatusCode.END_USER_NOT_FOUND);
			}
            em.getTransaction().begin();
            EndUser endUser = (EndUser) em.createNamedQuery("EndUser.getByKey").setParameter("key", key).getSingleResult();
            em.remove(endUser);
            em.getTransaction().commit();
        } catch (NoResultException e) {
        	return Utility.apiError(this, ApiStatusCode.END_USER_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more end users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        
        try {
			json.put("apiStatus", apiStatus);
		} catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
        	e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
        return new JsonRepresentation(json);
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
            int pageSize = EndUser.DEFAULT_PAGE_SIZE;
            if(this.pageSizeStr != null) {
            	try {
            		pageSize = new Integer(this.pageSizeStr);
            		if(pageSize > EndUser.MAX_PAGE_SIZE) {
                		log.info("pageSizeStr exceeds maximum, value = " + pageSize);
    			    	return Utility.apiError(this, ApiStatusCode.INVALID_PAGE_SIZE_PARAMETER);
            		}
            	} catch(NumberFormatException e) {
            		log.info("pageSizeStr is not an integer, value = " + this.pageSizeStr);
			    	return Utility.apiError(this, ApiStatusCode.INVALID_PAGE_SIZE_PARAMETER);
            	}
            }
            
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Query q = new Query("EndUser");
            q.addFilter("applicationId", Query.FilterOperator.EQUAL, this.applicationId);
            q.addSort("createdGmtDate", SortDirection.DESCENDING);
            PreparedQuery pq = datastore.prepare(q);
            FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);
            log.info("setting page size to " + pageSize);
            
            if (this.cursor != null) {
                fetchOptions.startCursor(Cursor.fromWebSafeString(this.cursor));
                log.info("setting cursor to " + this.cursor);
              }
            QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
            log.info("number of end users from query = " + results.size());
            
            JSONArray ja = new JSONArray();
            for (Entity entity : results) {
            	EndUser eu = EndUser.build(entity);
            	ja.put(getEndUserJson(eu));
            }

//            List<EndUser> endUsers = new ArrayList<EndUser>();
//            JSONArray ja = new JSONArray();
//            endUsers = (List<EndUser>) em.createNamedQuery("EndUser.getAllWithApplicationId")
//            		.setParameter("applicationId", this.applicationId)
//            		.getResultList();
//            
//            for (EndUser endUser : endUsers) {
//                ja.put(getEndUserJson(endUser));
//            }
            
            json.put("endUsers", ja);
            json.put("apiStatus", apiStatus);
            json.put("cursor", results.getCursor().toWebSafeString());
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
    	User currentUser = Utility.getCurrentUser(getRequest());
		EndUser endUser = null;
		try {
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
        	AppMember currentUserMember = AppMember.getAppMember(this.applicationId, KeyFactory.keyToString(currentUser.getKey()));
        	if(currentUserMember == null) {
				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
        	}

			if (this.id == null || this.id.length() == 0) {
				return Utility.apiError(this, ApiStatusCode.END_USER_ID_REQUIRED);
			}
			
			// id of end user specified
            Key key;
			try {
				key = KeyFactory.stringToKey(this.id);
			} catch (Exception e) {
				log.info("ID provided cannot be converted to a Key");
				return Utility.apiError(this, ApiStatusCode.END_USER_NOT_FOUND);
			}
			
    		endUser = (EndUser)em.createNamedQuery("EndUser.getByKey")
				.setParameter("key", key)
				.getSingleResult();
		} catch (NoResultException e) {
			return Utility.apiError(this, ApiStatusCode.END_USER_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more end users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} 
        
        return new JsonRepresentation(getEndUserJson(endUser, apiStatus));
    }

    private JsonRepresentation save_end_user(Representation entity, Application theApplication) {
        EntityManager em = EMF.get().createEntityManager();

        EndUser endUser = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
    	User currentUser = Utility.getCurrentUser(getRequest());
    	String oldVersion = null;
        try {
            endUser = new EndUser();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            Boolean isUpdate = false;
            if (id != null) {
            	/////////////////////
            	// Update EndUser API 
            	/////////////////////
            	
            	//////////////////////
            	// Authorization Rules
            	//////////////////////
            	AppMember currentUserMember = AppMember.getAppMember(this.applicationId, KeyFactory.keyToString(currentUser.getKey()));
            	if(currentUserMember == null) {
    				return Utility.apiError(this, ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
            	}

                Key key = KeyFactory.stringToKey(this.id);
                endUser = (EndUser) em.createNamedQuery("EndUser.getByKey").setParameter("key", key).getSingleResult();
        		this.setStatus(Status.SUCCESS_OK);
                isUpdate = true;
            } else {
            	/////////////////////
            	// Create EndUser API 
            	/////////////////////
            	
                // userId can NOT be changed. If this is a new userId, then the "existing" end user will not be found below
            	if (json.has("userId")) {
                	endUser.setUserId(json.getString("userId"));
                } else {
                	// for backward compatibility, rskybox clients used to pass in only userName
                	if(json.has("userName")) {
                    	endUser.setUserId(json.getString("userName"));
                	} else {
                    	return Utility.apiError(this, ApiStatusCode.USER_ID_IS_REQUIRED);
                	}
                }

                // EndUser create is designed to be called multiple times. So it's ok if the endUser has not been defined yet and it is also
            	// ok if the endUser has already been defined.
            	try {
                    endUser = (EndUser) em.createNamedQuery("EndUser.getByUserId").setParameter("userId", endUser.getUserId()).getSingleResult();
                    log.info("End User already exists");
            	} catch (NoResultException e) {
            		// NOT an error - first time create has been called for an endUser with this userName
        			log.info("End User not found -- new end user will be created");
        			endUser.setCreatedGmtDate(new Date());
        			theApplication.incrementEndUserCount();
        		} catch (NonUniqueResultException e) {
        			log.severe("should never happen - two or more end users have same key");
        			this.setStatus(Status.SERVER_ERROR_INTERNAL);
        		}
            }
            
            if (!isUpdate && json.has("userName")) {
            	endUser.setUserName(json.getString("userName"));
            }
            
            if (!isUpdate && json.has("application")) {
            	endUser.setApplication(json.getString("application"));
            }
            
            if (!isUpdate && json.has("summary")) {
            	endUser.setSummary(json.getString("summary"));
            }
            
            oldVersion = endUser.getVersion();
            if (json.has("version")) {
            	String newVersion = json.getString("version");
            	endUser.setVersion(newVersion);
            	if(oldVersion != null && !oldVersion.equalsIgnoreCase(newVersion)) {
            		// track the date and time when the version number changes
            		endUser.setVersionUpdatedGmtDate(new Date());
            	}
            }
            
            if (!isUpdate && json.has("instanceUrl")) {
            	endUser.setInstanceUrl(json.getString("instanceUrl"));
            }
            
            if(!isUpdate) {
            	endUser.setApplicationId(this.applicationId);
            }
            
            em.persist(endUser);
        } catch (IOException e) {
            log.severe("error extracting JSON object from Post. exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (NoResultException e) {
        	return Utility.apiError(this, ApiStatusCode.END_USER_NOT_FOUND);
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more end users have same key");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        
        return new JsonRepresentation(getEndUserJson(endUser, apiStatus));
    }
    
    private JSONObject getEndUserJson(EndUser endUser) {
    	return getEndUserJson(endUser, null);
    }

    private JSONObject getEndUserJson(EndUser theEndUser, String theApiStatus) {
        JSONObject json = new JSONObject();

        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(theEndUser != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
                json.put("id", KeyFactory.keyToString(theEndUser.getKey()));
                
                // for backward compatibility. Only userName used to be required/reported. Now userId is required.
                String userId = theEndUser.getUserId();
                String userName = theEndUser.getUserName();
                if(userId != null) {
                    json.put("userId", userId);
                } else {
                    json.put("userId", userName);
                }
                json.put("userName", userName);
                
                json.put("application", theEndUser.getApplication());
                json.put("version", theEndUser.getVersion());
                json.put("instanceUrl", theEndUser.getInstanceUrl());
                json.put("summary", theEndUser.getSummary());
                json.put("appId", theEndUser.getApplicationId());
        	}
        } catch (JSONException e) {
        	log.severe("getEndUserJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
}
