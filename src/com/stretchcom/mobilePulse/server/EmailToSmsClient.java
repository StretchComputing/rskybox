package com.stretchcom.mobilePulse.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.google.appengine.repackaged.com.google.common.util.Base64;

public class EmailToSmsClient {
	private static final Logger log = Logger.getLogger(EmailToSmsClient.class.getName());
	
	// HTTP Methods
	private static final String HTTP_PUT = "PUT";
	private static final String HTTP_POST = "POST";
	private static final String HTTP_GET = "GET";
	
	// Email-to-SMS URL
	private static final String EMAIL_TO_SMS_BASE_URL = "https://50.57.64.254:8443/rTeamSms/";
	//private static final String EMAIL_TO_SMS_BASE_URL = "http://50.57.64.254:8080/rTeamSms/";
	private static final String EMAIL_RESOURCE_URI = "email";
	private static final String IS_ALIVE_RESOURCE_URI = "vitals";
	
	private static final String BASIC_AUTH_USER_NAME = "rTeamLogin";
	private static final String BASIC_AUTH_PASSWORD = "test123";
	
	private static final int TEN_SECONDS_IN_MILLIS = 10000;
	
	
	// can return NULL
	static public String sendMail(String theSubject, String theBody, String theToEmailAddress, String theFromEmailAddress) {
		log.info("EmailToSmsClient::sendMail entered");
		
		//////////////////////////
		// Create the JSON Payload
		//////////////////////////
		JSONObject jsonPayload = null;
		try {
			jsonPayload = new JSONObject();
			jsonPayload.put("subject", theSubject);
			jsonPayload.put("body", theBody);
			jsonPayload.put("toEmailAddress", theToEmailAddress);
			jsonPayload.put("fromEmailAddress", theFromEmailAddress);
		} catch (JSONException e1) {
			log.severe("JSONException exception: " + e1.getMessage());
			return null;
		}
		
		String response = null;
		String urlStr = EMAIL_TO_SMS_BASE_URL + EMAIL_RESOURCE_URI;
		URL url = null;
		try {
			url = new URL(urlStr);
			response = send(url, HTTP_POST, jsonPayload.toString(), BASIC_AUTH_USER_NAME, BASIC_AUTH_PASSWORD);
		} catch (MalformedURLException e) {
			log.severe("MalformedURLException exception: " + e.getMessage());
		}
		
		return response;
	}
	
	
	// can return NULL
	static public String isAlive() {
		log.info("EmailToSmsClient::isAlive entered");
		
		//////////////////////////
		// Create the JSON Payload
		//////////////////////////
		String response = null;
		String urlStr = EMAIL_TO_SMS_BASE_URL + IS_ALIVE_RESOURCE_URI;
		URL url = null;
		try {
			url = new URL(urlStr);
			response = send(url, HTTP_GET, null, BASIC_AUTH_USER_NAME, BASIC_AUTH_PASSWORD);
		} catch (MalformedURLException e) {
			log.severe("MalformedURLException exception: " + e.getMessage());
		}
		
		return response;
	}

	
	// theUrl: complete url
	// thePayload: the JSON payload to send, if any.  Can be null.
	// theHttpMethod: one of GET POST HEAD OPTIONS PUT DELETE TRACE
	static private String send(URL theUrl, String theHttpMethod, String theJsonPayload, 
			                   String theBasicAuthUserName, String theBasicAuthPassword) {
		log.info("EmailToSmsClient::send theUrl = " + theUrl.toString());
		log.info("EmailToSmsClient::send theJsonPayload = " + theJsonPayload);
		log.info("EmailToSmsClient::send theHttpMethod = " + theHttpMethod);

		String response = "";
		HttpURLConnection connection = null;
		OutputStreamWriter writer = null;
		InputStreamReader reader = null;
		try {
			/////////////////////
			// Prepare connection
			/////////////////////
			connection = (HttpURLConnection)theUrl.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setAllowUserInteraction(false);
			connection.setRequestMethod(theHttpMethod);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setConnectTimeout(TEN_SECONDS_IN_MILLIS);
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("accept-encoding", "*/*");
			
			
			///////////////////////
			// Basic Authentication
			///////////////////////
			StringBuilder buf = new StringBuilder(theBasicAuthUserName);
			buf.append(':');
			buf.append(theBasicAuthPassword);
			byte[] bytes = null;
			try {
				bytes = buf.toString().getBytes("ISO-8859-1");
			} catch (java.io.UnsupportedEncodingException uee) {
				log.severe("base64 encoding failed: " + uee.getMessage());
			}

			String header = "Basic " + Base64.encode(bytes);
			connection.setRequestProperty("Authorization", header);

			////////////////////
			// Send HTTP Request
			////////////////////
			connection.connect();
			
			if(theJsonPayload == null) {
				theJsonPayload = "{}";
			}
			if(!theHttpMethod.equalsIgnoreCase(HTTP_GET)) {
				writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
				writer.write(theJsonPayload);
				writer.flush();
			}

			////////////////////
			// Get HTTP response
			////////////////////
			int responseCode = connection.getResponseCode();
			log.info("responseCode = " + responseCode);
			
			if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
				// read-back the response
				reader = new InputStreamReader(connection.getInputStream());
				BufferedReader in = new BufferedReader(reader);
				StringBuffer responseBuffer = new StringBuffer();
				while (true) {
					String inputLine = in.readLine();
					if(inputLine == null) {break;}
					responseBuffer.append(inputLine);
				}
				in.close();
				response = responseBuffer.toString();
			} else // Server returned HTTP error code.
			{
				log.severe("EmailToSmsClient::send() server returned error code: " + responseCode);
			}

		} catch (UnsupportedEncodingException ex) {
			log.severe("EmailToSmsClient::send() UnsupportedEncodingException: " + ex);
		} catch (MalformedURLException ex) {
			log.severe("EmailToSmsClient::send() MalformedURLException: " + ex);
		} catch (IOException ex) {
			log.severe("EmailToSmsClient::send() IOException: " + ex);
		} finally {
			try {
				if (writer != null) {writer.close();}
			} catch (Exception ex) {
				log.severe("EmailToSmsClient::send() Exception closing writer: " + ex);
			}
			try {
				if (reader != null) {reader.close();}
			} catch (Exception ex) {
				log.severe("EmailToSmsClient::send() Exception closing reader: " + ex);
			}
			if (connection != null) {connection.disconnect();}
		}

		return response;
	}
}
