package com.wcs.uns.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import com.wcs.uns.service.UnsService;

@Stateless
public class SendMailHelper {

	Logger logger = Logger.getLogger("com.wcs.uns");
	
	@EJB
	UnsService service;
	
	private static String DO_NOY_REPLY_SERVER;
	private static String DO_NOY_REPLY_EMAIL;
	private static String DO_NOT_REPLY_USR;
	private static String DO_NOT_REPLY_PSW;
	
	
    static { 
        Properties prop; 
        InputStream inCtx = SendMailHelper.class.getClassLoader().getResourceAsStream("unsCtx.properties");
        try {
        	prop = new Properties();
            prop.load(inCtx);
            DO_NOY_REPLY_SERVER = prop.getProperty("DO_NOY_REPLY_SERVER").trim();
            DO_NOY_REPLY_EMAIL = "do_not_reply@wilmar-intl.com";
            DO_NOT_REPLY_USR = prop.getProperty("DO_NOT_REPLY_USR").trim();
            DO_NOT_REPLY_PSW = prop.getProperty("DO_NOT_REPLY_PSW").trim();
        } catch (IOException e) {
            e.printStackTrace(); 
        } 
    }
	
	public int send(Map<String, String> record) {
		String[] emailccs = null;
		String[] emailbccs = null;
		String[] replyTos = null;
		
		try {
	    	HtmlEmail email = new HtmlEmail();
	        email.setHostName(DO_NOY_REPLY_SERVER);
	        email.setAuthentication(DO_NOT_REPLY_USR, DO_NOT_REPLY_PSW);

			String[] emails = record.get("email").split("[|]");
			String[] telnos = record.get("telno").split("[|]");
			String[] pernrs = record.get("pernr").split("[|]");
			if (record.get("emailcc") != null) {
				emailccs = record.get("emailcc").split("[|]");
			}
			if (record.get("emailbcc") != null) {
				emailbccs = record.get("emailbcc").split("[|]");
			}
			if (record.get("replyTo") != null) {
				replyTos = record.get("replyTo").split("[|]");
			}
			
			// 获得并设置接收人
			Map<String, String> recieversMap = service.getRecievers(emails, telnos, pernrs);
			for (String mail : emails) {
				if (mail == null || "".equals(mail)) { continue; }
				recieversMap.put(mail, mail);
			}
			for (String emailAddr : recieversMap.keySet()) {
				email.addTo(emailAddr);
			}
			
			// email cc
			if (emailccs != null) {
				for (String emailcc : emailccs) {
					if (emailcc == null || "".equals(emailcc)) { continue; }
					email.addCc(emailcc);
				}
			}
			
			// email bcc
			if (emailbccs != null) {
				for (String emailbcc : emailbccs) {
					if (emailbcc == null || "".equals(emailbcc)) { continue; }
					email.addBcc(emailbcc);
				}
			}
			
			// email reply to
			if (replyTos != null) {
				for (String replyTo : replyTos) {
					if (replyTo == null || "".equals(replyTo)) { continue; }
					email.addReplyTo(replyTo);
				}
			}
			
	        email.setFrom(DO_NOY_REPLY_EMAIL);  
	          
	        email.setSubject(record.get("subject"));
	          
	        //设置Charset
	        email.setCharset(UnsConsts.GB2312);
	        
	        email.setHtmlMsg(record.get("body"));
			
			logger.log(Level.INFO, ">>> Start to send email, ts:{0}", String.valueOf(System.currentTimeMillis()));
	        email.send();
	        logger.log(Level.INFO, ">>> Mail has bean send to recievers, ts:{0}", String.valueOf(System.currentTimeMillis()));
	        return 1;
		} catch (EmailException e) {
			logger.log(Level.INFO, "When sending a email error occured {0}" + e.getMessage());
			return 0;
		}
   
    }
	
}
