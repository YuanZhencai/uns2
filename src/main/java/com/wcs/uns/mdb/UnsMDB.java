package com.wcs.uns.mdb;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.wcs.uns.model.UnsEmailLog;
import com.wcs.uns.model.UnsFailedMsg;
import com.wcs.uns.model.UnsMsgLog;
import com.wcs.uns.model.UnsSmsLog;
import com.wcs.uns.service.UnsService;
import com.wcs.uns.utils.SendMailHelper;
import com.wcs.uns.utils.SendSmsHelper;
import com.wcs.uns.utils.UnsValidator;

/**
 * <p>Project: uns</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2011 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:shenbo@wcs-global.com">Shen Bo</a>
 */
@MessageDriven(mappedName = "jms/UNSINBOX", activationConfig = {
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
})
public class UnsMDB implements MessageListener {

	Logger logger = Logger.getLogger("com.wcs.uns");

	@PersistenceContext
	EntityManager em;

	@EJB
	UnsValidator validator;
	@EJB
	SendMailHelper sendMailHelper;
	@EJB
	SendSmsHelper sendSmsHelper;
	@EJB
	UnsService service;

	private List<Map<String, String>> msgList;
	private Date recieveDatetime;
	private int failureType;
	private int msgTarget;
	// 是否是敏感邮件，Y是N否
	private String sensitiveInd = "N";
	// 发送消息是否成功发出，1成功：0失败：-1部分成功部分失败
	private int result;

