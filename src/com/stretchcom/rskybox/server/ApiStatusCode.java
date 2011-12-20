package com.stretchcom.rskybox.server;

public class ApiStatusCode {
	public static final String SERVER_ERROR = "0";
	public static final String SUCCESS = "100";
	
	public static final String INVALID_USER_CREDENTIALS = "200";
	public static final String INVALID_STATUS = "201";
	public static final String INVALID_LOG_LEVEL = "202";
	public static final String USER_NOT_AUTHORIZED_FOR_APPLICATION = "203";
	public static final String USER_ALREADY_HAS_CONFIRMED_EMAIL_ADDRESS = "204";
	public static final String USER_ALREADY_HAS_CONFIRMED_PHONE_NUMBER = "205";
	public static final String USER_EMAIL_ADDRESS_NOT_PENDING_CONFIRMATION = "206";
	public static final String USER_PHONE_NUMBER_NOT_PENDING_CONFIRMATION = "207";
	public static final String EMAIL_ADDRESS_CAN_NO_LONGER_BE_MODIFIED = "208";
	public static final String PHONE_NUMBER_CAN_NO_LONGER_BE_MODIFIED = "209";
	public static final String USER_NOT_AUTHORIZED_TO_CREATE_MEMBER = "210";
	public static final String USER_NOT_AUTHORIZED_TO_CREATE_MEMBER_WITH_SPECIFIED_ROLE = "211";
	public static final String USER_NOT_AUTHORIZED_TO_UPDATE_MEMBER = "212";
	public static final String USER_NOT_AUTHORIZED_TO_UPDATE_MEMBER_WITH_SPECIFIED_ROLE = "213";
	public static final String MEMBER_NOT_PENDING_CONFIRMATION = "214";
	public static final String MEMBER_NOT_A_REGISTERED_USER = "215";
	
	public static final String FEEDBACK_ID_REQUIRED = "300";
	public static final String CRASH_DETECT_ID_REQUIRED = "301";
	public static final String CLIENT_LOG_ID_REQUIRED = "302";
	public static final String USER_ID_REQUIRED = "303";
	public static final String END_USER_ID_REQUIRED = "304";
	public static final String APPLICATION_ID_REQUIRED = "305";
	public static final String APPLICATION_NAME_REQUIRED = "306";
	public static final String APP_MEMBER_ID_REQUIRED = "307";
	public static final String EITHER_EMAIL_ADDRESS_OR_PHONE_NUMBER_IS_REQUIRED = "308";
	public static final String CONFIRMATION_CODE_IS_REQUIRED = "309";
	public static final String PASSWORD_IS_REQUIRED = "311";
	public static final String USER_NAME_IS_REQUIRED = "312";
	public static final String EMAIL_ADDRESS_IS_REQUIRED = "313";
	public static final String ROLE_IS_REQUIRED = "314";
	
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
	public static final String INVALID_PHONE_NUMBER = "410";
	public static final String INVALID_CONFIRMATION_CODE = "411";
	public static final String PASSWORD_TOO_SHORT = "412";
	public static final String PHONE_NUMBER_ALREADY_USED = "413";
	public static final String INVALID_TIMESTAMP_PARAMETER = "414";
	public static final String INVALID_DURATION_PARAMETER = "415";
	
	public static final String PHONE_NUMBER_AND_MOBILE_CARRIER_ID_MUST_BE_SPECIFIED_TOGETHER = "500";
	public static final String NO_PHONE_NUMBER_TO_ASSOCIATE_WITH_CARRIER_ID = "501";

	public static final String USER_NOT_FOUND = "600";
	public static final String FEEDBACK_NOT_FOUND = "601";
	public static final String CRASH_DETECT_NOT_FOUND = "602";
	public static final String CLIENT_LOG_NOT_FOUND = "603";
	public static final String END_USER_NOT_FOUND = "604";
	public static final String APPLICATION_NOT_FOUND = "605";
	public static final String APP_MEMBER_NOT_FOUND = "606";
	public static final String EMAIL_ADDRESS_NOT_FOUND = "607";
	public static final String PHONE_NUMBER_NOT_FOUND = "608";
	
	public static final String EMAIL_ADDRESS_PHONE_NUMBER_MUTUALLY_EXCLUSIVE = "700";
}
