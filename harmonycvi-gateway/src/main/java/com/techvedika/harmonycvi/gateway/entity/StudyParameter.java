package com.techvedika.harmonycvi.gateway.entity;

import java.util.Date;

import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "harmonycvi", name = "study_parameter")
public class StudyParameter {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "parameter_json", columnDefinition = "TEXT")
	private String parameterJson;

	@Column(name = "radial_strain", columnDefinition = "TEXT")
	private String radialStrainJson;

	@Column(name = "graph", columnDefinition = "TEXT")
	private String graph;

	@Column(name = "summary", columnDefinition = "TEXT")
	private String summary;

	@Column(name = "wall_motation", columnDefinition = "TEXT")
	private String wallMotation;

	@Column(name = "enhancement_pattern", columnDefinition = "TEXT")
	private String enhancementPattern;

	@Column(name = "study_id")
	private String studyId;

	// @CreationTimestamp
	@Column(name = "created_time", updatable = false)
	private Date createdTime;

	@UpdateTimestamp
	@Column(name = "updated_time")
	private Date updatedTime;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "bookmark", nullable = false)
	@JsonBackReference("bookmark-study-parameter")
	private Bookmarks bookmark;
	
	@Column(name="computed_series",columnDefinition = "TEXT")
	private String computedSeries;

	@Column(name = "version")
	private int version;

	public String getParameterJson() {
		return parameterJson;
	}

	public String getRadialStrainJson() {
		return radialStrainJson;
	}

	public String getGraph() {
		return graph;
	}

	public String getSummary() {
		return summary;
	}

	public String getWallMotation() {
		return wallMotation;
	}

	public String getEnhancementPattern() {
		return enhancementPattern;
	}

	public String getStudyId() {
		return studyId;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}

	public Bookmarks getBookmark() {
		return bookmark;
	}

	public int getVersion() {
		return version;
	}

	public void setParameterJson(String parameterJson) {
		this.parameterJson = parameterJson;
	}

	public void setRadialStrainJson(String radialStrainJson) {
		this.radialStrainJson = radialStrainJson;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public void setWallMotation(String wallMotation) {
		this.wallMotation = wallMotation;
	}

	public void setEnhancementPattern(String enhancementPattern) {
		this.enhancementPattern = enhancementPattern;
	}

	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}

	public void setBookmark(Bookmarks bookmark) {
		this.bookmark = bookmark;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	public String getComputedSeries() {
		return computedSeries;
	}

	public void setComputedSeries(String computedSeries) {
		this.computedSeries = computedSeries;
	}
}