	@SuppressWarnings("unchecked")
	public void onMessage(Message message) {
		try {
			// 获得消息接收到的时间
			recieveDatetime = service.findCurrentTimestamp();

			TextMessage msg = (TextMessage) message;

			msgList = new ObjectMapper().readValue(msg.getText(), List.class);

			logger.log(Level.INFO, ">>> Message is arrived!");
			logger.log(Level.INFO, ">>> Message recieved:{0}" + msg.getText());

			if (msgList == null || msgList.size() == 0) {
				return;
			}

			// 每次MDB只能响应一条消息，取出该条消息。
			Map<String, String> record = msgList.get(0);

			// 验证是否包含敏感邮箱地址
			if (validator.isSensitive(record)) {
				sensitiveInd = "Y";
			}

			// 对传入的消息格式进行验证
			if (!validator.isValidMsgFommat(record)) {
				logger.log(Level.INFO, ">>> It's not a valid message format, message will be rejected!");
				failureType = 1;
				// 消息格式错误，记录发送失败日志
				registerFailedMsg(record, failureType, new Date(System.currentTimeMillis()));
				return;
			}

			// 获得该消息的发送类型
			int type = Integer.valueOf(record.get("type"));

			// 判断消息中的系统密钥是否匹配
			if (!validator.isValidSysKeyPair(record.get("sys"), record.get("key"))) {
				logger.log(Level.INFO, ">>> It's not a valid System-key pair, message will be rejected!");
				failureType = 2;
				registerFailedMsg(record, failureType, new Date(System.currentTimeMillis()));
				return;
			}

			// 判断邮件地址，手机号码，员工号是否有效
			if (!validator.isValidPernr(record)) {
				logger.log(Level.INFO, ">>> The person with the pernr:{0} is not exist, message will be rejected!", record.get("pernr"));
				failureType = 3;
				registerFailedMsg(record, failureType, new Date(System.currentTimeMillis()));
				return;
			}
			if (validator.isValidMsgInfo(record) == -1 || validator.isValidMsgInfo(record) == 1) {
				if (validator.isValidMsgInfo(record) == 1) {
					logger.log(Level.INFO, ">>> This message is not valid, message will be rejected!");
					failureType = 3;
					registerFailedMsg(record, failureType, new Date(System.currentTimeMillis()));
					return;
				} else {
					result = -1;
				}
			}

			// 判断是否超出消息发送定额
			boolean isMailActive = (Integer.valueOf(Integer.toHexString(type)) & Integer
					.valueOf(Integer.toHexString(1))) > 0 ? true : false;
			boolean isSmsActive = (Integer.valueOf(Integer.toHexString(type)) & Integer
					.valueOf(Integer.toHexString(2))) > 0 ? true : false;

			if (isMailActive & isSmsActive) {
				// 需要发送邮件和短信
				msgTarget = 3;
			} else {
				if (isMailActive) {
					// 只发送邮件
					msgTarget = 1;
				} else {
					// 只发送短信
					msgTarget = 2;
				}
			}

			if (validator.isOverAllocationSize(record, recieveDatetime, msgTarget)) {
				logger.log(Level.INFO, ">>> This message is over allocation size, message will be rejected!");
				failureType = 5;
				registerFailedMsg(record, failureType, new Date(System.currentTimeMillis()));
				return;
			}

			// 信息验证完毕，开始发送消息。
			switch (msgTarget) {
			case 1:
				result = sendMailHelper.send(record);
				break;
			case 2:
				result = sendSmsHelper.send(record);
				break;
			case 3:
				if (result == -1) {
					sendMailHelper.send(record);
					sendSmsHelper.send(record);
					break;
				}
				result = sendMailHelper.send(record) + sendSmsHelper.send(record);
				// 部分失败情况下 result置为-1
				if (result == 1) {
					result = -1;
				}
				;
				break;
			}

			// 发送信息时服务器异常则记入退信日志
			if (result == 0 || result == -1) {
				if (result == 0) {
					failureType = 4;
				} else {
					failureType = 6;
				}
				registerFailedMsg(record, failureType, service.findCurrentTimestamp());
				return;
			}

			// 信息发送成功的情况下，记录相应日志表。
			registerSuccMsg(record, type);

		} catch (JMSException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 消息成功发送，将消息记录在相应数据表中
	 * @param map
	 * @param type
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void registerSuccMsg(Map<String, String> record, int type) {
		// ***@LiWei***以下为解决超过4000字符，保存异常添加代码***//
		// 如果消息内容过长，截取一部分保存
		String body = record.get("body");
		if (body == null) {
			body = "";
		} else {
			try {
				byte[] bytes = body.getBytes();
				if (bytes.length > 3800) {
					body = new String(bytes, 0, 3800);
					System.out.println(body);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		record.put("body", body);
		// ***@LiWei***以上为解决超过4000字符，保存异常添加代码***//
		// 是否发邮件
		boolean isMailActived = (Integer.valueOf(Integer.toHexString(type)) &
				Integer.valueOf(Integer.toHexString(1))) > 0 ? true : false;
		// 是否发短信
		boolean isSmsActived = (Integer.valueOf(Integer.toHexString(type)) &
				Integer.valueOf(Integer.toHexString(2))) > 0 ? true : false;
		if (isMailActived) {
			registerMailLog(record);
		}
		if (isSmsActived) {
			registerSmsLog(record);
		}
		registerMsgLog(record);
	}

	/**
	 * 记录邮件信息
	 * @param map
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void registerMailLog(Map<String, String> record) {
		UnsEmailLog log = new UnsEmailLog();
		log.setSysId(record.get("sys"));
		log.setEmailAddr(record.get("email"));
		log.setEmailCc(record.get("emailcc"));
		log.setEmailBcc(record.get("emailbcc"));
		log.setEmailSubject(record.get("subject"));
		log.setEmailBody(record.get("body"));
		log.setEmailAux(record.get("aux"));
		log.setSensitiveInd(sensitiveInd);
		log.setEmailDatetime(service.findCurrentTimestamp());
		log.setCreatedDatetime(service.findCurrentTimestamp());
		em.persist(log);
	}

	/**
	 * 记录短信信息
	 * @param map
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void registerSmsLog(Map<String, String> record) {
		UnsSmsLog log = new UnsSmsLog();
		log.setSysId(record.get("sys"));
		log.setSmsTelno(record.get("telno"));
		log.setSmsBody(record.get("body"));
		log.setSmsAux(record.get("aux"));
		log.setSmsDatetime(service.findCurrentTimestamp());
		log.setCreatedDatetime(service.findCurrentTimestamp());
		em.persist(log);
	}

	/**
	 * 记录消息信息
	 * @param map
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void registerMsgLog(Map<String, String> record) {
		UnsMsgLog log = new UnsMsgLog();
		log.setSysId(record.get("sys"));
		log.setMsgType(Integer.valueOf(record.get("type")));
		log.setMsgEmail(record.get("email"));
		log.setMsgEmailcc(record.get("emailcc"));
		log.setMsgEmailbcc(record.get("emailbcc"));
		log.setMsgTelno(record.get("telno"));
		log.setMsgPernr(record.get("pernr"));
		log.setMsgBody(record.get("body"));
		log.setMsgAux(record.get("aux"));
		log.setSensitiveInd(sensitiveInd);
		log.setMsgDatetime(service.findCurrentTimestamp());
		log.setCreatedDatetime(service.findCurrentTimestamp());
		em.persist(log);
	}

	/**
	 * 记录退信消息
	 * @param map
	 * @param failureType
	 * @param updatedDatetime
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void registerFailedMsg(Map<String, String> record, int failureType, Date updatedDatetime) {
		// ***@LiWei***以下为解决超过4000字符，保存异常添加代码***//
		// 如果消息内容过长，截取一部分保存
		String body = record.get("body");
		if (body == null) {
			body = "";
		} else {
			try {
				byte[] bytes = body.getBytes();
				if (bytes.length > 3800) {
					body = new String(bytes, 0, 3800);
					System.out.println(body);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		record.put("body", body);
		// ***@LiWei***以上为解决超过4000字符，保存异常添加代码***//
		UnsFailedMsg log = new UnsFailedMsg();
		log.setSysId(record.get("sys"));
		try {
			log.setMsgType(Integer.valueOf(record.get("type")));
		} catch (NumberFormatException e) {
			log.setMsgType(1);
		}

		log.setMsgEmail(record.get("email"));
		log.setMsgEmailcc(record.get("emailcc"));
		log.setMsgEmailbcc(record.get("emailbcc"));
		log.setMsgTelno(record.get("telno"));
		log.setMsgPernr(record.get("pernr"));
		log.setMsgBody(record.get("body"));
		if (record.containsKey("subject")) {
			log.setMsgSubject(record.get("subject"));
		} else {
			log.setMsgSubject("");
		}
		log.setMsgAux(record.get("aux"));
		log.setMsgDatetime(service.findCurrentTimestamp());
		log.setFailureType(failureType);
		log.setSensitiveInd(sensitiveInd);
		log.setDefunctInd("N");
		log.setCreatedDatetime(service.findCurrentTimestamp());
		log.setUpdatedDatetime(updatedDatetime);
		em.persist(log);
		logger.log(Level.INFO, ">>> Failed message have been persisted into DB, ts:{0}", String.valueOf(System.currentTimeMillis()));
	}
}
