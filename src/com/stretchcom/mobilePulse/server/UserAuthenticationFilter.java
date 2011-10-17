package com.stretchcom.mobilePulse.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.stretchcom.mobilePulse.models.User;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.appengine.repackaged.com.google.common.util.Base64DecoderException;

public class UserAuthenticationFilter implements Filter {

    private static final String HTML_DIR = "/WEB-INF/html";
    private static final String A_PRIORI_TOKEN = "agxtb2JpbGUtcHVsc2VyDgsSCEZlZWRiYWNrGBEM";
    private static final Logger log = Logger.getLogger(UserAuthenticationFilter.class.getName());

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {
    	
    	// ------------------------------------------------------------------
    	// Filter requires authentication for the following type of requests:
    	// ------------------------------------------------------------------
    	// * MobilePulse Client application REST calls with a priori token
    	// * MobilePulse welcome-file with Google account token
    	// * MobilePulse HTML requests with Google account token
    	// * MobilePulse REST calls with Google account token
    	//
    	// -------------------------------------------
    	// Authentication (for above type of requests)
    	// -------------------------------------------
    	// * MobilePulse Client application REST calls with a priori token
    	// * All mobilePulse users must be currently logged into a Google account.
    	// * If user is an admin of this mobilePulse app engine application, they are given full access to the app.
    	// * If user is not and admin, they must be in the User table. Lookup is via Google account email address.
    	//
    	// --------------------------------
    	// Authentication failure responses
    	// --------------------------------
    	// * If HTML request and user is not logged into Google account, user is redirected to Google login screen.
    	// * If REST request and user is not logged into Google account, HTTP 401 Unauthorized status is returned.
    	// * If HTML/REST request and user is not admin and not in User table, HTTP 401 Unauthorized status is returned.
    	
    	// *********************************************************************************
    	// TODO How to restrict the REST admin calls from MobilePulse client ***************
    	// *********************************************************************************
    	
    	log.info("**********  entered doFilter()  **********");

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
        	HttpServletRequest httpRequest = (HttpServletRequest)request;
    		HttpServletResponse httpResponse = (HttpServletResponse)response;

    		String thisURL = getURL(httpRequest);
    		log.info("thisURL = " + thisURL);
    		
    		// MobilePulse Client application REST calls with a priori token. So if this is a mobilePulse client, just flow thru to chain.doFilter() below.
    		// If there is no a priori token, this codes assumes it was NOT a client request - even though it may be a client request with a bad token. But
    		// the right thing will happen because without the a priori token, the code will determine the request is NOT authenticated.
    		if(!isMobilePulseClient(httpRequest)) {
    			handleMobilePulseAppRequestAuthorized(httpRequest, httpResponse, thisURL);
    			
    	        // ::PUNT:: Tried allowing this to fall thru so chain.doFilter() is called below, but could not get RequestDispatcher.forward()
    			//          to play nice with chain.doFilter(). If an answer is found, the code would have to be restructured because not all
    			//          code paths in handleMobilePulseAppRequestAuthorized() should flow thru to chain.doFilter().
    			return;
    		}
        } else {
        	log.info("***** request is NOT an instance of HttpServletRequest *******");
        }
        
		chain.doFilter(request, response);
    }
    
    private Boolean isMobilePulseClient(HttpServletRequest httpRequest) {
    	String token = getToken(httpRequest);
    	if(token != null && token.equals(A_PRIORI_TOKEN)) {
    		return true;
    	}
    	return false;
    }
    
    private void handleMobilePulseAppRequestAuthorized(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String thisURL) {
    	// All mobilePulse users must be currently logged into a Google account.
		try {
	    	UserService userService = UserServiceFactory.getUserService();
	    	com.google.appengine.api.users.User currentUser = userService.getCurrentUser();
			if(currentUser == null) {
				if(thisURL.contains("/rest/")) {
					// If REST request and user is not logged into Google account, HTTP 401 Unauthorized status is returned.
					httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					log.info("REST request, but user is not logged into Google account -- returning HTTP 401");
				} else {
					// If HTML request and user is not logged into Google account, user is redirected to Google login screen.
	        		String loginUrl = userService.createLoginURL(thisURL);
	        		httpResponse.sendRedirect(loginUrl);
	        		log.info("HTML request, but user is not logged into Google account -- redirecting to loginUrl = " + loginUrl);
				}
	    	} else {
	    		log.info("current user email address = " + currentUser.getEmail());
	    		log.info("current user nick name = " + currentUser.getNickname());
	    		if(userService.isUserAdmin()) {
	    			// If user is an admin of this mobilePulse app engine application, they are given full access to the app.
	    			log.info("User is logged into Google account and is an admin on app engine");
	    		} else {
	    			log.info("User is logged into Google account and is NOT an admin on app engine");
	    			
	    			// If user is not and admin, they must be in the User table. Lookup is via Google account email address.
	    			if(!User.isAuthenticated(currentUser.getEmail())) {
	    				// If HTML/REST request and user is not admin and not in User table, HTTP 401 Unauthorized status is returned.
	    				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	    				log.info("HTML/REST request, but user is not and admin and is not in User table -- returning HTTP 401");
	    			}
	    		}
	    		
	            // HTML files are stored in WEB-INF/html allowing HTML requests to be handled by app engine and thus this filter invoked for HTML files.
	    		if(thisURL.contains(".html")) {
		    		String uri = httpRequest.getRequestURI();
		            uri = HTML_DIR + uri;
		            log.info("modified URI: " + uri);
		            RequestDispatcher rd = httpRequest.getRequestDispatcher(uri);
		            try {
						rd.forward(httpRequest, httpResponse);
					} catch (ServletException e) {
						e.printStackTrace();
					} 
	    		}
	    	}
		} catch (IOException e) {
			log.severe("handleMobilePulseAppRequest() IOException. Exception = " + e.getMessage());
			e.printStackTrace();
		}
		
        return;
    }
    
	private String getToken(HttpServletRequest httpRequest) {
		String token = null;
		String authHeader = httpRequest.getHeader("Authorization");
		if (authHeader != null) {
			java.util.StringTokenizer st = new java.util.StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();
				if (basic.equalsIgnoreCase("Basic")) {
					String rawCredentials = st.nextToken();
					
					try {
						// 'credentials' is of the form "token:<token_value>"
				        byte[] credentialsArr = Base64.decode(rawCredentials);
						String credentials = new String(credentialsArr);
						int index = credentials.indexOf(":");
						if(index > -1) {
							token = credentials.substring(index+1);
						}
					} catch (Base64DecoderException e) {
						log.severe("base64 decode exception = " + e.getMessage());
						e.printStackTrace();
					} 
				}
			}
		}
		return token;
	}

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }
    
    private String getURL(HttpServletRequest req) {

        String scheme = req.getScheme();             // http
        String serverName = req.getServerName();     // hostname.com
        int serverPort = req.getServerPort();        // 80
        String contextPath = req.getContextPath();   // /mywebapp
        String servletPath = req.getServletPath();   // /servlet/MyServlet
        String pathInfo = req.getPathInfo();         // /a/b;c=123
        String queryString = req.getQueryString();   // d=789

        // Reconstruct original requesting URL
        StringBuffer url =  new StringBuffer();
        url.append(scheme).append("://").append(serverName);

        if (serverPort != 80) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath).append(servletPath);

        if (pathInfo != null) {
            url.append(pathInfo);
        }
        if (queryString != null) {
            url.append("?").append(queryString);
        }
        return url.toString();
    }    

}
