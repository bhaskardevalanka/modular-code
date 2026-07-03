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
@Table(schema = "harmonycvi", name = "study_clinical_details")
public class StudyClinicalDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // Changed to Long for consistency with JPA best practices

	@Column(name = "study_id")
	private String studyId;

	@Column(name = "file_location")
	private String fileLocation;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "status")
	private String status;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", updatable = false)
	private Date createdTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_time")
	private Date updatedTime;

	// Getters and setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStudyId() {
		return studyId;
	}

	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}

	public String getFileLocation() {
		return fileLocation;
	}

	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}

	// Automatically set timestamps before persisting
	@PrePersist
	protected void onCreate() {
		Date now = new Date();
		if (createdTime == null) {
			createdTime = now;
		}
		if (updatedTime == null) {
			updatedTime = now;
		}
	}

	// Automatically update updatedTime before updating
	@PreUpdate
	protected void onUpdate() {
		updatedTime = new Date();
	}

}
