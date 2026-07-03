package com.techvedika.harmonycvi.gateway.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

@Entity
@Table(schema = "harmonycvi", name = "study_upload")
public class StudyUpload {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pk")
	private Long pk;

	@Column(name = "study_id")
	private String studyId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time")
	private Date createdDate;

	@Column(name = "study_location")
	private String studyLocation;

	@Column(name = "study_file_name")
	private String studyFileName;

	@Column(name = "is_active")
	private boolean isActive;

	@Column(name = "is_uploaded")
	private boolean isUploaded;

	@Column(name = "is_transferred")
	private boolean isTransferred;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "org_id")
	private Long orgId;
	
	@Version
	@Column(name = "lock_version")
	private Long lockVersion;

	// Default constructor
	public StudyUpload() {
	}

	// Getters and setters (could use Lombok @Getter and @Setter here)

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getStudyId() {
		return studyId;
	}

	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getStudyLocation() {
		return studyLocation;
	}

	public void setStudyLocation(String studyLocation) {
		this.studyLocation = studyLocation;
	}

	public String getStudyFileName() {
		return studyFileName;
	}

	public void setStudyFileName(String studyFileName) {
		this.studyFileName = studyFileName;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	public boolean isUploaded() {
		return isUploaded;
	}

	public void setUploaded(boolean uploaded) {
		isUploaded = uploaded;
	}

	public boolean isTransferred() {
		return isTransferred;
	}

	public void setTransferred(boolean transferred) {
		isTransferred = transferred;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		StudyUpload that = (StudyUpload) o;
		return pk != null && pk.equals(that.pk);
	}

	@Override
	public int hashCode() {
		return 31;
	}
}