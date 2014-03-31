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
@Table(name="UNS_SMSLOG")
@NamedQueries({
	@NamedQuery(name="UnsSmsLog.findByDurationTime",
			query="SELECT e FROM UnsSmsLog e WHERE e.createdDatetime BETWEEN :durationDatetime AND :currentDatetime AND e.sysId = :sysId AND e.smsTelno = :msgAddr"),
	@NamedQuery(name="UnsSmsLog.findByDurationTime2",
			query="SELECT e FROM UnsSmsLog e WHERE e.createdDatetime BETWEEN :durationDatetime AND :currentDatetime AND e.sysId = :sysId")
})
public class UnsSmsLog {

    @Id
    @Column(unique = true, nullable = false)
    @TableGenerator(name = "unsSmsLog", table = "ID_GEN", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "UNSSMSLOG", initialValue = 10000, allocationSize=100)
    @GeneratedValue(strategy = GenerationType.TABLE, generator="unsSmsLog")
    private Long id;
    
    @Column(name="SYSID", length=20, nullable=false)
    private String sysId;
    
    @Column(name="SMS_TELNO", length=2000, nullable=false)
    private String smsTelno;
    
    @Column(name="SMS_BODY", length=2000, nullable=false)
    private String smsBody;
    
    @Column(name="SMS_AUX", length=2000, nullable=true)
    private String smsAux;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="SMS_DATETIME", nullable=false)
    private Date smsDatetime;
    
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

	public String getSmsTelno() {
		return smsTelno;
	}

	public void setSmsTelno(String smsTelno) {
		this.smsTelno = smsTelno;
	}

	public String getSmsBody() {
		return smsBody;
	}

	public void setSmsBody(String smsBody) {
		this.smsBody = smsBody;
	}

	public String getSmsAux() {
		return smsAux;
	}

	public void setSmsAux(String smsAux) {
		this.smsAux = smsAux;
	}

	public Date getSmsDatetime() {
		return smsDatetime;
	}

	public void setSmsDatetime(Date smsDatetime) {
		this.smsDatetime = smsDatetime;
	}

	public Date getCreatedDatetime() {
		return createdDatetime;
	}

	public void setCreatedDatetime(Date createdDatetime) {
		this.createdDatetime = createdDatetime;
	}
	
	@PrePersist
	public void initTimestamp() {
		smsDatetime = new Date();
		createdDatetime = new Date();
	}
}
