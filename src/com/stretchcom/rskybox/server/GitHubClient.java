package com.stretchcom.rskybox.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

//****************************************************************************************
// ********************* Process to get OAuth Token from Github **************************
//****************************************************************************************
// 1. On github, go to 'account settings=>applications"
// 2. Register a new application with name 'rskybox'
// 3. Copy-and-paste the applications CLIENT_ID and CLIENT_SECRET to rskybox-rest-test
// 4. In rskybox-rest-test, manuallly run generateGithubToken() with 'rskybox' github
//    user's credentials (login:rskybox  password:redst0ne)
// 5. Copy-and-paste the OAuth token returned in JSON from github to this method below.
// 6. Done -- ready to go ...
// 7. Issues created in github will indicate they were created by github user 'rskybox'
//    because that's the github user whose credentials were used to get the OAuth token.
//****************************************************************************************
//****************************************************************************************


public class GitHubClient {
	private static final Logger log = Logger.getLogger(GitHubClient.class.getName());
	
	// HTTP Methods
	private static final String HTTP_PUT = "PUT";
	private static final String HTTP_POST = "POST";
	private static final String HTTP_GET = "GET";
	
	// Email-to-SMS URL
	private static final String GIT_HUB_BASE_URL = "https://api.github.com/";
	private static final String ISSUE_RESOURCE_URI = "issues";
	
	// created 'manually' in rskybox-rest-test and copy-and-pasted here. See full description of this process above.
	//private static final String GITHUB_OAUTH_TOKEN = "2465a575222a1c62e7c00a158be4221c1047b9ae"; // created by joepwro
	private static final String GITHUB_OAUTH_TOKEN = "0c81f80e27556e45d156405c94fc9e16df202b2d"; // created by rSkybox
	
	
	//curl -i https://api.github.com/user/repos?client_id=a4ec3edf0db5370523e1&client_secret=585340aabdb07a0f48c7ff36a36321e2600cf891
	//curl -i https://api.github.com/user/repos?access_token=2465a575222a1c62e7c00a158be4221c1047b9ae
	//curl -H "Authorization: bearer 2465a575222a1c62e7c00a158be4221c1047b9ae" https://api.github.com/users/joepwro -I
	//curl -i https://api.github.com/issues?access_token=2465a575222a1c62e7c00a158be4221c1047b9ae
	
	// list user's repositories
	//curl -i https://api.github.com/user/repos?access_token=2465a575222a1c62e7c00a158be4221c1047b9ae&type=member
	// list all respositories
	//curl -i https://api.github.com/repositories?access_token=2465a575222a1c62e7c00a158be4221c1047b9ae
	//curl -i https://api.github.com/repos/StretchComputing/arc/issues?access_token=2465a575222a1c62e7c00a158be4221c1047b9ae
	
	
	private static final int TEN_SECONDS_IN_MILLIS = 10000;
	
	
	// can return NULL
	static public String createIssue(String theOwner, String theRepo, String theTitle, String theBody) {
		log.info("GitHubClient::createIssue entered");
		
		//////////////////////////
		// Create the JSON Payload
		//////////////////////////
		JSONObject jsonReturn = new JSONObject();
		JSONObject jsonPayload = null;
		try {
			jsonPayload = new JSONObject();
			jsonPayload.put("title", theTitle);
			jsonPayload.put("body", theBody);
		} catch (JSONException e1) {
			log.severe("JSONException exception: " + e1.getMessage());
			return null;
		}
		
		String response = null;
		String issueUrl = null;
		String urlStr = GIT_HUB_BASE_URL + "repos/" + theOwner + "/" + theRepo + "/" + ISSUE_RESOURCE_URI + "?access_token=" + GITHUB_OAUTH_TOKEN;
				
		URL url = null;
		try {
			url = new URL(urlStr);
			response = send(url, HTTP_POST, jsonPayload.toString(), null, null);
			jsonReturn = new JSONObject(response);
			if(jsonReturn.has("html_url")) {
				issueUrl = jsonReturn.getString("html_url");
			}
		} catch (MalformedURLException e) {
			log.severe("MalformedURLException exception: " + e.getMessage());
		} catch (JSONException e) {
			log.severe("JSONException exception: " + e.getMessage());
        } 
		
		return issueUrl;
	}

	
	// theUrl: complete url
	// thePayload: the JSON payload to send, if any.  Can be null.
	// theHttpMethod: one of GET POST HEAD OPTIONS PUT DELETE TRACE
	static private String send(URL theUrl, String theHttpMethod, String theJsonPayload, 
			                   String theBasicAuthUserName, String theBasicAuthPassword) {
		log.info("GitHubClient::send theUrl = " + theUrl.toString());
		log.info("GitHubClient::send theJsonPayload = " + theJsonPayload);
		log.info("GitHubClient::send theHttpMethod = " + theHttpMethod);

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
			//connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setConnectTimeout(TEN_SECONDS_IN_MILLIS);
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("accept-encoding", "*/*");
			
			
			///////////////////////
			// Basic Authentication
			///////////////////////
			if(theBasicAuthUserName != null && theBasicAuthPassword != null) {
				StringBuilder buf = new StringBuilder(theBasicAuthUserName);
				buf.append(':');
				buf.append(theBasicAuthPassword);
				byte[] bytes = null;
				try {
					bytes = buf.toString().getBytes("ISO-8859-1");
				} catch (java.io.UnsupportedEncodingException uee) {
					log.severe("base64 encoding failed: " + uee.getMessage());
				}

				String header = "Basic " + Base64.encodeBase64String(bytes);
				connection.setRequestProperty("Authorization", header);
			}
			
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
				log.severe("GitHubClient::send() server returned error code: " + responseCode);
			}

		} catch (UnsupportedEncodingException ex) {
			log.severe("GitHubClient::send() UnsupportedEncodingException: " + ex);
		} catch (MalformedURLException ex) {
			log.severe("GitHubClient::send() MalformedURLException: " + ex);
		} catch (IOException ex) {
			log.severe("GitHubClient::send() IOException: " + ex);
		} finally {
			try {
				if (writer != null) {writer.close();}
			} catch (Exception ex) {
				log.severe("GitHubClient::send() Exception closing writer: " + ex);
			}
			try {
				if (reader != null) {reader.close();}
			} catch (Exception ex) {
				log.severe("GitHubClient::send() Exception closing reader: " + ex);
			}
			if (connection != null) {connection.disconnect();}
		}

		return response;
	}
}
