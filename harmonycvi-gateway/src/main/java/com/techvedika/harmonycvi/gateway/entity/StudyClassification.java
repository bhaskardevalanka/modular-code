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
@Table(schema = "harmonycvi", name = "study_classification")
public class StudyClassification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // Changed to Long for consistency

	@Column(name = "study_id")
	private String studyId;

	@Column(name = "series_id")
	private String seriesId;

	@Column(name = "sequence_type")
	private String sequenceType;

	@Column(name = "image_plane")
	private String imagePlane;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", updatable = false)
	private Date createdDt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_updated_time")
	private Date lastUpdatedDt;

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

	public String getSeriesId() {
		return seriesId;
	}

	public void setSeriesId(String seriesId) {
		this.seriesId = seriesId;
	}

	public String getSequenceType() {
		return sequenceType;
	}

	public void setSequenceType(String sequenceType) {
		this.sequenceType = sequenceType;
	}

	public String getImagePlane() {
		return imagePlane;
	}

	public void setImagePlane(String imagePlane) {
		this.imagePlane = imagePlane;
	}

	public Date getCreatedDt() {
		return createdDt;
	}

	public void setCreatedDt(Date createdDt) {
		this.createdDt = createdDt;
	}

	public Date getLastUpdatedDt() {
		return lastUpdatedDt;
	}

	public void setLastUpdatedDt(Date lastUpdatedDt) {
		this.lastUpdatedDt = lastUpdatedDt;
	}

	// Automatically set timestamps before persisting
	@PrePersist
	protected void onCreate() {
		Date now = new Date();
		if (createdDt == null) {
			createdDt = now;
		}
		if (lastUpdatedDt == null) {
			lastUpdatedDt = now;
		}
	}

	// Automatically update lastUpdatedDt before updating
	@PreUpdate
	protected void onUpdate() {
		lastUpdatedDt = new Date();
	}

}
