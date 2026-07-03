package com.techvedika.harmonycvi.gateway.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.techvedika.harmonycvi.gateway.util.HashMapListConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(schema = "harmonycvi", name = "study_annotation")
public class StudyAnnotation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "study_id")
	private String studyId;

//	@Column(name = "annotation_data")
//	private List<HashMap<String, Object>> annotationData;
	
	@Column(name = "annotation_data", columnDefinition = "TEXT")
	@Convert(converter = HashMapListConverter.class)
	private List<Map<String, Object>> annotationData;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", updatable = false)
	private Date createdDt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_updated_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private Date lastUpdatedDt;

	public List<Map<String, Object>> getAnnotationData() {
		return annotationData;
	}

	public void setAnnotationData(List<Map<String, Object>> annotationData) {
		this.annotationData = annotationData;
	}

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
}