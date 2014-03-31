package com.wcs.uns.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.wcs.uns.model.UnsFailedMsg;
import com.wcs.uns.utils.UnsConsts;
import com.wcs.uns.utils.UnsUtil;

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
@Stateless
public class UnsService {

	@PersistenceContext
	EntityManager em;
	private Map<String, String> queryMap;

	/**
	 * 通过keyword筛选得到JSON格式的users的信息
	 * 
	 * @param keyword
	 * @return
	 * @throws IOException
	 */
	public String getUsers(String keyword) {
		queryMap = new HashMap<String, String>();
		queryMap.put("keyword", keyword);
		return new UnsUtil().getResource(UnsConsts.RES_USER, UnsConsts.MARK_EMPTY, queryMap);
	}

	/**
	 * 通过系统id筛选得到JSON格式的system信息
	 * 
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public String getSys(String id) {
		queryMap = new HashMap<String, String>();
		return new UnsUtil().getResource(UnsConsts.RES_SYS, id, queryMap);
	}

	/**
	 * 获得所有接收者
	 * 
	 * @param emails
	 * @param telnos
	 * @param pernrs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getRecievers(String[] emails, String[] telnos, String[] pernrs) {
		Map<String, String> revieverMap = new HashMap<String, String>();

		try {
			for (String email : emails) {
				if (email == null || "".equals(email)) {
					break;
				}
				List<Map<String, String>> userList = new ObjectMapper().readValue(getUsers(email), List.class);
				for (int i = 0; i < userList.size(); i++) {
					revieverMap.put(userList.get(i).get("EMAIL"), userList.get(i).get("TELNO"));
				}
			}

			for (String telno : telnos) {
				if (telno == null || "".equals(telno)) {
					break;
				}
				List<Map<String, String>> userList = new ObjectMapper().readValue(getUsers(telno), List.class);
				for (int i = 0; i < userList.size(); i++) {
					revieverMap.put(userList.get(i).get("EMAIL"), userList.get(i).get("TELNO"));
				}
			}

			for (String pernr : pernrs) {
				if (pernr == null || "".equals(pernr)) {
					break;
				}
				List<Map<String, String>> userList = new ObjectMapper().readValue(getUsers(pernr), List.class);
				for (int i = 0; i < userList.size(); i++) {
					revieverMap.put(userList.get(i).get("EMAIL"), userList.get(i).get("TELNO"));
				}
			}

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return revieverMap;
	}

	/**
	 * 获得某一时间端内发送到某个系统某组用户的所有消息记录
	 * 
	 * @param queryName
	 * @param durationTimeAgo
	 * @param recieveDatetime
	 * @param sysId
	 * @param msgAddr
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List getDurationMsgCount(String queryName, Date durationTimeAgo, Date recieveDatetime, String sysId, String msgAddr) {
		return em.createNamedQuery(queryName).setParameter("durationDatetime", durationTimeAgo).setParameter("currentDatetime", recieveDatetime).setParameter("sysId", sysId).setParameter("msgAddr", msgAddr).getResultList();
	}

	/**
	 * 获得某一时间端内发送到某个系统的所有消息记录
	 * 
	 * @param queryName
	 * @param durationTimeAgo
	 * @param recieveDatetime
	 * @param sysId
	 * @param msgAddr
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List getDurationMsgCount(String queryName, Date durationTimeAgo, Date recieveDatetime, String sysId) {
		return em.createNamedQuery(queryName).setParameter("durationDatetime", durationTimeAgo).setParameter("currentDatetime", recieveDatetime).setParameter("sysId", sysId).getResultList();
	}

	/**
	 * 通过SysId从退信表中找到被拦截的消息信息
	 * 
	 * @param sysId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UnsFailedMsg> getUnsFailedMsgBySysId(String sysId) {
		return em.createNamedQuery("UnsFailedMsg.findBySysId").setParameter("sysId", sysId).getResultList();
	}

	/**
	 * 通过SysId从退信表中找到最近的退信时间
	 * 
	 * @param sysId
	 * @return
	 */
	public Long getUnsFailedMsgVersionBySysId(String sysId) {
		return em.createNamedQuery("UnsFailedMsg.findVersionBySysId", Timestamp.class).setParameter("sysId", sysId).getSingleResult().getTime();
	}

