package com.stretchcom.mobilePulse.models;

import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.stretchcom.mobilePulse.server.ApiStatusCode;
import com.stretchcom.mobilePulse.server.EMF;

@Entity
@NamedQueries({
    @NamedQuery(
    		name="Application.getAll",
    		query="SELECT a FROM Application a"
    ),
    @NamedQuery(
    		name="Application.getByKey",
    		query="SELECT a FROM Application a WHERE a.key = :key"
    ),
    @NamedQuery(
    		name="Application.getByOrganizationId",
    		query="SELECT a FROM Application a WHERE a.organizationId = :organizationId"
    ),
})
public class Application {
    private static final Logger log = Logger.getLogger(Application.class.getName());
	
	private String name;
	private String organizationId;
	private String version;
	private Date createdGmtDate;
	private Date versionUpdatedGmtDate;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    public Key getKey() {
        return key;
    }
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	public Date getCreatedGmtDate() {
		return createdGmtDate;
	}

	public void setCreatedGmtDate(Date createdGmtDate) {
		this.createdGmtDate = createdGmtDate;
	}

	public Date getVersionUpdatedGmtDate() {
		return versionUpdatedGmtDate;
	}

	public void setVersionUpdatedGmtDate(Date versionUpdatedGmtDate) {
		this.versionUpdatedGmtDate = versionUpdatedGmtDate;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public static String verifyApplicationId(String theApplicationId) {
		if(theApplicationId == null) {
			return ApiStatusCode.APPLICATION_ID_REQUIRED;
		}
		
        EntityManager em = EMF.get().createEntityManager();
        String apiStatus = ApiStatusCode.SUCCESS;
        
		try {
			Key appKey = null;
			try {
				appKey = KeyFactory.stringToKey(theApplicationId);
			} catch (Exception e) {
				apiStatus = ApiStatusCode.APPLICATION_NOT_FOUND;
			}
					
    		Application app = (Application)em.createNamedQuery("Application.getByKey")
				.setParameter("key", appKey)
				.getSingleResult();
		} catch (NoResultException e) {
			log.info("application ID not found");
			apiStatus = ApiStatusCode.APPLICATION_NOT_FOUND;
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more applications have same key");
			apiStatus = ApiStatusCode.SERVER_ERROR;
		}
		
		return apiStatus;
	}
}
