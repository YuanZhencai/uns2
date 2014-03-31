package com.wcs.uns.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.wcs.uns.model.UnsFailedMsg;
import com.wcs.uns.utils.UnsUtil;
import com.wcs.uns.utils.UnsValidator;

/**
 * <p>
 * Project: uns
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright (c) 2011 Wilmar Consultancy Services
 * </p>
 * <p>
 * All Rights Reserved.
 * </p>
 * 
 * @author <a href="mailto:shenbo@wcs-global.com">Shen Bo</a>
 */
@Path("/msg/rejected/{sys}")
@Stateless
public class MsgResource {

	@EJB
	UnsValidator validator;
	@EJB
	UnsService service;

	@GET
	@Produces("text/plain;charset=UTF-8")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String getRejectedMessages(@PathParam("sys") String sys, @QueryParam("key") String key, @QueryParam("ts") @DefaultValue("0") String ts, @QueryParam("max") @DefaultValue("200") String max) {
		int defaultSize = 200;
		try {
			// 如果未指明sysId,返回{}
			if (UnsUtil.isNullOrEmpty(sys))
				return "{}";

			// 如果未指定密钥或系统密钥不符合，返回{}
			if (UnsUtil.isNullOrEmpty(key) || !validator.isValidSysKeyPair(sys, key))
				return "{}";

			// 系统中没有找到任何被拒绝待处理的消息,返回{}
			int failedMsgCount = service.getUnsFailedMsgCountBySysId(sys);
			// List<UnsFailedMsg> failMsgs =
			// service.getUnsFailedMsgBySysId(sys);
			if (failedMsgCount == 0)
				return "{}";

			// 找到被拒信息，但都在给定的ts之前，返回{"ts":"1234567890"}
			Long version = service.getUnsFailedMsgVersionBySysId(sys);
			if (version - Long.valueOf(ts) < 0)
				return "{\"ts\":\"" + ts + "\"}";

			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			Map<String, String> tsMap = new HashMap<String, String>();
			tsMap.put("ts", String.valueOf(version));
			list.add(tsMap);
			int resultSize = defaultSize;
			try {
				resultSize = Integer.valueOf(max).intValue();
			} catch (Exception e) {
				resultSize = defaultSize;
			}
			// 获得给定时间戳之后的退信记录,如果超过200条，则只返回后200条
			List<UnsFailedMsg> failMsgs = null;
			if (resultSize > 0) {
				failMsgs = service.getUnsFailedMsgLast(resultSize, ts, sys);
			} else {
				failMsgs = service.getUnsFailedMsgByVersion2(ts, sys);
			}
			for (UnsFailedMsg failMsg : failMsgs) {
				Map<String, String> failMsgMap = new HashMap<String, String>();
				failMsgMap.put("sys", sys);
				failMsgMap.put("key", key);
				failMsgMap.put("type", String.valueOf(failMsg.getMsgType()));
				failMsgMap.put("email", failMsg.getMsgEmail());
				failMsgMap.put("emailcc", failMsg.getMsgEmailcc());
				failMsgMap.put("emailbcc", failMsg.getMsgEmailbcc());
				failMsgMap.put("telno", failMsg.getMsgTelno());
				failMsgMap.put("pernr", failMsg.getMsgPernr());
				failMsgMap.put("subject", failMsg.getMsgSubject());
				failMsgMap.put("body", failMsg.getMsgBody());
				failMsgMap.put("aux", failMsg.getMsgAux());
				failMsgMap.put("ts", String.valueOf((failMsg.getMsgDatetime()).getTime()));
				failMsgMap.put("reason", String.valueOf(failMsg.getFailureType()));
				failMsgMap.put("sensitiveInd", failMsg.getSensitiveInd());
				list.add(failMsgMap);
			}
			return new ObjectMapper().writeValueAsString(list);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "{}";
	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String clearRejectedMessages(@PathParam("sys") String sys, @QueryParam("key") String key, @QueryParam("ts") String ts) {

		// 如果未指明sysId,返回{}
		if (UnsUtil.isNullOrEmpty(sys))
			return "{}";

		// 如果未指定密钥或系统密钥不符合，返回{}
		if (UnsUtil.isNullOrEmpty(key) || !validator.isValidSysKeyPair(sys, key))
			return "{}";

		// 获得旧的退信消息
		List<UnsFailedMsg> failedMsgs = service.getUnsFailedMsgByVersion(ts, sys);
		// 更新旧消息的状态
		service.updateUnsFailedMsg(failedMsgs);
		// 返回删除掉的旧消息记录数
		int cleared = service.deleteUnsFailedMsg();

		return "{\"cleared\":\"" + cleared + "\"}";
	}

}
