package com.techvedika.harmonycvi.gateway.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@JsonAutoDetect
@Entity
@Table(schema = "harmonycvi", name = "series_segments")
public class SeriesSegments {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "study_id")
	private String studyId;

	@Column(name = "series_id")
	private String seriesId;

	@Column(name = "segment_type")
	private String segment_type;

	@Column(name = "instance_array")
	private ArrayList<HashMap<String, Object>> instanceArray;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creation_date", updatable = false)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_updated_date")
	private Date lastUpdatedDate;

	@Column(name = "type")
	private String type;

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

	public String getSegment_type() {
		return segment_type;
	}

	public void setSegment_type(String segment_type) {
		this.segment_type = segment_type;
	}

	public ArrayList<HashMap<String, Object>> getInstanceArray() {
		return instanceArray;
	}

	public void setInstanceArray(ArrayList<HashMap<String, Object>> instanceArray) {
		this.instanceArray = instanceArray;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(Date lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}