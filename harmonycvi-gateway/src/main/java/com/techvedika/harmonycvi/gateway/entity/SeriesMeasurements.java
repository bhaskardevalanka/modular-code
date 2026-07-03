package com.techvedika.harmonycvi.gateway.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.techvedika.harmonycvi.gateway.util.HashMapConverter;
import com.techvedika.harmonycvi.gateway.util.HashMapListConverter;
import com.techvedika.harmonycvi.gateway.util.JsonArrayListAnyConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "harmonycvi", name = "series_measurements_data")
@JsonAutoDetect
public class SeriesMeasurements {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id")
    private String patientId;

    @Column(name = "study_id")
    private String studyId;

    @Column(name = "series_id")
    private String seriesId;

    @Column(name = "patient_weight")
    private String patientWeight;

    @Column(name = "patient_height")
    private String patientHeight;

    @Convert(converter = HashMapConverter.class)
    @Column(name = "common_data", columnDefinition = "text")
    private HashMap<String, Object> commonData;

    @Convert(converter = HashMapListConverter.class)
    @Column(name = "measurement_json", columnDefinition = "text")
    private List<Map<String, Object>> measurementJson;

    @Convert(converter = JsonArrayListAnyConverter.class)
    @Column(name = "instance_array", columnDefinition = "text")
    private ArrayList<?> instanceArray;

    @Column(name = "creation_date")
    private String creationDate;

    @Column(name = "last_updated_date")
    private String lastUpdatedDate;

    @Column(name = "version")
    private int version;

    @ManyToOne(optional = true)
    @JoinColumn(name = "bookmark_id", nullable = true)
    @JsonBackReference("bookmark-measurements")
    private Bookmarks bookmark;

	// Getters and Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
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

	public String getPatientWeight() {
		return patientWeight;
	}

	public void setPatientWeight(String patientWeight) {
		this.patientWeight = patientWeight;
	}

	public String getPatientHeight() {
		return patientHeight;
	}

	public void setPatientHeight(String patientHeight) {
		this.patientHeight = patientHeight;
	}

	public HashMap<String, Object> getCommonData() {
		return commonData;
	}

	public void setCommonData(HashMap<String, Object> commonData) {
		this.commonData = commonData;
	}

	public List<Map<String, Object>> getMeasurementJson() {
		return measurementJson;
	}

	public void setMeasurementJson(List<Map<String, Object>> measurementJson) {
		this.measurementJson = measurementJson;
	}

	public ArrayList<?> getInstanceArray() {
		return instanceArray;
	}

	public void setInstanceArray(ArrayList<?> instanceArray) {
		this.instanceArray = instanceArray;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(String lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Bookmarks getBookmark() {
		return bookmark;
	}

	public void setBookmark(Bookmarks bookmark) {
		this.bookmark = bookmark;
	}
}
