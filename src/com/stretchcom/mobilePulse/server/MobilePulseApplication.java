package com.stretchcom.mobilePulse.server;

import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class MobilePulseApplication extends Application {
    private static final Logger log = Logger.getLogger(MobilePulseApplication.class.getName());
    public static final String APPLICATION_BASE_URL = "https://1-2.mobile-pulse.appspot.com/";
    public static final String APPLICATION_WELCOME_PAGE = APPLICATION_BASE_URL + "index.html";
    public static final String LIST_DATE_FORMAT = "MM/dd/yy kk:mm";
    public static final String INFO_DATE_FORMAT = "MM/dd/yyyy 'at' hh:mm a";
    public static final String DEFAULT_LOCAL_TIME_ZONE = "America/Chicago";

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        log.info("in createInboundRoot");

        router.attach("/users", UsersResource.class);
        router.attach("/users/{id}", UsersResource.class);

        router.attach("/crashDetects", CrashDetectsResource.class);
        router.attach("/crashDetects/{id}", CrashDetectsResource.class);

        router.attach("/feedback", FeedbackResource.class);
        router.attach("/feedback/{id}", FeedbackResource.class);

        router.attach("/clientLogs", ClientLogsResource.class);
        router.attach("/clientLogs/{id}", ClientLogsResource.class);

        router.attach("/mobileCarriers", MobileCarriersResource.class);

        router.attach("/betaTesters", BetaTestersResource.class);
        router.attach("/betaTesters/{id}", BetaTestersResource.class);
        
        return router;
    }
}
