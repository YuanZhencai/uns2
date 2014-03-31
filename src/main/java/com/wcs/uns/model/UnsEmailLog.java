package com.wcs.uns.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
@Table(name = "UNS_EMAILLOG")
@NamedQueries({
	@NamedQuery(name="UnsEmailLog.findByDurationTime",
			query="SELECT e FROM UnsEmailLog e WHERE e.createdDatetime BETWEEN :durationDatetime AND :currentDatetime AND e.sysId = :sysId AND e.emailAddr = :msgAddr"),
	@NamedQuery(name="UnsEmailLog.findByDurationTime2",
			query="SELECT e FROM UnsEmailLog e WHERE e.createdDatetime BETWEEN :durationDatetime AND :currentDatetime AND e.sysId = :sysId")
})
public class UnsEmailLog {
	
    @Id
    @Column(unique = true, nullable = false)
    @TableGenerator(name = "unsEmailLog", table = "ID_GEN", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "UNSEMAILLOG", initialValue = 10000, allocationSize=100)
    @GeneratedValue(strategy = GenerationType.TABLE, generator="unsEmailLog")
    private Long id;
    
    @Column(name="SYSID", length=20, nullable=false)
    private String sysId;
    
    @Column(name="EMAIL_ADDR", length=2000, nullable=false)
    private String emailAddr;
    
    @Column(name="EMAIL_CC", length=2000, nullable=false)
    private String emailCc;
    
    @Column(name="EMAIL_BCC", length=2000, nullable=false)
    private String emailBcc;
    
    @Column(name="EMAIL_SUBJECT", length=4000, nullable=false)
    private String emailSubject;
    
    @Column(name="EMAIL_BODY", length=4000, nullable=false)
    private String emailBody;
    
    @Column(name="EMAIL_AUX", length=2000, nullable=true)
    private String emailAux;
    
    @Column(name="SENSITIVE_IND", length=1)
    private String sensitiveInd;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="EMAIL_DATETIME", nullable=false)
    private Date emailDatetime;
    
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

	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}
	
	public String getEmailCc() {
		return emailCc;
	}

	public void setEmailCc(String emailCc) {
		this.emailCc = emailCc;
	}
	
	public String getEmailBcc() {
		return emailBcc;
	}

	public void setEmailBcc(String emailBcc) {
		this.emailBcc = emailBcc;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getEmailBody() {
		return emailBody;
	}

	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}

	public String getEmailAux() {
		return emailAux;
	}

	public void setEmailAux(String emailAux) {
		this.emailAux = emailAux;
	}
	
	public String getSensitiveInd() {
		return sensitiveInd;
	}

	public void setSensitiveInd(String sensitiveInd) {
		this.sensitiveInd = sensitiveInd;
	}

	public Date getEmailDatetime() {
		return emailDatetime;
	}

	public void setEmailDatetime(Date emailDatetime) {
		this.emailDatetime = emailDatetime;
	}

	public Date getCreatedDatetime() {
		return createdDatetime;
	}

	public void setCreatedDatetime(Date createdDatetime) {
		this.createdDatetime = createdDatetime;
	}
	
	@PrePersist
    public void initTimestamp() {
		emailDatetime = new Date();
        createdDatetime = new Date();
    }
	
}
