package com.stretchcom.rskybox.server;

import java.math.BigInteger;
import java.security.SecureRandom;


public final class TF {
	private static final SecureRandom random = new SecureRandom();
	
    private TF() {}

    public static String get() {
        return new BigInteger(130, random).toString(32);
    }

    public static String getPassword() {
        String token = new BigInteger(130, random).toString(32);
        if(token.length() < 9){
        	return token;
        } else {
            return token.substring(0, 7);
        }
    }

    public static String getConfirmationCode() {
        String token = new BigInteger(130, random).toString(32);
        if(token.length() < 4){
        	return token;
        } else {
            return token.substring(0, 3).toLowerCase();
        }
    }
}