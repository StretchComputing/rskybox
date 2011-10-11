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
	
	// returns true if all characters are digits
	public static Boolean isPhoneNumber(String thePotentialNumber) {
		if(thePotentialNumber == null) {return false;}
		int originalSize = thePotentialNumber.length();
		
		// remove all non-digits from the string
		thePotentialNumber = thePotentialNumber.replaceAll("\\D", "");
		int modifiedSize = thePotentialNumber.length();
		return originalSize == modifiedSize;
	}
	
	public static Boolean doesEmailAddressStartWithPhoneNumber(String theEmailAddress) {
		if(theEmailAddress == null) {return false;}
		
		int index = theEmailAddress.indexOf("@");
		if(index >= 1) {
			String potentialPhoneNumber = theEmailAddress.substring(0, index);
			if(isPhoneNumber(potentialPhoneNumber)) {
				return true;
			}
		}
		
		return false;
	}
	
	// returns email domain name (with the leading "@") if found, otherwise returns null
	public static String getEmailDomainNameFromSmsEmailAddress(String theSmsEmailAddress) {
		if(theSmsEmailAddress == null) {return null;}
		
		int index = theSmsEmailAddress.indexOf("@");
		if(index >= 0) {
			String emailDomainName = theSmsEmailAddress.substring(index);
			return emailDomainName;
		}
		return null;
	}
}
