package com.wcs.uns.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * <p>Project: uns</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2011 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:shenbo@wcs-global.com">Shen Bo</a>
 */
@javax.persistence.Entity
@Table(name="UNS_MSGLOG")
public class UnsMsgLog {

    @Id
    @Column(unique = true, nullable = false)
    @TableGenerator(name = "unsMsgLog", table = "ID_GEN", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "UNSMSGLOG", initialValue = 10000, allocationSize=100)
    @GeneratedValue(strategy = GenerationType.TABLE, generator="unsMsgLog")
    private Long id;
    
    @Column(name="SYSID", length=20, nullable=false)
    private String sysId;
    
    @Column(name="MSG_TYPE", nullable=false)
    private int msgType;
    
    @Column(name="MSG_EMAIL", length=2000)
    private String msgEmail;
    
    @Column(name="MSG_EMAILCC", length=2000, nullable=false)
    private String msgEmailcc;
    
    @Column(name="MSG_EMAILBCC", length=2000, nullable=false)
    private String msgEmailbcc;
    
    @Column(name="MSG_TELNO", length=2000)
    private String msgTelno;
    
    @Column(name="MSG_PERNR", length=50)
    private String msgPernr;
    
    @Column(name="MSG_BODY", length=4000, nullable=false)
    private String msgBody;
    
    @Column(name="MSG_AUX", length=2000, nullable=true)
    private String msgAux;
    
    @Column(name="SENSITIVE_IND", length=1)
    private String sensitiveInd;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="MSG_DATETIME", nullable=false)
    private Date msgDatetime;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="CREATED_DATETIME", nullable=false)
    private Date createdDatetime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSysId() {
		return sysId;
	}

	public void setSysId(String sysId) {
		this.sysId = sysId;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getMsgEmail() {
		return msgEmail;
	}

	public void setMsgEmail(String msgEmail) {
		this.msgEmail = msgEmail;
	}
	
	public String getMsgEmailcc() {
		return msgEmailcc;
	}

	public void setMsgEmailcc(String msgEmailcc) {
		this.msgEmailcc = msgEmailcc;
	}
	
	public String getMsgEmailbcc() {
		return msgEmailbcc;
	}

	public void setMsgEmailbcc(String msgEmailbcc) {
		this.msgEmailbcc = msgEmailbcc;
	}

	public String getMsgTelno() {
		return msgTelno;
	}

	public void setMsgTelno(String msgTelno) {
		this.msgTelno = msgTelno;
	}

	public String getMsgPernr() {
		return msgPernr;
	}

	public void setMsgPernr(String msgPernr) {
		this.msgPernr = msgPernr;
	}

	public String getMsgBody() {
		return msgBody;
	}

	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}

	public String getMsgAux() {
		return msgAux;
	}

	public void setMsgAux(String msgAux) {
		this.msgAux = msgAux;
	}
	
	public String getSensitiveInd() {
		return sensitiveInd;
	}

	public void setSensitiveInd(String sensitiveInd) {
		this.sensitiveInd = sensitiveInd;
	}

	public Date getMsgDatetime() {
		return msgDatetime;
	}

	public void setMsgDatetime(Date msgDatetime) {
		this.msgDatetime = msgDatetime;
	}

	public Date getCreatedDatetime() {
		return createdDatetime;
	}

	public void setCreatedDatetime(Date createdDatetime) {
		this.createdDatetime = createdDatetime;
	}
	
	@PrePersist
	public void initTimestamp() {
		msgDatetime = new Date();
		createdDatetime = new Date();
	}
}
