package com.stretchcom.mobilePulse.models;

import java.util.ArrayList;
import java.util.List;


public final class MobileCarrier {
    private static final List<MobileCarrier> mobileCarriers = initMobileCarriers();
    
    // instance variables
    private String name;
	private String code;
	private String emailDomainName;

	private static List<MobileCarrier> initMobileCarriers() {
    	List<MobileCarrier> buildList = new ArrayList<MobileCarrier>();
    	
    	buildList.add(new MobileCarrier("AT&T", "103", "@mms.att.net"));
       	buildList.add(new MobileCarrier("Verizon", "158", "@vtext.com"));
    	buildList.add(new MobileCarrier("Sprint", "147", "@messaging.sprintpcs.com"));
    	buildList.add(new MobileCarrier("T-Mobile", "150", "@tmomail.net"));
    	buildList.add(new MobileCarrier("Alltel", "102", "@message.alltel.com"));
    	buildList.add(new MobileCarrier("US Cellular", "155", "@email.uscc.net"));
    	buildList.add(new MobileCarrier("3 River Wireless", "100", "@sms.3rivers.net"));
    	buildList.add(new MobileCarrier("ACS Wireless", "101", "@paging.acswireless.com"));
    	buildList.add(new MobileCarrier("Bell Canada", "104", "@txt.bellmobility.ca"));
    	buildList.add(new MobileCarrier("Bell Mobility (Canada)", "105", "@txt.bell.ca"));
    	buildList.add(new MobileCarrier("Bell Mobility", "106", "@txt.bellmobility.ca"));
    	buildList.add(new MobileCarrier("Blue Sky Frog", "107", "@blueskyfrog.com"));
    	buildList.add(new MobileCarrier("Bluegrass Cellular", "108", "@sms.bluecell.com"));
    	buildList.add(new MobileCarrier("Boost Mobile", "109", "@myboostmobile.com"));
    	buildList.add(new MobileCarrier("BPL Mobile", "110", "@bplmobile.com"));
    	buildList.add(new MobileCarrier("Carolina West Wireless", "111", "@cwwsms.com"));
    	buildList.add(new MobileCarrier("Cellular One", "112", "@mobile.celloneusa.com"));
    	buildList.add(new MobileCarrier("Cellular South", "113", "@csouth1.com"));
    	buildList.add(new MobileCarrier("Centennial Wireless", "114", "@cwemail.com"));
    	buildList.add(new MobileCarrier("CenturyTel", "115", "@messaging.centurytel.net"));
    	buildList.add(new MobileCarrier("Cingular (Now AT&T)", "116", "@mms.att.net"));
    	buildList.add(new MobileCarrier("Clearnet", "117", "@msg.clearnet.com"));
    	buildList.add(new MobileCarrier("Comcast", "118", "@comcastpcs.textmsg.com"));
    	buildList.add(new MobileCarrier("Corr Wireless Communications", "119", "@corrwireless.net"));
    	buildList.add(new MobileCarrier("Dobson", "120", "@mobile.dobson.net"));
    	buildList.add(new MobileCarrier("Edge Wireless", "121", "@sms.edgewireless.com"));
    	buildList.add(new MobileCarrier("Fido", "122", "@fido.ca"));
    	buildList.add(new MobileCarrier("Golden Telecom", "123", "@sms.goldentele.com"));
    	buildList.add(new MobileCarrier("Helio", "124", "@messaging.sprintpcs.com"));
    	buildList.add(new MobileCarrier("Houston Cellular", "125", "@text.houstoncellular.net"));
    	buildList.add(new MobileCarrier("Idea Cellular", "126", "@ideacellular.net"));
    	buildList.add(new MobileCarrier("Illinois Valley Cellular", "127", "@ivctext.com"));
    	buildList.add(new MobileCarrier("Inland Cellular Telephone", "128", "@inlandlink.com"));
    	buildList.add(new MobileCarrier("MCI", "129", "@pagemci.com"));
    	buildList.add(new MobileCarrier("Metrocall", "130", "@page.metrocall.com"));
    	buildList.add(new MobileCarrier("Metrocall 2-way", "131", "@my2way.com"));
    	buildList.add(new MobileCarrier("Metro PCS", "132", "@mymetropcs.com"));
    	buildList.add(new MobileCarrier("Microcell", "133", "@fido.ca"));
    	buildList.add(new MobileCarrier("Midwest Wireless", "134", "@clearlydigital.com"));
    	buildList.add(new MobileCarrier("Mobilcomm", "135", "@mobilecomm.net"));
    	buildList.add(new MobileCarrier("MTS", "136", "@text.mtsmobility.com"));
    	buildList.add(new MobileCarrier("Nextel", "137", "@messaging.nextel.com"));
    	buildList.add(new MobileCarrier("OnlineBeep", "138", "@onlinebeep.net"));
    	buildList.add(new MobileCarrier("PCS One", "139", "@pcsone.net"));
    	buildList.add(new MobileCarrier("President's Choice", "140", "@txt.bell.ca"));
    	buildList.add(new MobileCarrier("Public Service Cellular", "141", "@sms.pscel.com"));
    	buildList.add(new MobileCarrier("Qwest", "142", "@qwestmp.com"));
    	buildList.add(new MobileCarrier("Rogers AT&T Wireless", "143", "@pcs.rogers.com"));
    	buildList.add(new MobileCarrier("Rogers Canada", "144", "@pcs.rogers.com"));
    	buildList.add(new MobileCarrier("Satellink", "145", ".pageme@satellink.net"));
    	buildList.add(new MobileCarrier("Southwestern Bell", "146", "@email.swbw.com"));
    	buildList.add(new MobileCarrier("Sumcom", "148", "@tms.suncom.com"));
    	buildList.add(new MobileCarrier("Surewest Communicaitons", "149", "@mobile.surewest.com"));
    	buildList.add(new MobileCarrier("Telus", "151", "@msg.telus.com"));
    	buildList.add(new MobileCarrier("Tracfone", "152", "@mms.att.net"));
    	buildList.add(new MobileCarrier("Triton", "153", "@tms.suncom.com"));
    	buildList.add(new MobileCarrier("Unicel", "154", "@utext.com"));
    	buildList.add(new MobileCarrier("Solo Mobile", "156", "@txt.bell.ca"));
    	buildList.add(new MobileCarrier("US West", "157", "@uswestdatamail.com"));
     	buildList.add(new MobileCarrier("Virgin Mobile", "159", "@vmobl.com"));
    	buildList.add(new MobileCarrier("Virgin Mobile Canada", "160", "@vmobile.ca"));
    	buildList.add(new MobileCarrier("West Central Wireless", "161", "@sms.wcc.net"));
    	buildList.add(new MobileCarrier("Western Wireless", "162", "@cellularonewest.com"));
    	
    	return buildList;
    }
	
	// returns null if emailDomainName not found
	public static String findEmailDomainName(String theCode) {
		String emailDomainName = null;
		for(MobileCarrier mc : mobileCarriers) {
			if(mc.getCode().equalsIgnoreCase(theCode)) {
				emailDomainName = mc.getEmailDomainName();
				break;
			}
		}
		return emailDomainName;
	}
	
	// for now, it is assumed that only ATT supports use of the from address as opposed to the 'return-path'
	public static Boolean usesFromAddress(String theSmsEmailAddress) {
		if(theSmsEmailAddress == null) {return false;}
		
		if(theSmsEmailAddress.contains("att.net")) {
			return true;
		}
		
		return false;
	}

    // only instances of this entity are held in the static list built inside of this class
	private MobileCarrier() {}
	
	private MobileCarrier(String theName, String theCode, String theEmailDomainName) {
		this.name = theName;
		this.code = theCode;
		this.emailDomainName = theEmailDomainName;
	}

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
    
    public String getEmailDomainName() {
		return emailDomainName;
	}

	public void setEmailDomainName(String emailDomainName) {
		this.emailDomainName = emailDomainName;
	}
	
	// static methods
    public static List<MobileCarrier> getList() {
        return mobileCarriers;
    }
}