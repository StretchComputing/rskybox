package com.stretchcom.mobilePulse.server;

public class ApiStatusCode {
	public static final String SERVER_ERROR = "0";
	public static final String SUCCESS = "100";
	
	public static final String INVALID_USER_CREDENTIALS = "200";
	public static final String INVALID_STATUS = "201";
	public static final String INVALID_LOG_LEVEL = "202";
	public static final String USER_NOT_AUTHORIZED_FOR_APPLICATION = "203";
	
	public static final String FEEDBACK_ID_REQUIRED = "300";
	public static final String CRASH_DETECT_ID_REQUIRED = "301";
	public static final String CLIENT_LOG_ID_REQUIRED = "302";
	public static final String USER_ID_REQUIRED = "303";
	public static final String BETA_TESTER_ID_REQUIRED = "304";
	public static final String APPLICATION_ID_REQUIRED = "305";
	public static final String APPLICATION_NAME_REQUIRED = "306";
	public static final String APP_MEMBER_ID_REQUIRED = "307";
	
	public static final String INVALID_STATUS_PARAMETER = "400";
	public static final String INVALID_RECORDED_DATE_PARAMETER = "401";
	public static final String INVALID_DETECTED_DATE_PARAMETER = "402";
	public static final String INVALID_EMAIL_ADDRESS_PARAMETER = "403";
	public static final String INVALID_MOBILE_CARRIER_PARAMETER = "404";
	public static final String EMAIL_ADDRESS_ALREADY_USED = "405";
	public static final String APPLICATION_NAME_ALREADY_USED = "406";
	public static final String USER_ALREADY_MEMBER = "407";
	public static final String INVALID_ROLE = "408";
	public static final String APP_MEMBER_ALREADY_ACTIVE = "409";
	

	public static final String USER_NOT_FOUND = "600";
	public static final String FEEDBACK_NOT_FOUND = "601";
	public static final String CRASH_DETECT_NOT_FOUND = "602";
	public static final String CLIENT_LOG_NOT_FOUND = "603";
	public static final String BETA_TESTER_NOT_FOUND = "604";
	public static final String APPLICATION_NOT_FOUND = "605";
	public static final String APP_MEMBER_NOT_FOUND = "606";
}
