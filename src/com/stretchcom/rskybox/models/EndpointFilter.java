package com.stretchcom.rskybox.models;

import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.stretchcom.rskybox.server.ApiStatusCode;
import com.stretchcom.rskybox.server.GMT;

@Entity
@NamedQueries({
    @NamedQuery(
    		name="EndpointFilter.getAll",
    		query="SELECT ef FROM EndpointFilter ef ORDER BY ef.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="EndpointFilter.getAllWithApplicationId",
    		query="SELECT ef FROM EndpointFilter ef WHERE ef.applicationId = :applicationId ORDER BY ef.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="EndpointFilter.getByUserIdAndApplicationId",
    		query="SELECT ef FROM EndpointFilter ef WHERE ef.userId = :userId and ef.applicationId = :applicationId ORDER BY ef.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="EndpointFilter.getByKey",
    		query="SELECT ef FROM EndpointFilter ef WHERE ef.key = :key"
    ),
})
public class EndpointFilter {
	private static final Logger log = Logger.getLogger(EndpointFilter.class.getName());
	
	private Date createdGmtDate;
	private String userId;
	private String applicationId;
	private String localEndpoint;
	private String remoteEndpoint;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    public Key getKey() {
        return key;
    }
	
	public Date getCreatedGmtDate() {
		return createdGmtDate;
	}

	public void setCreatedGmtDate(Date createdGmtDate) {
		this.createdGmtDate = createdGmtDate;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	
	public String getLocalEndpoint() {
		return localEndpoint;
	}

	public void setLocalEndpoint(String localEndpoint) {
		this.localEndpoint = localEndpoint;
	}

	public String getRemoteEndpoint() {
		return remoteEndpoint;
	}

	public void setRemoteEndpoint(String remoteEndpoint) {
		this.remoteEndpoint = remoteEndpoint;
	}
	
	public Boolean matches(String theLEP, String theREP) {
		if(this.localEndpoint.equalsIgnoreCase(theLEP) && this.remoteEndpoint.equalsIgnoreCase(theREP)) {
			return true;
		}
		return false;
	}

    public static JSONObject getJson(EndpointFilter feedback, Boolean isList) {
    	return getJson(feedback, null, isList);
    }

    public static JSONObject getJson(EndpointFilter endpointFilter, String theApiStatus, Boolean isList) {
    	
        JSONObject json = new JSONObject();
        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(endpointFilter != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
        		json.put("id", KeyFactory.keyToString(endpointFilter.getKey()));
            	json.put("localEndPoint", endpointFilter.getLocalEndpoint());
            	json.put("remoteEndPoint", endpointFilter.getRemoteEndpoint());
            	json.put("appId", endpointFilter.getApplicationId());
        	}
        } catch (JSONException e) {
        	log.severe("getJson() error creating JSON return object. Exception = " + e.getMessage());
            return null;
        }
        return json;
    }
}
