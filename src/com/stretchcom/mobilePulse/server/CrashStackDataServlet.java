package com.stretchcom.mobilePulse.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
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

import org.restlet.data.Status;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.appengine.repackaged.com.google.common.util.Base64DecoderException;

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
		
		// create a file name based on today's date
		Format formatter = new SimpleDateFormat("MM_dd_yy");
		Date todayDate = new Date();
		String todayDateFormatted = formatter.format(todayDate);
		String fileName = todayDateFormatted + FILE_EXT;
		log.info("crash data file name = " + fileName);
		resp.addHeader("Content-Disposition", "attachment; filename=" + fileName);

		try {
			String crashDetectId = this.getCrashDetectId(req);
			if (crashDetectId == null) {
				log.info("could not extract crashDetectId from URL");
				return;
			}

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
	
	// extracts crash detect id from URL
	// returns crashDetectID or null if error
	private String getCrashDetectId(HttpServletRequest theReq) {
		// extract the crash detect ID from the URL (for http://hostname.com/mywebapp/servlet/MyServlet/a/b;c=123?d=789, returns /a/b;c=123
		String pathInfo = theReq.getPathInfo();
		log.info("get Crash Stack Data URL pathInfo = " + pathInfo);
		if(pathInfo == null || pathInfo.length() == 0) {return null;}
		if(pathInfo.startsWith("/") && pathInfo.endsWith(FILE_EXT)) {
		    int extIndex = pathInfo.indexOf(FILE_EXT);
			pathInfo = pathInfo.substring(1, extIndex);
		}
		return pathInfo;
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
