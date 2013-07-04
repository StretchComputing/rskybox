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
	private Boolean isActive;

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

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
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
        		if(endpointFilter.getKey() != null) {
            		json.put("id", KeyFactory.keyToString(endpointFilter.getKey()));
        		}
            	json.put("active", endpointFilter.getIsActive());
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
    
    @Override
    // if two objects are equal according to the equals() method, they must have the same hashCode()
    // value (although the reverse is not generally true)
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((localEndpoint == null) ? 0 : localEndpoint.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass()) {
        return false;
      }
     
      // email takes precedence on the compare and is used if either object being compared has an non-null email address
      EndpointFilter other = (EndpointFilter) obj;
      if (localEndpoint == null) {
    	  if(other.localEndpoint != null) {return false;}
      } else {
    	  if(other.localEndpoint != null && !localEndpoint.equals(other.localEndpoint)) {return false;}
      }
      
      if (remoteEndpoint == null) {
    	  if(other.remoteEndpoint != null) {return false;}
      } else {
    	  if(other.remoteEndpoint != null && !remoteEndpoint.equals(other.remoteEndpoint)) {return false;}
      }
      
      return true;
    }

}
