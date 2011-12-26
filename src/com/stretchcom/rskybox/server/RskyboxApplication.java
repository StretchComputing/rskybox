package com.stretchcom.rskybox.server;

import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class RskyboxApplication extends Application {
    private static final Logger log = Logger.getLogger(RskyboxApplication.class.getName());
    public static final String APPLICATION_BASE_URL = "https://rskybox-stretchcom.appspot.com/";
    public static final String APPLICATION_WELCOME_PAGE = APPLICATION_BASE_URL + "index.html";
    public static final String USER_VERIFICATION_PAGE = APPLICATION_BASE_URL + "index.html#confirm";
    public static final String MEMBER_VERIFICATION_PAGE = APPLICATION_BASE_URL + "index.html#confirm";
    public static final String SIGN_IN_PAGE = "/";
	public static final String EMAIL_START_TOKEN_MARKER = ":rSkyboxId:";
	public static final String EMAIL_END_TOKEN_MARKER = "::";
	public static final String AUTO_SENDER = "rSkybox";
    
    public static final String LIST_DATE_FORMAT = "MM/dd/yy kk:mm";
    public static final String INFO_DATE_FORMAT = "MM/dd/yyyy 'at' hh:mm a";
    public static final String APP_ACTION_DATE_FORMAT = "yyyy-MM-dd kk:mm:ss.SSS";
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

        router.attach("/applications/{applicationId}/endUsers", EndUsersResource.class);
        router.attach("/applications/{applicationId}/endUsers/{id}", EndUsersResource.class);

        router.attach("/mobileCarriers", MobileCarriersResource.class);
        
        return router;
    }
}
