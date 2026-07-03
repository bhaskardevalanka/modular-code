package com.techvedika.harmonycvi.gateway.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(schema = "harmonycvi", name = "archived_study")
public class ArchivedStudy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "org_id")
	private Long orgId;

	@Column(name = "org_name")
	private String orgName;

	@Column(name = "study_id")
	private String studyId;

	@Column(name = "study_name")
	private String studyName;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", updatable = false)
	private Date createdDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modified_time")
	private Date modifiedDate;

	// 0: uploaded to S3, 1: deleted from PACS, 2: deleted from DB
	@Column(name = "status")
	private int status;

	@Column(name = "study_location")
	private String studyLocation;

	// Getters and setters

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getStudyId() {
		return studyId;
	}

	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getStudyLocation() {
		return studyLocation;
	}

	public void setStudyLocation(String studyLocation) {
		this.studyLocation = studyLocation;
	}

	// Optional: Automatically set timestamps before saving and updating
	@PrePersist
	protected void onCreate() {
		if (createdDate == null) {
			createdDate = new Date();
		}
		if (modifiedDate == null) {
			modifiedDate = new Date();
		}
	}

	@PreUpdate
	protected void onUpdate() {
		modifiedDate = new Date();
	}
}
