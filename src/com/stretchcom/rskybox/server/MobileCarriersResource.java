package com.stretchcom.rskybox.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import com.stretchcom.rskybox.models.MobileCarrier;

public class MobileCarriersResource extends ServerResource {
    private static final Logger log = Logger.getLogger(MobileCarriersResource.class.getName());

    @Override
    protected void doInit() throws ResourceException {
        log.info("UserResource in doInit");
    }

    // Handles OPTIONS for Get MobileCarriers API
    @Options
    public Representation options() {
        return null;
    }

    // Handles 'Get List of MobileCarrier API'
    @Get("json")
    public JsonRepresentation get(Variant variant) {
    	log.info("Get List of MobileCarriers API");
    	return index();
    }
    
    private JsonRepresentation index() {
        log.info("MobileCarrierResource in index");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_OK);
        try {
            List<MobileCarrier> mobileCarriers = MobileCarrier.getList();
            JSONArray ja = new JSONArray();
            for (MobileCarrier mc : mobileCarriers) {
                ja.put(getMobileCarrierJson(mc));
            }
            json.put("mobileCarriers", ja);
            json.put("apiStatus", apiStatus);
        } catch (JSONException e) {
            log.severe("exception = " + e.getMessage());
        	e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return new JsonRepresentation(json);
    }
    
    private JSONObject getMobileCarrierJson(MobileCarrier mobileCarrier) {
    	return getMobileCarrierJson(mobileCarrier, null);
    }

    private JSONObject getMobileCarrierJson(MobileCarrier mobileCarrier, String theApiStatus) {
        JSONObject json = new JSONObject();

        try {
        	if(theApiStatus != null) {
        		json.put("apiStatus", theApiStatus);
        	}
        	if(theApiStatus == null || (theApiStatus !=null && theApiStatus.equals(ApiStatusCode.SUCCESS))) {
                json.put("id", mobileCarrier.getCode());
                json.put("name", mobileCarrier.getName());
        	}
        } catch (JSONException e) {
        	log.severe("MobileCarrierResrouce::getUserJson() error creating JSON return object. Exception = " + e.getMessage());
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
}
