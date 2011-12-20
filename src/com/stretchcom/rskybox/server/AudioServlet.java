package com.stretchcom.rskybox.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.google.appengine.api.blobstore.ByteRange;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.stretchcom.rskybox.models.AppMember;
import com.stretchcom.rskybox.models.Feedback;
import com.stretchcom.rskybox.models.User;

@SuppressWarnings("serial")
public class AudioServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(AudioServlet.class.getName());
    private static final String AUDIO_EXT = ".mp4";

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("AudioServlet.doPost() entered - SHOULD NOT BE CALLED!!!!!!!!!!!!!!!!!");
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("AudioServlet.doGet() entered");
        ServletOutputStream out = null;
        resp.setContentType("audio/mp4");

        try {
    		
        	//////////////////////
        	// Authorization Rules
        	//////////////////////
    		User currentUser = (User)req.getAttribute(RskyboxApplication.CURRENT_USER);
    		log.info("currentUser from filter = " + currentUser);
    		
    		List<String> pathIds = this.getPathIds(req);
    		if (pathIds == null || pathIds.size() < 2 ) {
    			log.info("could not extract application ID or feedbackId from URL");
    			return;
    		}
    		String applicationId = pathIds.get(0);
    		log.info("application ID = " + applicationId);
    		String feedbackId = pathIds.get(1);
    		log.info("feedback ID = " + feedbackId);
    		
        	AppMember currentUserMember = AppMember.getAppMember(applicationId, KeyFactory.keyToString(currentUser.getKey()));
        	if(currentUserMember == null) {
        		log.info("current user is not a member of the application");
    			return;
        	}

            byte[] voice = getFeedbackAudio(feedbackId);
            if (voice == null)
                return;

            byte[] voice_output = voice;
            resp.setContentLength(voice.length);
            resp.setHeader("Content-Range", "bytes 0-" + (voice.length - 1) + "/" + voice.length);
            resp.setHeader("Accept-Ranges", "bytes");
            if (req.getHeader("Range") != null) {
                ByteRange range = ByteRange.parse(req.getHeader("Range"));
                if (range.hasEnd()) {
                    int range_length = (int)(range.getEnd() - range.getStart() + 1);
                    voice_output = new byte[range_length];
                    resp.setHeader("Content-Range", "bytes " + range.getStart() + "-" + range.getEnd() + "/" + voice.length);
                    resp.setContentLength(range_length);
                    resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                    System.arraycopy(voice, (int)range.getStart(), voice_output, 0, range_length);
                } else {
                    resp.setHeader("Content-Range", "bytes " + range.getStart() + "-" + (voice.length - 1) + "/" + voice.length);
                    resp.setContentLength(voice.length);
                }
            }

            out = resp.getOutputStream();
            out.write(voice_output);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Servlet exception = " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }
    
	// returns applicationID and feedbackID in List or null if error
	private List<String> getPathIds(HttpServletRequest theReq) {
		// extract the crash detect ID from the URL (for http://hostname.com/mywebapp/servlet/MyServlet/a/b;c=123?d=789, returns /a/b;c=123
		String pathInfo = theReq.getPathInfo();
		log.info("get Feedback Audio URL pathInfo = " + pathInfo);
		if(pathInfo == null || pathInfo.length() == 0) {return null;}
		
		// if all is going well, pathInfo should have the following format:  /<applicationId>/<feedbackId>.mp4
		if(pathInfo.startsWith("/")) {
			pathInfo = pathInfo.substring(1);
		}
		StringTokenizer st = new StringTokenizer(pathInfo, "/");
		List<String> pathIds = new ArrayList<String>();
		while (st.hasMoreElements()) {
			String token = st.nextToken();
			
			// strip the file extension if there is one
			if(token.endsWith(AUDIO_EXT)) {
			    int extIndex = token.indexOf(AUDIO_EXT);
				token = token.substring(0, extIndex);
			}
			pathIds.add(token);
		}
		return pathIds;
	}

    // returns base64 decoded audio data of the specified feedback record if
    // successful; null otherwise.
    private byte[] getFeedbackAudio(String theFeedbackId) {
        byte[] rawAudio = null;

        // using the feedbackID, retrieve the appropriate feedback record
        EntityManager em = EMF.get().createEntityManager();
        try {
            Key feedbackKey;
			try {
				feedbackKey = KeyFactory.stringToKey(theFeedbackId);
			} catch (Exception e) {
				log.severe("exception = " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
            Feedback feedback = null;
            feedback = (Feedback) em.createNamedQuery("Feedback.getByKey").setParameter("key", feedbackKey)
                .getSingleResult();

            rawAudio = Base64.decodeBase64(feedback.getVoiceBase64());
        } catch (NoResultException e) {
            // feedback ID passed in is not valid
            log.info("Feedback ID not found");
        } catch (NonUniqueResultException e) {
            log.severe("should never happen - two or more feedback have same key");
        }

        return rawAudio;
    }
}
