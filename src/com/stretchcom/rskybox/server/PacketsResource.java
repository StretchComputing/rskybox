package com.stretchcom.rskybox.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.stretchcom.rskybox.models.AppAction;
import com.stretchcom.rskybox.models.AppMember;
import com.stretchcom.rskybox.models.Application;
import com.stretchcom.rskybox.models.ClientLog;
import com.stretchcom.rskybox.models.ClientLogRemoteControl;
import com.stretchcom.rskybox.models.CrashDetect;
import com.stretchcom.rskybox.models.Incident;
import com.stretchcom.rskybox.models.Notification;
import com.stretchcom.rskybox.models.Stream;
import com.stretchcom.rskybox.models.User;

public class PacketsResource extends ServerResource {
	private static final Logger log = Logger.getLogger(PacketsResource.class.getName());
	private String streamId;
	private String applicationId;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        this.streamId = (String) getRequest().getAttributes().get("streamId");
        this.applicationId = (String) getRequest().getAttributes().get("applicationId");
    }

    // Handles 'Get List of Packets API'
    @Get("json")
    public JsonRepresentation get(Variant variant) {
    	 log.info("Get List of Packets API");
         return index();
    }

    // Handles 'Packet OPTIONS'
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

    // Handles 'Create Packet API'
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("in post");
		if(this.applicationId == null) {return Utility.apiError(this, ApiStatusCode.APPLICATION_ID_REQUIRED);}
        return save_packet(entity);
    }
    
    private JsonRepresentation index() {
        log.info("in index");
        JSONObject json = new JSONObject();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
        try {
        	List<String> packets = Stream.consumePackets(this.applicationId, this.streamId);
            if(packets == null) {
            	return Utility.apiError(this, ApiStatusCode.STREAM_CLOSED);
            }
        	
        	JSONArray packetsJsonArray = new JSONArray();
        	for(String packet : packets) {
        		JSONObject packetJsonObj = new JSONObject();
        		packetJsonObj.put("body", packet);
        		packetsJsonArray.put(packetJsonObj);
        	}
        	json.put("packets", packetsJsonArray);
            json.put("apiStatus", apiStatus);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return new JsonRepresentation(json);
    }

    private JsonRepresentation save_packet(Representation entity) {
        JSONObject jsonReturn = new JSONObject();

		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
        try {
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            String body = null;
            
            if(json.has("body")) {
				body = json.getString("body").toLowerCase();
			} else {
				return Utility.apiError(this, ApiStatusCode.BODY_IS_REQUIRED);
			}
            
            Boolean wasProduced = Stream.producePacket(this.applicationId, this.streamId, body);
            if(!wasProduced) {
            	return Utility.apiError(this, ApiStatusCode.STREAM_CLOSED);
            }
        } catch (IOException e) {
            log.severe("error extracting JSON object from Post. exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        
	    try {
	    	jsonReturn.put("apiStatus", apiStatus);
	    } catch (JSONException e) {
	        log.severe("exception = " + e.getMessage());
	        this.setStatus(Status.SERVER_ERROR_INTERNAL);
	    }
	    return new JsonRepresentation(jsonReturn);
    }
    
}
