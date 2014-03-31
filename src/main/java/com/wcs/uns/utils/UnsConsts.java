package com.wcs.uns.utils;

/**
 * <p>Project: uns</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2011 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:shenbo@wcs-global.com">Shen Bo</a>
 */
public class UnsConsts {

	/**
	 * the base path of mds
	 */
	// PRD
	//public static final String BASEPATH = "http://10.229.12.153:9081/rs";
	// QAS&UAT
	//public static final String BASEPATH = "http://10.229.12.153:9089/rs";
	// TEST
	//public static final String BASEPATH = "http://10.228.191.203:9085/rs";
	// 221 dev
	//public static final String BASEPATH = "http://10.228.191.221:9081/rs";
	// 221 tst
	//public static final String BASEPATH = "http://10.228.191.221:9085/rs";
	// 231 prd
	//public static final String BASEPATH = "http://10.228.191.231:9081/rs";
	// 231 qas
	public static final String BASEPATH = "http://10.228.191.231:9085/rs";
	
	public static final String HTTPHOST = "http://210.21.237.245/services/Sms";
	public static final String 	MARK_SLASH = "/";
	public static final String MARK_EMPTY = "";
	public static final String MARK_AMPERSAND = "&";
	public static final String MARK_QUE = "?";
	public static final String MARK_EUQ = "=";
	public static final String MARK_COMMA = ",";
	public static final String UTF8 = "UTF-8";
	public static final String GB2312 = "GB2312";
	public static final String RES_USER = "users";
	public static final String RES_SYS = "sys";
	public static final long ONE_SECOND = 1000;
	public static final long ONE_MINUTE = 60*ONE_SECOND;
	public static final long ONE_HOUR = 60*ONE_MINUTE;
	public static final int MAX_IN_MINUTE = 20;
	public static final int MAX_IN_HOUR = 50;
	public static final int MAX_IN_DAY = 100;
	public static final int MAX_TOTAL_IN_HOUR = 3000;
}
