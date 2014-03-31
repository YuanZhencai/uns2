package com.wcs.uns.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.wcs.uns.service.UnsService;

/**
 * <p>Project: uns</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2011 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:shenbo@wcs-global.com">Shen Bo</a>
 */
@SuppressWarnings("rawtypes")
@Stateless
public class UnsValidator {
	
	Logger logger = Logger.getLogger("com.wcs.uns");
	
	@EJB
	UnsService service;
	private Set<String> props;
	
	/**
	 * 验证消息格式是否正确
	 * @param record
	 * @param type
	 * @return
	 */
	public boolean isValidMsgFommat(Map<String, String> record) {
		
		int typeInt = 0;
		if (UnsUtil.isNullOrEmpty(record.get("type"))) {
			logger.log(Level.INFO, "Type is null or empty");
			return false;
		}
		
		try {
			typeInt = Integer.valueOf(record.get("type"));
		} catch (NumberFormatException e) {
			logger.log(Level.INFO, "It's not a integer");
			return false;
		}
		
		if (typeInt < 1 || typeInt > 7) { 
			logger.log(Level.INFO, "Undefined type is detected");
			return false;
		};
		
		boolean isMailActived = (Integer.valueOf(Integer.toHexString(typeInt)) &
				Integer.valueOf(Integer.toHexString(1))) > 0 ? true : false;
		//if (record.size() != 8 && isMailActived) { return false; }
		for (String prop : getProps()) {
			if (!record.containsKey(prop)) {
				if (!isMailActived && "subject".equals(prop)) {
					continue;
				} else {
					logger.log(Level.INFO, "Property: {0} is not exist", prop);
					return false;
				}
			}
		}
		
		if (UnsUtil.isNullOrEmpty(record.get("sys")) || UnsUtil.isNullOrEmpty(record.get("key"))) {
			logger.log(Level.INFO, "Syskey can't be null or empty");
			return false;
		}
		
		return true;
	}
	
	/**
	 * 验证消息中的员工是否存在
	 * @param record
	 * @return
	 */
	public boolean isValidPernr(Map<String, String> record) {
		// pernr is empty
		if (UnsConsts.MARK_EMPTY.equals(record.get("pernr"))) {
			return true;
		}
		if (!UnsUtil.isNullOrEmpty(record.get("pernr"))&& isPernr(record.get("pernr"))) {
			return haveSuchUser(record.get("pernr"));
		}
		return false;
	}
	
	/**
	 * 验证系统密钥是否正确
	 * @param record
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean isValidSysKeyPair(String sys, String key) {
		try {
			String sysRes = service.getSys(sys);
			List<Map<String, String>> sysInfo = new ObjectMapper().readValue("["+sysRes+"]", List.class);
			if (!key.equals((sysInfo.get(0)).get("SYSKEY"))) { return false; }
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * 验证消息各字段内容格式是否正确
	 * @param record
	 * @return
	 */
	public int isValidMsgInfo(Map<String, String> record) {
		int result = 1;
		
		// check the message type
		int type = Integer.valueOf(record.get("type"));
		boolean isMailActive = (Integer.valueOf(Integer.toHexString(type)) & Integer
				.valueOf(Integer.toHexString(1))) > 0 ? true : false;
		boolean isSmsActive = (Integer.valueOf(Integer.toHexString(type)) & Integer
				.valueOf(Integer.toHexString(2))) > 0 ? true : false;
				
		if (isMailActive && isSmsActive) {
			if (isEmail(record.get("email")) == isTelno(record.get("telno")) && isEmail(record.get("email")) == true) {
				result = 0;
			} else if (isEmail(record.get("email")) != isTelno(record.get("telno"))) {
				result = -1;
			}
		} else {
			if (isMailActive) {
				if(isEmail(record.get("email"))) result = 0;
			}
			if (isSmsActive) {
				if(isTelno(record.get("telno"))) result = 0;
			}
		}
		return result;
	}
	
