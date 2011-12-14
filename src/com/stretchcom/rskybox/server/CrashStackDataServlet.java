package com.stretchcom.rskybox.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.appengine.repackaged.com.google.common.util.Base64DecoderException;
import com.stretchcom.rskybox.models.AppMember;
import com.stretchcom.rskybox.models.CrashDetect;
import com.stretchcom.rskybox.models.User;

public class CrashStackDataServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(CrashStackDataServlet.class.getName());
	public static final String FILE_EXT = ".plcrash";

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		log.info("CrashStackDataServlet.doPost() entered - SHOULD NOT BE CALLED!!!!!!!!!!!!!!!!!");
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.info("CrashStackDataServlet.doGet() entered");
		ServletOutputStream out = null;
		resp.setContentType("application/octet-stream");
		JSONObject json = new JSONObject();
		
    	//////////////////////
    	// Authorization Rules
    	//////////////////////
		User currentUser = (User)req.getAttribute(RskyboxApplication.CURRENT_USER);
		log.info("currentUser from filter = " + currentUser);
		List<String> pathIds = this.getPathIds(req);
		if (pathIds == null || pathIds.size() < 2 ) {
			log.info("could not extract application ID or crashDetectId from URL");
			return;
		}
		String applicationId = pathIds.get(0);
		log.info("application ID = " + applicationId);
		String crashDetectId = pathIds.get(1);
		log.info("crashDetect ID = " + crashDetectId);
		
    	AppMember currentUserMember = AppMember.getAppMember(applicationId, KeyFactory.keyToString(currentUser.getKey()));
    	if(currentUserMember == null) {
    		log.info("current user is not a member of the application");
//    		try {
//				json.put("apiStatus", ApiStatusCode.USER_NOT_AUTHORIZED_FOR_APPLICATION);
//				byte[] byteArray = json.toString().getBytes("UTF-8");
//				out = resp.getOutputStream();
//				out.write(byteArray);
//			} catch (JSONException e) {
//				log.severe("exception = " + e.getMessage());
//				e.printStackTrace();
//			} finally {
//				if (out != null) {
//					out.flush();
//					out.close();
//				}
//			}
			return;
    	}
		
		
		// create a file name based on today's date
		Format formatter = new SimpleDateFormat("MM_dd_yy");
		Date todayDate = new Date();
		String todayDateFormatted = formatter.format(todayDate);
		String fileName = todayDateFormatted + FILE_EXT;
		log.info("crash data file name = " + fileName);
		resp.addHeader("Content-Disposition", "attachment; filename=" + fileName);

		try {
			byte[] crashStackData = getCrashStackData(crashDetectId);
			if (crashStackData == null)
				return;

			out = resp.getOutputStream();
			out.write(crashStackData);
		} catch (Exception e) {
			log.info("Servlet exception = " + e.getMessage());
			resp.setStatus(HttpServletResponse.SC_OK);
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}
	
	// returns applicationID and crashDetectID in List or null if error
	private List<String> getPathIds(HttpServletRequest theReq) {
		// extract the crash detect ID from the URL (for http://hostname.com/mywebapp/servlet/MyServlet/a/b;c=123?d=789, returns /a/b;c=123
		String pathInfo = theReq.getPathInfo();
		log.info("get Crash Stack Data URL pathInfo = " + pathInfo);
		if(pathInfo == null || pathInfo.length() == 0) {return null;}
		
		// if all is going well, pathInfo should have the following format:  /<applicationId>/<crashDetectId>.plcrash
		if(pathInfo.startsWith("/")) {
			pathInfo = pathInfo.substring(1);
		}
		StringTokenizer st = new StringTokenizer(pathInfo, "/");
		List<String> pathIds = new ArrayList<String>();
		while (st.hasMoreElements()) {
			String token = st.nextToken();
			
			// strip the file extension if there is one
			if(token.endsWith(FILE_EXT)) {
			    int extIndex = token.indexOf(FILE_EXT);
				token = token.substring(0, extIndex);
			}
			pathIds.add(token);
		}
		return pathIds;
	}
	
	// returns base64 decoded audio data of the specified feedback record if successful; null otherwise.
	private byte[] getCrashStackData(String theCrashDetectId) {
		byte[] rawCrashStackData = null;
		
		// using the feedbackID, retrieve the appropriate feedback record
       	EntityManager em = EMF.get().createEntityManager();
		try {
			Key feedbackKey = KeyFactory.stringToKey(theCrashDetectId);
    		CrashDetect crashDetect = null;
    		crashDetect = (CrashDetect)em.createNamedQuery("CrashDetect.getByKey")
				.setParameter("key", feedbackKey)
				.getSingleResult();
    		
    		rawCrashStackData = Base64.decode(crashDetect.getStackDataBase64());    		
		} catch (NoResultException e) {
			// crash detect ID passed in is not valid
			log.info("Crash Detect ID not found");
		} catch (NonUniqueResultException e) {
			log.severe("should never happen - two or more crash detects have same key");
		} catch (Base64DecoderException e) {
			log.severe("base64 decode exception = " + e.getMessage());
			e.printStackTrace();
		} 
		
		return rawCrashStackData;
	}
}
