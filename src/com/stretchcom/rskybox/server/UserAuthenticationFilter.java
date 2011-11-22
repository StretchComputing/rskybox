package com.stretchcom.rskybox.server;

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
import com.stretchcom.rskybox.models.User;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.appengine.repackaged.com.google.common.util.Base64DecoderException;

public class UserAuthenticationFilter implements Filter {

    private static final String HTML_DIR = "/WEB-INF";
    private static final String A_PRIORI_TOKEN = "agxtb2JpbGUtcHVsc2VyDgsSCEZlZWRiYWNrGBEM";
    private static final Logger log = Logger.getLogger(UserAuthenticationFilter.class.getName());

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {
    	
    	// ------------------------------------------------------------------
    	// Authentication Algorithm:
    	// ------------------------------------------------------------------
    	// 1. For select user management APIs, bypass any authentication check and return (e.g. create user, get token, password reset, ...)
    	// 2. Extract token for HTTP authorization header
    	// 3. If token not present, return HTTP authentication error
    	// 4. Attempt User match via token and set currentUser for downstream use
    	// 5. If no User match, attempt application match via token and set currentUser as appropriate (not sure what this looks like yet ...)
    	// 6. If no token match, return HTTP authentication error
    	
    	// TODO remove the following comment block
    	// ------------------------------------------------------------------
    	// Filter requires authentication for the following type of requests:
    	// ------------------------------------------------------------------
    	// * rSkybox Client application REST calls with a priori token
    	// * rSkybox welcome-file with Google account token
    	// * rSkybox HTML requests with Google account token
    	// * rSkybox REST calls with Google account token
    	// * rSkybox Admin REST calls with Google account token
    	//
    	// -------------------------------------------
    	// Authentication (for above type of requests)
    	// -------------------------------------------
    	// * rSkybox Client application REST calls with valid a priori token are given full access to the app (for now, including admin REST calls)
    	// * All rSkybox users must be currently logged into a Google account.
    	// * If user is an admin of this rSkybox app engine application, they are given full access to the app.
    	// * If user is not and admin, they must be in the User table. Lookup is via Google account email address.
    	// * Only an admin can access Admin related REST calls.
    	//
    	// --------------------------------
    	// Authentication failure responses
    	// --------------------------------
    	// * If HTML request and user is not logged into Google account, user is redirected to Google login screen.
    	// * If REST request and user is not logged into Google account, HTTP 401 Unauthorized status is returned.
    	// * If HTML/REST request and user is not admin and not in User table, HTTP 401 Unauthorized status is returned.
    	// * If an Admin REST reqeust and user is not admin, HTTP 401 Unauthorized status is returned.
    	
    	log.info("**********  entered doFilter()  **********");

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
        	HttpServletRequest httpRequest = (HttpServletRequest)request;
    		HttpServletResponse httpResponse = (HttpServletResponse)response;

    		String thisURL = getURL(httpRequest);
    		log.info("thisURL = " + thisURL);
    		
    		// get the currentUser and store in the request for easy access by down stream REST Resource handlers
	    	UserService userService = UserServiceFactory.getUserService();
	    	com.google.appengine.api.users.User currentGoogleUser = userService.getCurrentUser();
	    	User currentUser = null;
	    	String emailAddress = null;
	    	if(currentGoogleUser != null) {
	    		emailAddress = currentGoogleUser.getEmail();
	    		currentUser = User.getUser(emailAddress);
	    		if(currentUser != null && userService.isUserAdmin()) {
	    			currentUser.setIsSuperAdmin(true);
	    		}
	    	} else {
		    	// *****************  TODO temp code  ********************
	    		emailAddress = "joe@test.com";
	    		currentUser = User.getUser(emailAddress);
	    	}
	    	
	    	httpRequest.setAttribute(RskyboxApplication.CURRENT_USER, currentUser);
    		
    		
    		// ::::::::::::::::::::::::::::TESTING ONLY:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    		// uncomment during testing to allow all rest calls
//    		if(thisURL.contains("/rest/")) {
//    			chain.doFilter(request, response);
//    			log.info("**********  REST Filter by pass -- SHOULD ONLY BE USED DURING TESTING  **********");
//    			return;
//    		}
    		//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    		
    		
    		// rSkybox Client application REST calls with valid a priori token are given full access to the app (for now, including admin REST calls)
    		// So if this is a rSkybox client, just flow thru to chain.doFilter() below.  If there is no a priori token, this codes assumes it was NOT
    		// a client request - even though it may be a client request with a bad token. But the right thing will happen because without the a priori
    		// token, the code will determine the request is NOT authenticated.
    		if(!isRskyboxClientWithValidToken(httpRequest)) {
    			handleRskyboxAppRequest(httpRequest, httpResponse, chain, thisURL);
    			
    	        // ::PUNT:: Tried allowing this to fall thru so chain.doFilter() is called below, but could not get RequestDispatcher.forward()
    			//          to play nice with chain.doFilter(). If an answer is found, this code would have to be restructured because not all
    			//          code paths in handleRskyboxAppRequestAuthorized() should flow thru to chain.doFilter().
    			return;
    		}
        } else {
        	log.info("***** request is NOT an instance of HttpServletRequest *******");
        }
        
