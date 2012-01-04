package com.stretchcom.rskybox.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.stretchcom.rskybox.server.EMF;

@Entity
@NamedQueries({
    @NamedQuery(
    		name="ClientLogRemoteControl.getAll",
    		query="SELECT clrc FROM ClientLogRemoteControl clrc"
    ),
    @NamedQuery(
    		name="ClientLogRemoteControl.getAllWithApplicationId",
    		query="SELECT clrc FROM ClientLogRemoteControl clrc WHERE clrc.applicationId = :applicationId"
    ),
    @NamedQuery(
    		name="ClientLogRemoteControl.getByApplicationIdAndLogName",
    		query="SELECT clrc FROM ClientLogRemoteControl clrc WHERE clrc.applicationId = :applicationId and clrc.logName = :logName"
    ),
})
public class ClientLogRemoteControl {
	private static final Logger log = Logger.getLogger(ClientLogRemoteControl.class.getName());
	
	public final static String ACITVE_MODE = "active";
	public final static String INACTIVE_MODE = "inactive";

	private String logName;
	private String mode;
	private Date modeModifiedGmtDate;
	private String applicationId;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    public Key getKey() {
        return key;
    }
	
	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}
	
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public Date getModeModifiedGmtDate() {
		return modeModifiedGmtDate;
	}

	public void setModeModifiedGmtDate(Date modeModifiedGmtDate) {
		this.modeModifiedGmtDate = modeModifiedGmtDate;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	
	public static Boolean isModeValid(String theMode) {
		if(theMode.equals(ClientLogRemoteControl.ACITVE_MODE) || theMode.equals(ClientLogRemoteControl.INACTIVE_MODE)) return true;
		return false;
	}
	
	// updates the ClientLogRemoteControl if it exists; otherwise it creates a new ClientLogRemoteControl
	public static ClientLogRemoteControl update(String theApplicationId, String theClientLogName, String theMode) {
		if(theApplicationId == null || theClientLogName == null) {
			log.severe("ClientLogRemoteControl::update() has a null parameter");
		}
		
        EntityManager em = EMF.get().createEntityManager();
        ClientLogRemoteControl clientLogRemoteControl = null;
        
        em.getTransaction().begin();
		try {
			String originalMode = null;
			try {
				clientLogRemoteControl = (ClientLogRemoteControl)em.createNamedQuery("ClientLogRemoteControl.getByApplicationIdAndLogName")
						.setParameter("applicationId", theApplicationId)
						.setParameter("logName", theClientLogName)
						.getSingleResult();
				originalMode = clientLogRemoteControl.getMode();
			} catch (NoResultException e) {
				////////////////////////////////////////////////////////
				// NOT an error -- no clientLogRemoteControl created yet
				////////////////////////////////////////////////////////
				clientLogRemoteControl = new ClientLogRemoteControl();
				clientLogRemoteControl.setApplicationId(theApplicationId);
				clientLogRemoteControl.setLogName(theClientLogName);
				clientLogRemoteControl.setModeModifiedGmtDate(new Date());
			} catch (NonUniqueResultException e) {
				log.severe("should never happen - two or more clientLogRemoteControl entities have same applicationId and logName");
			}
			
			clientLogRemoteControl.setMode(theMode);
			if(originalMode != null && !originalMode.equalsIgnoreCase(theMode)) {
				clientLogRemoteControl.setModeModifiedGmtDate(new Date());
			}
			
			em.persist(clientLogRemoteControl);
			em.getTransaction().commit();
		}  finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
		return clientLogRemoteControl;
	}
	
	
	public static ClientLogRemoteControl getEntity(String theApplicationId, String theClientLogName) {
		if(theApplicationId == null || theClientLogName == null) {
			log.severe("ClientLogRemoteControl::get() has a null parameter");
			return null;
		}
		
        EntityManager em = EMF.get().createEntityManager();
        ClientLogRemoteControl clientLogRemoteControl = null;
		try {
			clientLogRemoteControl = (ClientLogRemoteControl)em.createNamedQuery("ClientLogRemoteControl.getByApplicationIdAndLogName")
					.setParameter("applicationId", theApplicationId)
					.setParameter("logName", theClientLogName)
					.getSingleResult();
		} catch (NoResultException e) {
			// NOT an error -- at any given time, clientLogRemoteControl may not have been created yet
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more clientLogRemoteControl entities have same applicationId and logName");
		}
		
		return clientLogRemoteControl;
	}
}