	/**
	 * 获得系统给定时间戳之前的退信记录
	 * 
	 * @param ts
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UnsFailedMsg> getUnsFailedMsgByVersion(String ts, String sysId) {
		return em.createNamedQuery("UnsFailedMsg.findByVersion").setParameter("ts", new Date(Long.parseLong(ts) + UnsConsts.ONE_SECOND)).setParameter("sysId", sysId).getResultList();
	}

	/**
	 * 获得系统给定时间戳之后的退信记录
	 * 
	 * @param ts
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UnsFailedMsg> getUnsFailedMsgByVersion2(String ts, String sysId) {
		return em.createNamedQuery("UnsFailedMsg.findByVersion2").setParameter("ts", new Date(Long.parseLong(ts))).setParameter("sysId", sysId).getResultList();
	}

	/**
	 * 获取指定时间戳之后的最后size条退信记录
	 * @param size
	 * @param ts
	 * @param sysId
	 * @return
	 */
	public List<UnsFailedMsg> getUnsFailedMsgLast(int size, String ts, String sysId) {
		String jpql = "SELECT e FROM UnsFailedMsg e WHERE e.updatedDatetime >= :ts AND e.sysId = :sysId order by e.msgDatetime DESC";
		Query query = em.createQuery(jpql).setParameter("ts", new Date(Long.parseLong(ts))).setParameter("sysId", sysId);
		query.setFirstResult(0).setMaxResults(size);
		return query.getResultList();
	}

	/**
	 * 更新退信表中旧记录的信息
	 * 
	 * @param failedMsgs
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void updateUnsFailedMsg(List<UnsFailedMsg> failedMsgs) {
		for (UnsFailedMsg failedMsg : failedMsgs) {
			failedMsg.setDefunctInd("Y");
			failedMsg.setUpdatedDatetime(new Date(System.currentTimeMillis()));
			em.merge(failedMsg);
		}
	}

	/**
	 * 删除被标记的退信记录，返回被删除的消息条数
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public int deleteUnsFailedMsg() {
		int count = 0;
		List<UnsFailedMsg> failedMsgs = em.createNamedQuery("UnsFailedMsg.findByDefunctInd").setParameter("defunctInd", "Y").getResultList();
		for (UnsFailedMsg failedMsg : failedMsgs) {
			em.remove(failedMsg);
			count++;
		}
		return count;
	}

	/**
	 * 获得发短信时需要的信息
	 * 
	 * @param record
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getSmsSendInfo(Map<String, String> record) {

		Map<String, String> smsSendInfo = new HashMap<String, String>();
		try {
			// 获得接收人电话号码
			String[] emails = record.get("email").split("[|]");
			String[] telnos = record.get("telno").split("[|]");
			String[] pernrs = record.get("pernr").split("[|]");
			Map<String, String> recieverMap = getRecievers(emails, telnos, pernrs);
			StringBuffer recievers = new StringBuffer();
			for (String email : recieverMap.keySet()) {
				recievers.append(recieverMap.get(email));
				recievers.append(UnsConsts.MARK_COMMA);
			}
			if (recievers.length() > 0) {
				recievers.deleteCharAt(recievers.length() - 1);
			}

			// 获得短信序列号及接入号
			String sysRes = getSys(record.get("sys"));
			List<Map<String, String>> sysInfo = new ObjectMapper().readValue("[" + sysRes + "]", List.class);

			smsSendInfo.put("sn", sysInfo.get(0).get("SMSSN"));
			smsSendInfo.put("orgaddr", sysInfo.get(0).get("SMSNO"));
			smsSendInfo.put("telno", recievers.toString());
			smsSendInfo.put("content", record.get("body"));

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return smsSendInfo;
	}

	/**
	 * 获得开关标示符
	 * 
	 * @param sys
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getSwitchOnFlg(String sys) {
		String sysRes = getSys(sys);
		List<Map<String, String>> sysInfo;
		try {
			sysInfo = new ObjectMapper().readValue("[" + sysRes + "]", List.class);
			return sysInfo.get(0).get("SMSON");
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "N";
	}

	/**
	 * Get system date time
	 * 
	 * @return
	 */
	public Timestamp findCurrentTimestamp() {
		Query query = em.createNativeQuery("SELECT current timestamp FROM sysibm.sysdummy1", Timestamp.class);
		return (Timestamp) query.getSingleResult();
	}

	/**
	 * SysId从退信表中找到被拦截的消息信息的数量
	 * 
	 * @param sysId
	 * @return
	 * @Author:LiWei 2013-6-6下午4:54:12
	 */
	public int getUnsFailedMsgCountBySysId(String sysId) {
		if (sysId == null) {
			return 0;
		}
		Query query = em.createQuery("SELECT COUNT(e) FROM UnsFailedMsg e WHERE e.sysId = :sysId");
		query.setParameter("sysId", sysId);
		String strCount = query.getSingleResult().toString();
		Long lCount = 0L;
		try {
			lCount = new Long(strCount);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return lCount.intValue();
	}
}
