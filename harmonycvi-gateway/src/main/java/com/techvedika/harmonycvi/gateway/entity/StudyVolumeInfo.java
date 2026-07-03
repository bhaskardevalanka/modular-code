package com.techvedika.harmonycvi.gateway.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.techvedika.harmonycvi.gateway.util.JsonListObjectConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(schema = "harmonycvi", name = "study_volume_info")
public class StudyVolumeInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "study_id")
	private String studyId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", updatable = false)
	private Date createdTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_time")
	private Date updatedTime;

	@Column(name = "end_volume", columnDefinition = "TEXT")
	@Convert(converter = JsonListObjectConverter.class)
	private List<Object> endVolume;

	@ManyToOne
	@JoinColumn(name = "bookmark", nullable = false)
	@JsonBackReference("bookmark-volume-info")
	private Bookmarks bookmark;

	@Column(name = "version")
	private int version;

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

	public List<Object> getEndVolume() {
		return endVolume;
	}

	public void setEndVolume(List<Object> endVolume) {
		this.endVolume = endVolume;
	}

	@PrePersist
	public void onPrePersist() {
		Date now = new Date();
		createdTime = now;
		updatedTime = now;
	}

	@PreUpdate
	public void onPreUpdate() {
		Date now = new Date();
		updatedTime = now;
	}

	public Bookmarks getBookmark() {
		return bookmark;
	}

	public void setBookmark(Bookmarks bookmark) {
		this.bookmark = bookmark;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}
