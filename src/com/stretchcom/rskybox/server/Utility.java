package com.stretchcom.rskybox.server;

import java.security.MessageDigest;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.ext.servlet.internal.ServletCall;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.stretchcom.rskybox.models.Application;
import com.stretchcom.rskybox.models.User;

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
	
	public static String extractAllDigits(String theInputString) {
		// remove all non-digits from the string
		theInputString = theInputString.replaceAll("\\D", "");
		return theInputString;
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
	
	public static String verifyUserAuthorizedForApplication(Request theRequest, String theApplicationId) {
		log.info("entered verifyUserAuthorizedForApplication()");
    	String appIdStatus = Application.verifyApplicationId(theApplicationId);
    	if(!appIdStatus.equalsIgnoreCase(ApiStatusCode.SUCCESS)) {
    		return appIdStatus;
    	}
    	
    	// TODO access the currentUser which is created by authenticationFilter
//    	HttpServletRequest servletRequest = ServletUtils.getRequest(getRequest());
//    	String joeTest = (String)servletRequest.getAttribute(RskyboxApplication.CURRENT_USER);
    	//appIdStatus = User.verifyUserMemberOfApplication(currentUser.getEmail(), theApplicationId);
    	
    	return appIdStatus;
	}
	
	public static User getCurrentUser(Request theRequest) {
    	HttpServletRequest servletRequest = ServletUtils.getRequest(theRequest);
    	return (User)servletRequest.getAttribute(RskyboxApplication.CURRENT_USER);
	}

	public static String encrypt(String thePlainText) {
		String encryptedText = null;
		MessageDigest md = null;
		try {
			// use SHA encryption algorithm
			md = MessageDigest.getInstance("SHA");
			
			// convert input plain text into UTF-8 encoded bytes
			md.update(thePlainText.getBytes("UTF-8"));
			
			// extract the encrypted bytes
			byte raw[] = md.digest();
			
			// convert encrypted bytes to base64 encoded string so data can be stored in the database
			encryptedText = Base64.encode(raw);
		} catch (Exception e) {
			log.severe("Utility::encrypt() exception = " + e.getMessage());
		}
		return encryptedText;
	}
}
