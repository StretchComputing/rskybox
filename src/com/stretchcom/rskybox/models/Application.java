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
    
    private static int MAX_DAYS_IN_LIMBO = 10;
    private static int DAYS_UNTIL_AUTO_ARCHIVE = 7;
    //private static int DAYS_UNTIL_AUTO_ARCHIVE = -2;  // for local testing
    private static int MAX_EVENTS_PER_INCIDENT = 10;
    
    private static float LEAST_SENSITIVITY = 16;
    private static float LOW_SENSITIVITY = 8;
    private static float MEDIUM_SENSITIVITY = 4;
    private static float HIGH_SENSITIVITY = 2;
    private static float MOST_SENSITIVITY = 1;
	
	private String name;
	private String organizationId;
	private String version;
	private Date createdGmtDate;
	private Date versionUpdatedGmtDate;
	private String token;
	private Integer nextIncidentNumber = 1;
	private Integer daysInLimbo = MAX_DAYS_IN_LIMBO; // how long an incident can be closed and still be reopened
	private Float severitySensitivity = MOST_SENSITIVITY;
	private Integer numberOfEndUsers = 0;
	private Integer maxEventsPerIncident = MAX_EVENTS_PER_INCIDENT;
	private Integer numberOfOpenLogs = 0;
	private Integer numberOfOpenCrashes = 0;
	private Integer numberOfOpenFeedbacks = 0;

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

	public Integer getNextIncidentNumber() {
		// backward compatibility -- for apps created before this field added
		nextIncidentNumber = nextIncidentNumber == null ? 1 : nextIncidentNumber;
		return nextIncidentNumber;
	}

	public void setNextIncidentNumber(Integer nextIncidentNumber) {
		this.nextIncidentNumber = nextIncidentNumber;
	}
	
	// returns the application ID as a string
	public String getId() {
		return KeyFactory.keyToString(this.key);
	}

	public Integer getDaysInLimbo() {
		// backward compatibility for apps created before this field existed
		daysInLimbo = daysInLimbo == null ? MAX_DAYS_IN_LIMBO : daysInLimbo;
		return daysInLimbo;
	}

	public void setDaysInLimbo(Integer daysInLimbo) {
		this.daysInLimbo = daysInLimbo;
	}

	public Float getSeveritySensitivity() {
		// backward compatibility for apps created before this field existed
		severitySensitivity = severitySensitivity == null ? MOST_SENSITIVITY : severitySensitivity;
		return severitySensitivity;
	}

	public void setSeveritySensitivity(Float severitySensitivity) {
		this.severitySensitivity = severitySensitivity;
	}

	public Integer getNumberOfEndUsers() {
		// for backward compatibility of apps created before this field existed
		numberOfEndUsers = numberOfEndUsers == null ? 0 : numberOfEndUsers;
		return numberOfEndUsers;
	}

	public void setNumberOfEndUsers(Integer numberOfEndUsers) {
		this.numberOfEndUsers = numberOfEndUsers;
	}

	public Integer getMaxEventsPerIncident() {
		// for backward compatibility of apps created before this field existed
		maxEventsPerIncident = maxEventsPerIncident == null ? Application.MAX_EVENTS_PER_INCIDENT : maxEventsPerIncident;
		return maxEventsPerIncident;
	}

	public void setMaxEventsPerIncident(Integer maxEventsPerIncident) {
		this.maxEventsPerIncident = maxEventsPerIncident;
	}

	public Integer getNumberOfOpenLogs() {
		return numberOfOpenLogs;
	}

	public void setNumberOfOpenLogs(Integer numberOfOpenLogs) {
		this.numberOfOpenLogs = numberOfOpenLogs;
	}
	
	public void incrementNumberOfOpenLogs() {
		this.numberOfOpenLogs = this.numberOfOpenLogs == null ? 0 : this.numberOfOpenLogs;
		this.numberOfOpenLogs++;
	}
	
	public void decrementNumberOfOpenLogs() {
		if(this.numberOfOpenLogs == null || this.numberOfOpenLogs == 0) {
			log.severe("Application.decrementNumberOfOpenLogs() failed because numberOfOpenLogs was zero or null");
			return;
		}
		this.numberOfOpenLogs--;
	}

	public Integer getNumberOfOpenCrashes() {
		return numberOfOpenCrashes;
	}
	
	public void incrementNumberOfOpenCrashes() {
		this.numberOfOpenCrashes = this.numberOfOpenCrashes == null ? 0 : this.numberOfOpenCrashes;
		this.numberOfOpenCrashes++;
	}
	
	public void decrementNumberOfOpenCrashes() {
		if(this.numberOfOpenCrashes == null || this.numberOfOpenCrashes == 0) {
			log.severe("Application.decrementNumberOfOpenCrashes() failed because numberOfOpenCrashes was zero or null");
			return;
		}
		this.numberOfOpenCrashes--;
	}

	public void setNumberOfOpenCrashes(Integer numberOfOpenCrashes) {
		this.numberOfOpenCrashes = numberOfOpenCrashes;
	}

	public Integer getNumberOfOpenFeedbacks() {
		return numberOfOpenFeedbacks;
	}
	
	public void incrementNumberOfOpenFeedbacks() {
		this.numberOfOpenFeedbacks = this.numberOfOpenFeedbacks == null ? 0 : this.numberOfOpenFeedbacks;
		this.numberOfOpenFeedbacks++;
	}
	
	public void decrementNumberOfOpenFeedbacks() {
		if(this.numberOfOpenFeedbacks == null || this.numberOfOpenFeedbacks == 0) {
			log.severe("Application.decrementNumberOfOpenFeedbacks() failed because numberOfOpenFeedbacks was zero or null");
			return;
		}
		this.numberOfOpenFeedbacks--;
	}

	public void setNumberOfOpenFeedbacks(Integer numberOfOpenFeedbacks) {
		this.numberOfOpenFeedbacks = numberOfOpenFeedbacks;
	}
	
	// theIsIncrement: if true, increment count; otherwise decrement
	public void adjustOpenEventCount(String theWellKnownTag, Boolean theIsIncrement) {
        EntityManager em = EMF.get().createEntityManager();
        
		try {
			em.getTransaction().begin();
    		Application app = (Application)em.createNamedQuery("Application.getByKey")
				.setParameter("key", this.getKey())
				.getSingleResult();
    		
    		if(theWellKnownTag.equalsIgnoreCase(Incident.LOG_TAG)) {
    			if(theIsIncrement) {app.incrementNumberOfOpenLogs();}
    			else               {app.decrementNumberOfOpenLogs();}
    		} else if(theWellKnownTag.equalsIgnoreCase(Incident.CRASH_TAG)) {
    			if(theIsIncrement) {app.incrementNumberOfOpenCrashes();}
    			else               {app.decrementNumberOfOpenCrashes();}
    		} else if(theWellKnownTag.equalsIgnoreCase(Incident.FEEDBACK_TAG)) {
    			if(theIsIncrement) {app.incrementNumberOfOpenFeedbacks();}
    			else               {app.decrementNumberOfOpenFeedbacks();}
    		}
    		em.persist(app);
    		em.getTransaction().commit();
		} catch (NoResultException e) {
			log.severe("adjustOpenEventCount(): should never happen -- could not get application by key using an application object!");
		} catch (NonUniqueResultException e) {
			log.severe("adjustOpenEventCount(): should never happen - two or more applications have same key");
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
		return;
	}

	public void incrementEndUserCount() {
        EntityManager em = EMF.get().createEntityManager();
        
		try {
    		Application app = (Application)em.createNamedQuery("Application.getByKey")
				.setParameter("key", this.getKey())
				.getSingleResult();
    		int currentNumberOfEndUsers = app.getNumberOfEndUsers() == null ? 0 : app.getNumberOfEndUsers();
    		currentNumberOfEndUsers += 1;
    		app.setNumberOfEndUsers(currentNumberOfEndUsers);
		} catch (NoResultException e) {
			log.severe("should never happen -- could not get application by key using an application object!");
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more applications have same key");
		}  finally {
			em.close();
		}
		return;
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
		}  finally {
			em.close();
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
		} finally {
			em.close();
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
		}  finally {
			em.close();
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
		} finally {
			em.close();
		}
		
		return application;
	}
	
	public int daysUntilAutoArchive() {
		// TODO allow application configuration to specify this
		return DAYS_UNTIL_AUTO_ARCHIVE;
	}
	
	// returns the next Incident number in sequence if successful; null otherwise
	public static Integer getAndIncrementIncidentNumber(String theApplicationId) {
        EntityManager em = EMF.get().createEntityManager();
        Integer nextIncidentNumber = null;
        Integer incidentNumber = null;
        em.getTransaction().begin();
        try {
			Key appKey = null;
			try {
				appKey = KeyFactory.stringToKey(theApplicationId);
			} catch (Exception e) {
				log.severe("could not convert application ID into a key");
				return null;
			}
			Application application = (Application)em.createNamedQuery("Application.getByKey")
				.setParameter("key", appKey)
				.getSingleResult();
			incidentNumber = application.getNextIncidentNumber();
			nextIncidentNumber = incidentNumber + 1;
			application.setNextIncidentNumber(nextIncidentNumber);
			em.persist(application);
			em.getTransaction().commit();
		} catch (NoResultException e) {
			log.severe("application ID not found");
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more applications have same key");
		} finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
		
		return incidentNumber;
	}
	
}
