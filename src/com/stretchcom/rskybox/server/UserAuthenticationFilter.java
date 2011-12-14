package com.stretchcom.rskybox.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.stretchcom.rskybox.models.Application;
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
    	
    	// ----------------------------------------------------------------------------------------------------------------------------------------------------
    	// Authentication Algorithm:
    	// ----------------------------------------------------------------------------------------------------------------------------------------------------
    	// 1. For select user management APIs, bypass any authentication check and return (e.g. create user, get token, password reset, ...)
    	// 2. Extract token for HTTP authorization header
    	// 3. If token not present, redirect user to login screen for HTML5 calls and return HTTP 401 Unauthorized for REST API calls
    	// 4. Attempt User match via token and set currentUser (with isSuperAdmin set) for downstream use
    	// 5. If no User match, attempt application match via token and set currentUser as appropriate (not sure what this looks like yet ...)
    	// 6. If no token match, redirect user to login screen for HTML5 calls and return HTTP 401 Unauthorized for REST API calls
    	// 7. At this point, request has been authenticated
    	// 8. Any non-REST request needs to be "URL adjusted" to the WEB-INF/html5 directory (HTML5 files in WEB-INF to enforce authentication via this filter!)
    	// ----------------------------------------------------------------------------------------------------------------------------------------------------
    	// ----------------------------------------------------------------------------------------------------------------------------------------------------
    	log.info("**********  entered doFilter()  **********");

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
        	HttpServletRequest httpRequest = (HttpServletRequest)request;
    		HttpServletResponse httpResponse = (HttpServletResponse)response;

    		// logout all request cookies
    		Cookie[] cookies = httpRequest.getCookies();
    		if(cookies != null) {
        		for(Cookie c : cookies) {
        			log.info("cookie name = " + c.getName() + " cookie value = " + c.getValue());
        		}
    		}

    		String thisURL = getURL(httpRequest);
    		log.info("thisURL = " + thisURL);
    		
    		List<User> outParameterList = new ArrayList<User>();
    		Boolean isRequestAuthentic = isRequestAuthentic(thisURL, httpRequest, outParameterList);
    		if(isRequestAuthentic) {
    			log.info("request is authentic");
    			
    			// if present, store in HTTP request for use downstream
    			if(outParameterList.size() > 0) {
    				User currentUser = (User)outParameterList.get(0);
    				if(SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
    					// for now, in Dev, all users are super admins
    					log.info("Dev Environment: making user a Super Admin");
    					currentUser.setIsSuperAdmin(true);
    				} else {
    					currentUser.setIsSuperAdmin(User.isAdmin());
    				}
    				log.info("isSuperAdmin = " + currentUser.getIsSuperAdmin());
    				
    				httpRequest.setAttribute(RskyboxApplication.CURRENT_USER, currentUser);
    				log.info("setting currentUser for downstream use. currentUser = " + currentUser);
    			}
    			
    			// adjust URL for non-REST calls
    			if(adjustUrl(thisURL, httpRequest, httpResponse)) {
    				// URL was adjusted which includes a forward to the dispatcher, so we're done
    				return;
    			}
    		} else {
    			log.info("request is NOT authentic");
    			sendErrorResponse(thisURL, httpResponse);
    			return;
    		}
        } else {
        	log.info("***** request is NOT an instance of HttpServletRequest *******");
        }
        log.info("calling chain.doFilter() in doFilter() ...");
        chain.doFilter(request, response);
    }
    
    
    // theOutParameterList: if authentic and token is associated with a user, that 'current' user is stored in this out parameter list
    private Boolean isRequestAuthentic(String theUrl, HttpServletRequest theHttpRequest, List<User> theOutParameterList) {
    	// 1. if bypass API, return TRUE
    	if(isBypassApi(theUrl, theHttpRequest)) {
    		return true;
    	}
    	
    	// 2. extract token
    	String token = getToken(theHttpRequest);
    	if(token == null) {
    		log.info("token is NULL, authentication failed");
    		return false;
    	}

    	// 3. if token matches a user, put CurrentUser in out parameter and return TRUE
    	User currentUser = User.getUserWithToken(token);
    	if(currentUser != null) {
    		theOutParameterList.add(currentUser);
    		return true;
    	}
    	
    	// 4. if token matches an app, return TRUE
    	Application currentApp = Application.getApplicationWithToken(token);
    	if(currentApp != null) {
    		return true;
    	}
    	
    	// 5. else return FALSE since we could not find a valid token
    	return false;
    }
    
    
	// 3. If token not present, redirect user to login screen for HTML5 calls and return HTTP 401 Unauthorized for REST API calls
    private void sendErrorResponse(String theUrl, HttpServletResponse theHttpResponse) {
		try {
	    	if(theUrl.contains("/rest/")) {
	    		theHttpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				log.info("un-authenticated REST request -- returning HTTP 401");
				return;
	    	} else {
	    		String loginUrl = RskyboxApplication.SIGN_IN_PAGE;
	    		theHttpResponse.sendRedirect(loginUrl);
	    		log.info("un-authenticated HTML request -- redirecting to loginUrl = " + loginUrl);
	    		return;
	    	}
		} catch (IOException e) {
			log.severe("sendErrorResponse() IOException. Exception = " + e.getMessage());
			e.printStackTrace();
		} 
    }
    
    // adjust URL for non-REST calls
    // returns True if URL adjusted by forwarding -- so filter processing is complete
    private Boolean adjustUrl(String theUrl, HttpServletRequest theHttpRequest, HttpServletResponse theHttpResponse) {
    	// skip over the REST APIs and the crashStackData and Audio Servlets
		if(!theUrl.contains("/rest/") && !theUrl.contains("/crashStackData/") && !theUrl.contains("/audio/")) {
            // any non-REST request needs to be redirected to the WEB-INF/html directory
    		String uri = theHttpRequest.getRequestURI();
    		if(!uri.toLowerCase().contains(".html") && !uri.endsWith("/")) {
    			uri = uri + "/";
    		}
            uri = HTML_DIR + uri;
            log.info("Calling RequestDispatcher modified URI: " + uri);
            RequestDispatcher rd = theHttpRequest.getRequestDispatcher(uri);
            try {
				rd.forward(theHttpRequest, theHttpResponse);
			} catch (IOException e) {
				log.severe("adjustUrl() IOException. Exception = " + e.getMessage());
				e.printStackTrace();
				return false;
			} catch (ServletException e) {
				log.severe("adjustUrl() ServletException. Exception = " + e.getMessage());
				e.printStackTrace();
				return false;
			} 
            return true;
		}
    	return false;
    }
    
    private Boolean isBypassApi(String theUrl, HttpServletRequest theHttpResponse) {
    	/////////////////
    	// HTML5 Bypasses
    	/////////////////
    	// Homepage bypass
    	if(theUrl.toLowerCase().endsWith("/html5") || theUrl.toLowerCase().endsWith("/html5/")) {
    		log.info("HTML5 Homepage bypassed");
    		return true;
    	}
    	
    	////////////////////
    	// REST API Bypasses
    	////////////////////
    	// Create User API is bypassed
    	if(theUrl.toLowerCase().contains("/users") && !theUrl.toLowerCase().contains("/users/") && theHttpResponse.getMethod().equalsIgnoreCase("post")) {
    		log.info("Create User API is bypassed");
    		return true;
    	}
    	
    	// User Get Confirmation Code API is bypassed
    	if(theUrl.toLowerCase().contains("/users/requestconfirmation") && theHttpResponse.getMethod().equalsIgnoreCase("post")) {
    		log.info("User Get Confirmation Code API is bypassed");
    		return true;
    	}
    	
    	// User Get Token API is bypassed
    	if(theUrl.toLowerCase().contains("/users/token") && theHttpResponse.getMethod().equalsIgnoreCase("get")) {
    		log.info("User Get Token API is bypassed");
    		return true;
    	}
    	
    	// User Confirm API is bypassed
    	if(theUrl.toLowerCase().contains("/users/confirm") && theHttpResponse.getMethod().equalsIgnoreCase("put")) {
    		log.info("User Confirm API is bypassed");
    		return true;
    	}

    	// List Mobile Carriers API is bypassed
    	if(theUrl.toLowerCase().contains("/mobilecarriers") && theHttpResponse.getMethod().equalsIgnoreCase("get")) {
    		log.info("List MobileCarriers API is bypassed");
    		return true;
    	}    
    	
    	return false;
    }
	
    // supports extracting the token from either a cookie or the HTTP authorization header with precedence given to the cookie.
    private String getToken(HttpServletRequest httpRequest) {
		String token = null;
		// first attempt to find the token in a cookie of the form "token=<token_value>"
		Cookie[] cookies = httpRequest.getCookies();
		if(cookies != null && cookies.length > 0) {
			for(Cookie c : cookies) {
				if (c.getName().equals("token")) {
					return c.getValue();
				}
			}
		}
		
		// if we get this far, no cookie token was found so extract from HTTP authorization header
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
