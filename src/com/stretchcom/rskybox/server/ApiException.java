package com.stretchcom.rskybox.server;

public class ApiException extends Exception {
	public ApiException() {
	  }
	 
	  public ApiException(String msg) {
	    super(msg);
	  }
}
