package com.wcs.uns.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
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
@Table(name="UNS_FAILEDMSG")
@NamedQueries({
	@NamedQuery(name="UnsFailedMsg.findBySysId", query="SELECT e FROM UnsFailedMsg e WHERE e.sysId = :sysId"),
	@NamedQuery(name="UnsFailedMsg.findVersionBySysId", query="SELECT MAX(e.updatedDatetime) FROM UnsFailedMsg e WHERE e.sysId = :sysId"),
	@NamedQuery(name="UnsFailedMsg.findByVersion", query="SELECT e FROM UnsFailedMsg e WHERE e.updatedDatetime <= :ts AND e.sysId = :sysId"),
	@NamedQuery(name="UnsFailedMsg.findByVersion2", query="SELECT e FROM UnsFailedMsg e WHERE e.updatedDatetime >= :ts AND e.sysId = :sysId"),
	@NamedQuery(name="UnsFailedMsg.findByDefunctInd", query="SELECT e FROM UnsFailedMsg e WHERE e.defunctInd = :defunctInd")})
public class UnsFailedMsg {

    @Id
    @Column(unique = true, nullable = false)
    @TableGenerator(name = "unsFailedMsg", table = "ID_GEN", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "UNSFAILEDMSG", initialValue = 10000, allocationSize=100)
    @GeneratedValue(strategy = GenerationType.TABLE, generator="unsFailedMsg")
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
    
    @Column(name="MSG_SUBJECT", length=4000, nullable=false)
    private String msgSubject;
    
    @Column(name="MSG_BODY", length=4000, nullable=false)
    private String msgBody;
    
    @Column(name="MSG_AUX")
    private String msgAux;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="MSG_DATETIME", nullable=false)
    private Date msgDatetime;
    
    @Column(name="FAILURE_TYPE", nullable=false)
    private int failureType;
    
    @Column(name="SENSITIVE_IND", length=1)
    private String sensitiveInd;
    
    @Column(name="DEFUNCT_IND", length=1, nullable=false)
    private String defunctInd;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="CREATED_DATETIME", nullable=false)
    private Date createdDatetime;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="UPDATED_DATETIME", nullable=false)
    private Date updatedDatetime;

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

	public String getMsgSubject() {
		return msgSubject;
	}

	public void setMsgSubject(String msgSubject) {
		this.msgSubject = msgSubject;
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

	public Date getMsgDatetime() {
		return msgDatetime;
	}

	public void setMsgDatetime(Date msgDatetime) {
		this.msgDatetime = msgDatetime;
	}

	public int getFailureType() {
		return failureType;
	}

	public void setFailureType(int failureType) {
		this.failureType = failureType;
	}
	
	public String getSensitiveInd() {
		return sensitiveInd;
	}

	public void setSensitiveInd(String sensitiveInd) {
		this.sensitiveInd = sensitiveInd;
	}

	public String getDefunctInd() {
		return defunctInd;
	}

	public void setDefunctInd(String defunctInd) {
		this.defunctInd = defunctInd;
	}

	public Date getCreatedDatetime() {
		return createdDatetime;
	}

	public void setCreatedDatetime(Date createdDatetime) {
		this.createdDatetime = createdDatetime;
	}

	public Date getUpdatedDatetime() {
		return updatedDatetime;
	}

	public void setUpdatedDatetime(Date updatedDatetime) {
		this.updatedDatetime = updatedDatetime;
	}

	@PrePersist
	public void initTimestamp() {
		msgDatetime = new Date();
		createdDatetime = new Date();
		updatedDatetime = new Date();
	}
	
	@PreUpdate
	public void updateTimestamp() {
		updatedDatetime = new Date();
	}
}
