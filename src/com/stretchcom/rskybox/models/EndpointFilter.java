package com.stretchcom.rskybox.models;

import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.stretchcom.rskybox.server.ApiStatusCode;
import com.stretchcom.rskybox.server.EMF;
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
    		name="EndpointFilter.getByUserIdAndApplicationIdandLepAndRep",
    		query="SELECT ef FROM EndpointFilter ef WHERE ef.userId = :userId and ef.applicationId = :applicationId and ef.localEndpoint = :localEndpoint and ef.remoteEndpoint = :remoteEndpoint"
    ),
    @NamedQuery(
    		name="EndpointFilter.getByUserIdAndApplicationIdAndIsActive",
    		query="SELECT ef FROM EndpointFilter ef WHERE ef.userId = :userId and ef.applicationId = :applicationId and ef.isActive = :isActive ORDER BY ef.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="EndpointFilter.getByKey",
    		query="SELECT ef FROM EndpointFilter ef WHERE ef.key = :key"
    ),
})
public class EndpointFilter implements Comparable<EndpointFilter> {
	private static final Logger log = Logger.getLogger(EndpointFilter.class.getName());
	
	public final static String ALL_FILTER = "ALL";
	
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
            	json.put("localEndpoint", endpointFilter.getLocalEndpoint());
            	json.put("remoteEndpoint", endpointFilter.getRemoteEndpoint());
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
    	  if(other.localEndpoint != null && !localEndpoint.equalsIgnoreCase(other.localEndpoint)) {return false;}
      }
      
      if (remoteEndpoint == null) {
    	  if(other.remoteEndpoint != null) {return false;}
      } else {
    	  if(other.remoteEndpoint != null && !remoteEndpoint.equalsIgnoreCase(other.remoteEndpoint)) {return false;}
      }
      
      return true;
    }
    
    // Create all filter if it doesn't already exist
	public static void createAllFilter(String theUserId, String theApplicationId) {
        EntityManager em = EMF.get().createEntityManager();
        EndpointFilter allFilter = null;
		
		try {
			allFilter = (EndpointFilter)em.createNamedQuery("EndpointFilter.getByUserIdAndApplicationIdandLepAndRep")
				.setParameter("userId", theUserId)
				.setParameter("applicationId", theApplicationId)
				.setParameter("localEndpoint", EndpointFilter.ALL_FILTER)
				.setParameter("remoteEndpoint", EndpointFilter.ALL_FILTER)
				.getSingleResult();
		} catch (NoResultException e) {
			// Not an error -- if there is no ALL filter, then create it
	        em.getTransaction().begin();
			try {
				allFilter = new EndpointFilter();
				allFilter.setApplicationId(theApplicationId);
				allFilter.setUserId(theUserId);
				allFilter.setIsActive(false);
				allFilter.setLocalEndpoint(EndpointFilter.ALL_FILTER);
				allFilter.setRemoteEndpoint(EndpointFilter.ALL_FILTER);
				allFilter.setCreatedGmtDate(new Date());
				em.persist(allFilter);
				em.getTransaction().commit();
			} catch (Exception e1) {
				log.severe("createAllFilter() exception = " + e1.getMessage());
			} finally {
	            if (em.getTransaction().isActive()) {
	                em.getTransaction().rollback();
	            }
	        }
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more EndpointFilters have the same key");
		} finally {
			em.close();
		}
	}
	
	public static Boolean isAllFilterActive(String theUserId, String theApplicationId) {
        EntityManager em = EMF.get().createEntityManager();
        EndpointFilter allFilter = null;
		Boolean allFilterActive = false;
		
		try {
			allFilter = (EndpointFilter)em.createNamedQuery("EndpointFilter.getByUserIdAndApplicationIdandLepAndRep")
				.setParameter("userId", theUserId)
				.setParameter("applicationId", theApplicationId)
				.setParameter("localEndpoint", EndpointFilter.ALL_FILTER)
				.setParameter("remoteEndpoint", EndpointFilter.ALL_FILTER)
				.getSingleResult();
			
			if(allFilter.getIsActive()) {
				allFilterActive = true;
			}
		} catch (NoResultException e) {
			// Not an error -- function will now end up returning false
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more EndpointFilters have the same key");
		} finally {
			em.close();
		}
		return allFilterActive;
	}
	
	public int compareTo(EndpointFilter aThat) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;

		if(this == aThat)
			return EQUAL;
		
		// logic is reverse since sorting is done in reverse chronological order
		if (this.isActive && !aThat.isActive) {
			return AFTER;
		} else if (!this.isActive && aThat.isActive) {
			return BEFORE;
		} else {
			return this.localEndpoint.compareTo(aThat.localEndpoint);
		}
	}

}
