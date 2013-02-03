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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.stretchcom.rskybox.server.ApiStatusCode;
import com.stretchcom.rskybox.server.EMF;
import com.stretchcom.rskybox.server.GMT;

@Entity
@NamedQueries({
    @NamedQuery(
    		name="Stream.getByKey",
    		query="SELECT s FROM Stream s WHERE s.key = :key"
    ),
    @NamedQuery(
    		name="Stream.getAll",
    		query="SELECT s FROM Stream s ORDER BY s.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="Stream.getAllWithApplicationId",
    		query="SELECT s FROM Stream s WHERE s.applicationId = :applicationId ORDER BY s.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="Stream.getByStatus",
    		query="SELECT s FROM Stream s WHERE s.status = :status  ORDER BY s.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="Stream.getByApplicationId",
    		query="SELECT s FROM Stream s WHERE s.applicationId = :applicationId"
    ),
    @NamedQuery(
    		name="Stream.getByStatusAndApplicationId",
    		query="SELECT s FROM Stream s WHERE s.status = :status and s.applicationId = :applicationId ORDER BY s.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="Stream.getByNameAndApplicationId",
    		query="SELECT s FROM Stream s WHERE s.name = :name and s.applicationId = :applicationId ORDER BY s.createdGmtDate DESC"
    ),
    @NamedQuery(
    		name="Stream.getByNameAndNotStatusApplicationId",
    		query="SELECT s FROM Stream s WHERE s.name = :name and s.status <> :status and s.applicationId = :applicationId"
    ),
})
public class Stream {
	private static final Logger log = Logger.getLogger(Stream.class.getName());
	
	public final static String INIT_STATUS = "init";
	public final static String OPEN_STATUS = "open";
	public final static String CLOSED_STATUS = "closed";
	public final static String ALL_STATUS = "all";

	private String applicationId;
	private String endUserId;
	private String memberId;
	private Date createdGmtDate;
	private String status;
	private String name;

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

	public String getEndUserId() {
		return endUserId;
	}

	public void setEndUserId(String endUserId) {
		this.endUserId = endUserId;
	}
	
	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public static Boolean isStatusValid(String theStatus) {
		if(theStatus.equals(Stream.INIT_STATUS) || theStatus.equals(Stream.OPEN_STATUS) || theStatus.equals(Stream.CLOSED_STATUS)) return true;
		return false;
	}
	
	public static Boolean isUpdateStatusValid(String theStatus) {
		if(theStatus.equals(Stream.CLOSED_STATUS)) return true;
		return false;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	
    public static JSONObject getJson(Stream clientLog, Boolean isList) {
    	return getJson(clientLog, null, isList);
    }

    public static JSONObject getJson(Stream stream, String theApiStatus, Boolean isList) {
        JSONObject json = new JSONObject();

        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(stream != null && (theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS)))) {
        		json.put("id", KeyFactory.keyToString(stream.getKey()));
            	Date createdDate = stream.getCreatedGmtDate();
            	if(createdDate != null) {
            		json.put("date", GMT.convertToIsoDate(createdDate));
            	}
            	json.put("endUserId", stream.getEndUserId());
            	json.put("memberId", stream.getMemberId());
            	json.put("status", stream.getStatus());
            	json.put("appId", stream.getApplicationId());
        	}
        } catch (JSONException e) {
        	log.severe("UsersResrouce::getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            return null;
        }
        return json;
    }
    
	
	// Returns list of users matching specified email address
	public static List<Stream> getByNameAndNotClosed(String theName, String theApplicationId) {
        EntityManager em = EMF.get().createEntityManager();
        List<Stream> streams = null;

		try {
    		streams = (List<Stream>)em.createNamedQuery("Stream.getByNameAndNotStatusApplicationId")
				.setParameter("name", theName)
				.setParameter("status", Stream.CLOSED_STATUS)
				.setParameter("applicationId", theApplicationId)
				.getResultList();
		} catch (Exception e) {
			log.severe("exception = " + e.getMessage());
		} finally {
			em.close();
		}
		return streams;
	}
	
	public static String getLowMarkerKey(String theApplicationId, String theStreamId) {
		String idFragment = theApplicationId + "_" + theStreamId;
		return "low_" + idFragment;
	}
	
	public static String getHighMarkerKey(String theApplicationId, String theStreamId) {
		String idFragment = theApplicationId + "_" + theStreamId;
		return "high_" + idFragment;
	}
	
	public static String getPacketKey(Integer theSequence, String theApplicationId, String theStreamId) {
		return theSequence.toString() + "_" + theApplicationId + "_" + theStreamId;
	}
	
	// lowMarker: sequence of next packet to be consumed
	// highMarker: sequence of where next produced packet will go
	// if lowMarker == highMarker, there are no consumable packets
	public static void createMarkers(String theApplicationId, String theStreamId) {
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		memcache.put(getLowMarkerKey(theApplicationId, theStreamId), 0);
		memcache.put(getHighMarkerKey(theApplicationId, theStreamId), 0);
	}
	
	public static void deleteMarkers(String theApplicationId, String theStreamId) {
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		memcache.delete(getLowMarkerKey(theApplicationId, theStreamId));
		memcache.delete(getHighMarkerKey(theApplicationId, theStreamId));
	}
	
	public static Boolean producePacket(String theApplicationId, String theStreamId, String theBody) {
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		Boolean wasProduced = true;
		
		String highMarkerKey = getHighMarkerKey(theApplicationId, theStreamId);
		if(memcache.contains(highMarkerKey)) {
			int nextSequence = (Integer)memcache.get(highMarkerKey);
			String packetKey = getPacketKey(nextSequence, theApplicationId, theStreamId);
			memcache.put(packetKey, theBody);
			memcache.increment(highMarkerKey, 1);
		} else {
			wasProduced = false;
		}
		
		return wasProduced;
	}
	
	public static List<String> consumePackets(String theApplicationId, String theStreamId) {
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		List<String> packets = new ArrayList<String>();

		String highMarkerKey = getHighMarkerKey(theApplicationId, theStreamId);
		String lowMarkerKey = getLowMarkerKey(theApplicationId, theStreamId);
		if(memcache.contains(highMarkerKey) && memcache.contains(lowMarkerKey)) {
			int lowSequence = (Integer)memcache.get(lowMarkerKey);
			int highSequence = (Integer)memcache.get(highMarkerKey);
			
			int numberOfConsumablePackets = highSequence - lowSequence;
			for(int i=0; i<numberOfConsumablePackets; i++) {
				int sequence = lowSequence + i;
				String packetKey = getPacketKey(sequence, theApplicationId, theStreamId);
				packets.add((String)memcache.get(packetKey));
				memcache.delete(packetKey);
			}
			memcache.increment(lowMarkerKey, numberOfConsumablePackets);
		} else {
			return null;
		}
			
		return packets;
	}
}
