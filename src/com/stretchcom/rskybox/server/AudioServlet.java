package com.stretchcom.rskybox.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.ByteRange;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.appengine.repackaged.com.google.common.util.Base64DecoderException;
import com.stretchcom.rskybox.models.Feedback;

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
            String feedbackId = this.getFeedbackId(req);
            if (feedbackId == null) {
                log.info("could not extract feedbackID from URL");
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

    // extracts feedback id from URL
    // returns feedbackID or null if error
    private String getFeedbackId(HttpServletRequest theReq) {
        // extract the feedback ID from the URL (for
        // http://hostname.com/mywebapp/servlet/MyServlet/a/b;c=123?d=789,
        // returns /a/b;c=123
        String pathInfo = theReq.getPathInfo();
        log.info("get Audio URL pathInfo = " + pathInfo);
        if (pathInfo == null || pathInfo.length() == 0) {
            return null;
        }
        if (pathInfo.startsWith("/") && pathInfo.endsWith(AUDIO_EXT)) {
            int extIndex = pathInfo.indexOf(AUDIO_EXT);
            pathInfo = pathInfo.substring(1, extIndex);
        }
        return pathInfo;
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

            rawAudio = Base64.decode(feedback.getVoiceBase64());
        } catch (NoResultException e) {
            // feedback ID passed in is not valid
            log.info("Feedback ID not found");
        } catch (NonUniqueResultException e) {
            log.severe("should never happen - two or more feedback have same key");
        } catch (Base64DecoderException e) {
            log.severe("base64 decode exception = " + e.getMessage());
            e.printStackTrace();
        }

        return rawAudio;
    }
}
