package com.techvedika.harmonycvi.gateway.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(schema = "harmonycvi", name = "series_parameter")
public class SeriesParameter {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "parameter_json",columnDefinition = "varchar")
	private String parameterJson;

	@Column(name = "graph",columnDefinition = "varchar")
	private String graph;

	@Column(name = "summary",columnDefinition = "varchar")
	private String summary;

	@Column(name = "study_id")
	private String studyId;

	@Column(name = "series_id")
	private String seriesId;

	// type refers to series type (Qflow, DE etc)
	@Column(name = "type")
	private String type;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", updatable = false)
	private Date createdTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_time")
	private Date updatedTime;

//	@OneToOne
//	@JoinColumn(name = "bookmark", nullable = false)
//	private Bookmarks bookmark;

	@ManyToOne
	@JoinColumn(name = "bookmark_id", nullable = false)
	@JsonBackReference("bookmark-parameter")
	private Bookmarks bookmark;

	@Column(name = "version")
	private int version;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Bookmarks getBookmark() {
		return bookmark;
	}

	public void setBookmark(Bookmarks bookmark) {
		this.bookmark = bookmark;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getGraph() {
		return graph;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}

	public String getParameterJson() {
		return parameterJson;
	}

	public void setParameterJson(String parameterJson) {
		this.parameterJson = parameterJson;
	}

	public String getStudyId() {
		return studyId;
	}

	public void setStudyId(String studyId) {
		this.studyId = studyId;
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

	public void setSeriesId(String seriesId) {
		this.seriesId = seriesId;
	}

	public String getSeriesId() {
		return seriesId;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}