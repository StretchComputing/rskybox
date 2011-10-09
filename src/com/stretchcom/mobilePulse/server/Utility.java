package com.stretchcom.mobilePulse.server;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;

public class Utility {
    private static final Logger log = Logger.getLogger(Utility.class.getName());

    public static JsonRepresentation apiError(String theApiStatus){
    	if(theApiStatus == null) {
    		log.severe("Utility::apiError() illegal parameter");
    	}
    	
    	JSONObject json = new JSONObject();
    	try {
			json.put("apiStatus", theApiStatus);
		} catch (JSONException e) {
			log.severe("Utility::apiError()  exception = " + e.getMessage());
		}
		return new JsonRepresentation(json);
	}
}
