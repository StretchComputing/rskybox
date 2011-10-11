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

public class UserAuthenticationFilter implements Filter {

    private static final String HTML_DIR = "/WEB-INF/html";
    private static final Logger log = Logger.getLogger(UserAuthenticationFilter.class.getName());

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {
    	
    	// allow LiveFeed Rest calls used a priori token. If it fails, send 401 error.
    	// allow mobilePulse rest calls. If it fails, send 401 error.
    	// for *.html, redirect to Google login URL.
    	
    	log.info("**********  entered doFilter()  **********");

        if (request instanceof HttpServletRequest) {
        	HttpServletRequest httpRequest = (HttpServletRequest)request;
        	
        	UserService userService = UserServiceFactory.getUserService();
        	com.google.appengine.api.users.User currentUser = userService.getCurrentUser();
        	
        	// TODO more reliable matching of the Login URL which should be ignored
    		String thisURL = getURL(httpRequest);
    		log.info("thisURL = " + thisURL);

    		if(thisURL.contains("rest")) {
    			chain.doFilter(request, response);
        		log.info("********** returning -- not yet authenticating REST URLs");
    			return;
    		}
    		
        	if(currentUser == null) {
        		// redirect to Google login URL
        		// TODO -- if this is a /rest URL, then send a 401 error back to client instead
        		HttpServletResponse httpResponse = (HttpServletResponse)response;
        		String loginUrl = userService.createLoginURL(thisURL);
        		httpResponse.sendRedirect(loginUrl);
        		log.info("********** just redirected to loginUrl = " + loginUrl);
        	} else {
        		log.info("current user email address = " + currentUser.getEmail());
        		log.info("current user nick name = " + currentUser.getNickname());
        		
        		if(userService.isUserAdmin()) {
        			log.info("user is an admin on app engine");
        		} else {
        			log.info("user is NOT an admin on app engine");
        		}
        		
                // HTML files are stored in WEB-INF/html. This allows HTML file requests to be handled by app engine directly and allow this filter to 
        		// do its job.
        		String uri = httpRequest.getRequestURI();
                uri = HTML_DIR + uri;
                log.info("modified URI: " + uri);
                RequestDispatcher rd = httpRequest.getRequestDispatcher(uri);
                rd.forward(request, response);
        		
        		chain.doFilter(request, response);
        	}
        } else {
        	log.info("***** request is NOT an instance of HttpServletRequest *******");
        }
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
        String queryString = req.getQueryString();          // d=789

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
