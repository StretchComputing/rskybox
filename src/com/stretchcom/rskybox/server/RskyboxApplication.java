package com.stretchcom.rskybox.server;

import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class RskyboxApplication extends Application {
    private static final Logger log = Logger.getLogger(RskyboxApplication.class.getName());
    public static final String APPLICATION_BASE_URL = "https://rskybox-stretchcom.appspot.com/";
    public static final String APPLICATION_WELCOME_PAGE = APPLICATION_BASE_URL + "index.html";
    // TODO what is the real login page
    public static final String SIGN_IN_PAGE = "www.rskybox.com";
    public static final String LIST_DATE_FORMAT = "MM/dd/yy kk:mm";
    public static final String INFO_DATE_FORMAT = "MM/dd/yyyy 'at' hh:mm a";
    public static final String DEFAULT_LOCAL_TIME_ZONE = "America/Chicago";
	public static final String CURRENT_USER = "rSkybox.currentUser";

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        log.info("in createInboundRoot");

        router.attach("/applications", ApplicationsResource.class);
        router.attach("/applications/{id}", ApplicationsResource.class);

        router.attach("/users", UsersResource.class);
        router.attach("/users/{id}", UsersResource.class);

        router.attach("/applications/{applicationId}/appMembers", AppMembersResource.class);
        router.attach("/applications/{applicationId}/appMembers/{id}", AppMembersResource.class);

        router.attach("/applications/{applicationId}/crashDetects", CrashDetectsResource.class);
        router.attach("/applications/{applicationId}/crashDetects/{id}", CrashDetectsResource.class);

        router.attach("/applications/{applicationId}/feedback", FeedbackResource.class);
        router.attach("/applications/{applicationId}/feedback/{id}", FeedbackResource.class);

        router.attach("/applications/{applicationId}/clientLogs", ClientLogsResource.class);
        router.attach("/applications/{applicationId}/clientLogs/{id}", ClientLogsResource.class);

        router.attach("/applications/{applicationId}/betaTesters", BetaTestersResource.class);
        router.attach("/applications/{applicationId}/betaTesters/{id}", BetaTestersResource.class);

        router.attach("/mobileCarriers", MobileCarriersResource.class);
        
        return router;
    }
}