        log.info("calling chain.doFilter() in doFilter() ...");
        chain.doFilter(request, response);
    }
    
    private Boolean isRskyboxClientWithValidToken(HttpServletRequest httpRequest) {
    	String token = getToken(httpRequest);
    	log.info("a priori token = " + token);
    	if(token != null && token.equals(A_PRIORI_TOKEN)) {
    		log.info("a priori token is valid");
    		return true;
    	}
    	log.info("a priori token is NOT valid or is NOT present");
    	return false;
    }
    
    private void handleRskyboxAppRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain, String thisURL) {
    	// All rSkybox users must be currently logged into a Google account.
		try {
	    	UserService userService = UserServiceFactory.getUserService();
	    	com.google.appengine.api.users.User currentUser = userService.getCurrentUser();
			if(currentUser == null) {
				if(thisURL.contains("/rest/")) {
					// If REST request and user is not logged into Google account, HTTP 401 Unauthorized status is returned.
					httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					log.info("REST request, but user is not logged into Google account -- returning HTTP 401");
					return;
				} else {
					// If HTML request and user is not logged into Google account, user is redirected to Google login screen.
	        		String loginUrl = userService.createLoginURL(thisURL);
	        		httpResponse.sendRedirect(loginUrl);
	        		log.info("HTML request, but user is not logged into Google account -- redirecting to loginUrl = " + loginUrl);
	        		return;
				}
	    	} else {
	    		log.info("current user email address = " + currentUser.getEmail());
	    		log.info("current user nick name = " + currentUser.getNickname());
	    		if(userService.isUserAdmin()) {
	    			// If user is an admin of this rSkybox app engine application, they are given full access to the app.
	    			log.info("User is logged into Google account and is an admin on app engine");
	    		} else {
	    			log.info("User is logged into Google account and is NOT an admin on app engine");
	    			
	    			// If user is not and admin, they must be in the User table. Lookup is via Google account email address.
	    			if(!User.isAuthenticated(currentUser.getEmail())) {
	    				// If HTML/REST request and user is not admin and not in User table, HTTP 401 Unauthorized status is returned.
	    				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	    				log.info("HTML/REST request, but user is not and admin and is not in User table -- returning HTTP 401");
	    				return;
	    			}
	    			
	    			// If this is an Admin REST request, then return HTTP 401 Unauthorized status since user is not admin
	    			if(isAdminRestRequest(thisURL, httpRequest)) {
	    				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	    				log.info("Admin REST request, but user is not and admin -- returning HTTP 401");
	    				return;
	    			}
	    		}
	    		
	    		// If we made it this far, user is authorized for request made, so continue on ....
	    		
	    		if(thisURL.contains("/rest/")) {
	    			// REST request
	    			log.info("calling chain.doFilter() in handleRskyboxAppRequest() ...");
	    			chain.doFilter(httpRequest, httpResponse);
	    		} else {
		            // any non-REST request needs to be redirected to the WEB-INF/html directory
		    		String uri = httpRequest.getRequestURI();
		    		if(!uri.toLowerCase().contains(".html") && !uri.endsWith("/")) {
		    			uri = uri + "/";
		    		}
		            uri = HTML_DIR + uri;
		            log.info("Calling RequestDispatcher modified URI: " + uri);
		            RequestDispatcher rd = httpRequest.getRequestDispatcher(uri);
		            try {
						rd.forward(httpRequest, httpResponse);
					} catch (ServletException e) {
						e.printStackTrace();
					} 
	    		}
	    	}
		} catch (IOException e) {
			log.severe("handleRskyboxAppRequest() IOException. Exception = " + e.getMessage());
			e.printStackTrace();
		} catch (ServletException e) {
			log.severe("handleRskyboxAppRequest() ServletException. Exception = " + e.getMessage());
			e.printStackTrace();
		}
		
        return;
    }
    
    private Boolean isAdminRestRequest(String thisURL, HttpServletRequest httpRequest) {
    	// currently, only the Users Create and Delete REST calls are considered 'Admin'
    	log.info("isAdminRestRequest(): method = " + httpRequest.getMethod());
    	if(thisURL.toLowerCase().contains("/rest") &&
           thisURL.toLowerCase().contains("/users") &&
           (httpRequest.getMethod().equalsIgnoreCase("post") || httpRequest.getMethod().equalsIgnoreCase("delete"))  ) {
    		return true;
    	}
    	return false;
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