	/**
	 * 验证是否超出消息发送定额
	 * @param record
	 * @param recieveDatetime
	 * @param msgTarget 
	 * 		发送消息的对象 1:email; 2:sms; 3:email&sms都有
	 * @return
	 */
	public boolean isOverAllocationSize(Map<String, String> record, Date recieveDatetime, int msgTarget) {
		
		String sysId = record.get("sys");
		Date one_minute_ago = new Date(recieveDatetime.getTime() - UnsConsts.ONE_MINUTE);
		Date one_hour_ago = new Date(recieveDatetime.getTime() - UnsConsts.ONE_HOUR);
		long to_day = Date.parse(new SimpleDateFormat("yyyy/MM/dd").format(new Date(System.currentTimeMillis())) + " 00:00:00");
		Date today = new Date(to_day);
		
		// SQL中用between and查询时，后面的时间是不包括的，为了避免边界情况下少统计已发送得信息故对收到消息的时间加一秒
		Date currDatetime = new Date(recieveDatetime.getTime() + UnsConsts.ONE_SECOND);
		
		boolean isOver_in_minute = false;
		boolean isOver_in_hour = false;
		boolean isOver_in_day = false;
		boolean isOver_total_size = false;
		
		if (msgTarget == 1 || msgTarget == 3) {
			isOver_in_minute = isOverAllocationSizeChecker(
					"UnsEmailLog.findByDurationTime", one_minute_ago,
					currDatetime, sysId, record.get("email"),
					UnsConsts.MAX_IN_MINUTE);
			
			if (isOver_in_minute) { logger.log(Level.INFO, "In the last one minute email is over allocation size : {0}", UnsConsts.MAX_IN_MINUTE); };
			
			isOver_in_hour = isOverAllocationSizeChecker(
					"UnsEmailLog.findByDurationTime", one_hour_ago,
					currDatetime, sysId, record.get("email"),
					UnsConsts.MAX_IN_HOUR);
			
			if (isOver_in_hour) { logger.log(Level.INFO, "In the last hour email is over allocation size : {0}", UnsConsts.MAX_IN_HOUR); };
			
			isOver_in_day = isOverAllocationSizeChecker(
					"UnsEmailLog.findByDurationTime", today,
					currDatetime, sysId, record.get("email"),
					UnsConsts.MAX_IN_DAY);
			
			if (isOver_in_day) { logger.log(Level.INFO, "Email is over the allocation size : {0} today", UnsConsts.MAX_IN_DAY); };
		}
		
		if (msgTarget == 2 || msgTarget == 3) {
			
			// 在邮件和短信都发的情况下，如果邮件超过发送定额，则不用再判断短信，该信息判定为超出定额，不发送
			if (msgTarget == 3 && (isOver_in_minute || isOver_in_hour || isOver_in_day)) {
				return true;
			}
			
			isOver_in_minute = isOverAllocationSizeChecker(
					"UnsSmsLog.findByDurationTime", one_minute_ago,
					currDatetime, sysId, record.get("telno"),
					UnsConsts.MAX_IN_MINUTE);
			
			if (isOver_in_minute) { logger.log(Level.INFO, "In the last one minute email is over allocation size : {0}", UnsConsts.MAX_IN_MINUTE); };
			
			isOver_in_hour = isOverAllocationSizeChecker(
					"UnsSmsLog.findByDurationTime", one_hour_ago,
					currDatetime, sysId, record.get("telno"),
					UnsConsts.MAX_IN_HOUR);
			
			if (isOver_in_hour) { logger.log(Level.INFO, "In the last hour email is over allocation size : {0}", UnsConsts.MAX_IN_HOUR); };
			
			isOver_in_day = isOverAllocationSizeChecker(
					"UnsSmsLog.findByDurationTime", today,
					currDatetime, sysId, record.get("telno"),
					UnsConsts.MAX_IN_DAY);
			
			if (isOver_in_day) { logger.log(Level.INFO, "Email is over the allocation size : {0} today", UnsConsts.MAX_IN_DAY); };
		}
		
		isOver_total_size = isOverTotalSizeChecker(record, today, currDatetime);
		
		if (isOver_total_size) { logger.log(Level.INFO, "The message send by {0} in the last hour is over the allocation size", record.get("sys")); };
		
		return isOver_in_minute || isOver_in_hour || isOver_in_day || isOver_total_size;
	}
	
	private boolean isOverAllocationSizeChecker(String queryName,Date durationTimeAgo,
			Date recieveDatetime, String sysId, String msgAddr, int maxsize) {
		List msgs = service.getDurationMsgCount(queryName, durationTimeAgo, recieveDatetime, sysId, msgAddr);
		if (msgs.size() > maxsize - 1) {
			return true;
		}
		return false;
	}
	
	private boolean isOverTotalSizeChecker(Map<String, String> record, Date durationTimeAgo,
			Date recieveDatetime) {
		List mails = service.getDurationMsgCount(
				"UnsEmailLog.findByDurationTime2", durationTimeAgo,
				recieveDatetime, record.get("sys"));
		List smss = service.getDurationMsgCount(
				"UnsSmsLog.findByDurationTime2", durationTimeAgo,
				recieveDatetime, record.get("sys"));
		return (mails.size() + smss.size()) > UnsConsts.MAX_TOTAL_IN_HOUR - 1?true : false;
	}
	
