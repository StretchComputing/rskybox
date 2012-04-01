package com.stretchcom.rskybox.models;

import java.util.Date;
import java.util.List;
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
import javax.persistence.Transient;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.stretchcom.rskybox.server.ApiStatusCode;
import com.stretchcom.rskybox.server.EMF;

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
    		name="Application.getByKeyAndOrganizationId",
    		query="SELECT a FROM Application a WHERE a.key = :key and a.organizationId = :organizationId"
    ),
    @NamedQuery(
    		name="Application.getByOrganizationId",
    		query="SELECT a FROM Application a WHERE a.organizationId = :organizationId"
    ),
    @NamedQuery(
    		name="Application.getByToken",
    		query="SELECT a FROM Application a WHERE a.token = :token"
    ),
    @NamedQuery(
    		name="Application.getByName",
    		query="SELECT a FROM Application a WHERE a.name = :name"
    ),
})
public class Application {
    private static final Logger log = Logger.getLogger(Application.class.getName());
    
    private static int DAYS_UNTIL_AUTO_ARCHIVE = 7;
	
	private String name;
	private String organizationId;
	private String version;
	private Date createdGmtDate;
	private Date versionUpdatedGmtDate;
	private String token;

	@Transient
	private String memberRole; // used internally on the server to return user role with list of user's applications

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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public String getMemberRole() {
		return memberRole;
	}

	public void setMemberRole(String memberRole) {
		this.memberRole = memberRole;
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
	
	// Returns the application with the specified token or null if application not found
	public static Application getApplicationWithToken(String theToken) {
        EntityManager em = EMF.get().createEntityManager();

        Application application = null;
        try {
    		application = (Application)em.createNamedQuery("Application.getByToken")
				.setParameter("token", theToken)
				.getSingleResult();
    		log.info("application with token = " + theToken + " found");
		} catch (NoResultException e) {
			// not an error is user not found
			log.info("application with token = " + theToken + " NOT found");
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more applications have same key");
		}
        
        return application;
	}
	
	// Returns the application with the specified token or null if application not found.
	// If multiple applications matching the name are found, the first is returned.
	public static Application getApplicationWithName(String theApplicationName) {
        EntityManager em = EMF.get().createEntityManager();
        Application application = null;
        List <Application> applications = null;
        try {
    		applications = (List <Application>)em.createNamedQuery("Application.getByName")
				.setParameter("name", theApplicationName)
				.getResultList();
    		if(applications.size() > 0) {
    			application = applications.get(0);
        		log.info("application with name = " + theApplicationName + " found");
    		} else {
    			log.info("no applications with specified name found");
    		}
		} catch (Exception e) {
			log.severe("exception = " + e.getMessage());
		}
        
        return application;
	}
	

	// returns the application entity matching specified application ID or null if it can't be found
	public static Application getApplicationWithId(String theApplicationId) {
        EntityManager em = EMF.get().createEntityManager();
        Application application = null;
        
		try {
			Key appKey = null;
			try {
				appKey = KeyFactory.stringToKey(theApplicationId);
			} catch (Exception e) {
				log.severe("could not convert application ID into a key");
				return null;
			}
					
			application = (Application)em.createNamedQuery("Application.getByKey")
				.setParameter("key", appKey)
				.getSingleResult();
		} catch (NoResultException e) {
			log.info("application ID not found");
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more applications have same key");
		}
		
		return application;
	}
	
	public int daysUntilAutoArchive() {
		// TODO allow application configuration to specify this
		return DAYS_UNTIL_AUTO_ARCHIVE;
	}
}
