package com.wcs.uns.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.wcs.uns.service.UnsService;

@Stateless
public class SendSmsHelper {

	Logger logger = Logger.getLogger("com.wcs.uns");

	@EJB
	UnsService service;
//	@EJB
//	SwitchController controller;
	private StringBuffer allTelno;
	private Map<String, String> telMap;

	public void sendMessageOne(String sn, String orgaddr, String telno,
			String content) throws MalformedURLException, IOException {

		HttpURLConnection connection = (HttpURLConnection) new URL(
				UnsConsts.HTTPHOST).openConnection();
		
		connection.setReadTimeout(30000);
		connection.setConnectTimeout(30000);
		
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
		connection.setRequestProperty("soapaction", "");
		connection.connect();
		OutputStream os = connection.getOutputStream();
		String xml = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:chin=\"http://chinagdn.com\">"
				+ "<soapenv:Header/>"
				+ "<soapenv:Body>"
				+ "<chin:InsertDownSms soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
				+ "<sn xsi:type=\"soapenc:string\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">"
				+ sn
				+ "</sn>"
				+ "<orgaddr xsi:type=\"soapenc:string\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">"
				+ orgaddr
				+ "</orgaddr>"
				+ "<telno xsi:type=\"soapenc:string\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">"
				+ telno
				+ "</telno>"
				+ "<content xsi:type=\"soapenc:string\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\"><![CDATA["
				+ content
				+ "]]></content>"
				+ "<sendtime xsi:type=\"soapenc:string\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\"></sendtime>"
				+ "</chin:InsertDownSms>"
				+ "</soapenv:Body>"
				+ "</soapenv:Envelope>";
		os.write(xml.getBytes("UTF-8"));
		os.flush();
		os.close();

		connection.getInputStream();
	}

	public int send(Map<String, String> record) {
		
		String[] emails = record.get("email").split("[|]");
		String[] telnos = record.get("telno").split("[|]");
		String[] pernrs = record.get("pernr").split("[|]");
		
		// 获得并设置接收人
		Map<String, String> recieversMap = service.getRecievers(emails, telnos, pernrs);
		telMap = new HashMap<String, String>();
		for (String tel : telnos) {
			telMap.put(tel, tel);
		}
		for (String key : recieversMap.keySet()) {
			telMap.put(recieversMap.get(key), recieversMap.get(key));
		}
		
		// 拼装手机群发号码
		allTelno = new StringBuffer();
		for (String tel : telMap.keySet()) {
			allTelno.append(tel);
			allTelno.append(UnsConsts.MARK_COMMA);
		}
		allTelno.deleteCharAt(allTelno.length() - 1);
		
		Map<String, String> smsSendInfo = service.getSmsSendInfo(record);
		String sn = smsSendInfo.get("sn");
		String orgaddr = smsSendInfo.get("orgaddr");
		String content = smsSendInfo.get("content");
		String telno = allTelno.toString();

		if ("".equals(telno) || telno == null) {
			logger.log(Level.INFO,
					"Telephone can not be empty, message is rejected!");
			return 0;
		}
		
		// switch controller
		if ("N".equals(service.getSwitchOnFlg(record.get("sys")))) {
			logger.log(Level.INFO, ">>> Switch OFF!!!");
			return 0;
		}
		
		try {
			sendMessageOne(sn, orgaddr, telno, content);
		} catch (MalformedURLException e) {
			return 0;
		} catch (IOException e) {
			return 0;
		}

		return 1;
	}

}