	/**
	 * 验证邮件地址格式是否正确
	 * @return
	 */
	private boolean isEmail(String mailAddr) {
		String regex = "^[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
		Pattern p = Pattern.compile(regex);
		if (mailAddr.contains("|")) {
			String emails[] = mailAddr.split("[|]");
			for (String email : emails) {
				Matcher m = p.matcher(email);
				if (!m.matches()) { 
					logger.log(Level.INFO, "Email: {0} is not valid", email);
					return false;
				}
			}
		} else {
			Matcher m = p.matcher(mailAddr);
			if (!m.matches()) {
				logger.log(Level.INFO, "Email: {0} is not valid", mailAddr);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 验证电话号码格式是否正确
	 * @return
	 */
	public boolean isTelno(String telno) {
		String regex = "^((13[0-9])|(14[0-9])|(15[^4,\\D])|(18[^4,\\D]))\\d{8}$";
		Pattern p = Pattern.compile(regex);
		if (telno.contains("|")) {
			String telnos[] = telno.split("[|]");
			for (String tel : telnos) {
				Matcher m = p.matcher(tel);
				if (!m.matches()) {
					logger.log(Level.INFO, "Telno: {0} is not valid", tel);
					return false;
				}
			}
		} else {
			Matcher m = p.matcher(telno);
			if (!m.matches()) {
				logger.log(Level.INFO, "Telno: {0} is not valid", telno);
				return false;
			}

		}
		return true;
	}
	
	/**
	 * Check sensitivity, returns true if mailto cc bcc contains sensitive address
	 * @param record
	 * @return
	 */
	public boolean isSensitive(Map<String, String> record) {
		boolean sensitiveFlg = false;
		
		String[] emails = record.get("email").split("[|]");
		String[] emailccs = null;
		String[] emailbccs = null;
		
		if (record.get("emailcc") != null) {
			emailccs = record.get("emailcc").split("[|]");
		}
		if (record.get("emailbcc") != null) {
			emailbccs = record.get("emailbcc").split("[|]");
		}
		
		// email
		for (String email : emails) {
			if (email == null || "".equals(email) || !isEmail(email)) continue;
			if (checkSensitive(email)) {
				sensitiveFlg = true;
				break;
			}
		}
		
		// email cc
		if (!sensitiveFlg && emailccs != null) {
			for (String emailcc : emailccs) {
				if (emailcc == null || "".equals(emailcc) || !isEmail(emailcc)) continue;
				if (checkSensitive(emailcc)) {
					sensitiveFlg = true;
					break;
				}
			}
		}
		
		// email bcc
		if (!sensitiveFlg && emailbccs != null) {
			for (String emailbcc : emailbccs) {
				if (emailbcc == null || "".equals(emailbcc) || !isEmail(emailbcc)) continue;
				if (checkSensitive(emailbcc)) {
					sensitiveFlg = true;
					break;
				}
			}
		}
		return sensitiveFlg;
	}
	
	private boolean checkSensitive(String mail) {
		boolean isSensitive = false;
		String suffix = mail.split("[@]")[1];
		if (!suffix.equals("wilmar-intl.com")) {
			isSensitive = true;
		}
		return isSensitive;
	}
	
	/**
	 * 验证员工号格式是否正确
	 * @return
	 */
	private boolean isPernr(String pernr) {
		//String regex = "";
		return true;
	}
	
	/**
	 * 验证是否存在该工号的用户
	 * @param pernr
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean haveSuchUser(String pernr) {
		List<Map<String, String>> userList = new ArrayList<Map<String,String>>();
		try {
			if (pernr.contains("|")) {
				String pernrs[] = pernr.split("[|]");
				for (String p : pernrs) {
					userList = new ObjectMapper().readValue(service.getUsers(p), List.class);
				}
			} else {
				userList = new ObjectMapper().readValue(service.getUsers(pernr), List.class);
			}
			if (userList.size() > 0) return true; 
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private Set<String> getProps() {
		props = new HashSet<String>();
		props.add("sys");
		props.add("key");
		props.add("type");
		props.add("email");
		props.add("telno");
		props.add("pernr");
		props.add("subject");
		props.add("body");
		props.add("aux");
		return props;
	}
	
}
