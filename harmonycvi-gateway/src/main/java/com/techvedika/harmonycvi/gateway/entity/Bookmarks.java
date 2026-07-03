package com.techvedika.harmonycvi.gateway.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

@Entity
@Table(schema = "harmonycvi", name = "bookmarks")
public class Bookmarks {

	public static final Logger LOG = LoggerFactory.getLogger(Bookmarks.class);

	public static final String FIND_BY_ID = "Bookmarks.findById";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "description",columnDefinition = "varchar")
	private String description;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", updatable = false)
	private Date createdDt;

	@Column(name = "study_iuid", nullable = false,columnDefinition = "varchar")
	private String studyInstanceUID;

	@OneToMany(mappedBy = "bookmark", cascade = CascadeType.ALL,orphanRemoval = true)
	 @JsonManagedReference("bookmark-measurements")
	private Collection<SeriesMeasurements> series_measurments = new ArrayList<SeriesMeasurements>();
	
	@OneToMany(mappedBy = "bookmark", cascade = CascadeType.ALL,orphanRemoval = true)
	@JsonManagedReference("bookmark-parameter")
	private Collection<SeriesParameter> series_parameter = new ArrayList<SeriesParameter>();

	@OneToMany(mappedBy = "bookmark", cascade = CascadeType.ALL,orphanRemoval = true)
	@JsonManagedReference("bookmark-study-parameter")
	private Collection<StudyParameter> study_parameter = new ArrayList<StudyParameter>();
	
	@OneToMany(mappedBy = "bookmark", cascade = CascadeType.ALL,orphanRemoval = true)
	@JsonManagedReference("bookmark-volume-info")
	private Collection<StudyVolumeInfo> study_volume_info = new ArrayList<StudyVolumeInfo>();

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "version")
	private int version;

	@Column(name = "name")
	private String name;

	@Column(name = "combined_series_id",columnDefinition = "TEXT")
	private String combinedSeriesIds;

	@Column(name = "is_archive")
	private int isArchive;

	@Column(name = "is_private_bookmark")
	private String isPrivateBookmark;
	
	@Version
	@Column(name = "lock_version")
	private Long lockVersion;

	// Getters and Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreatedDt() {
		return createdDt;
	}

	public void setCreatedDt(Date createdDt) {
		this.createdDt = createdDt;
	}

	public String getStudyInstanceUID() {
		return studyInstanceUID;
	}

	public void setStudyInstanceUID(String studyInstanceUID) {
		this.studyInstanceUID = studyInstanceUID;
	}

	public Collection<SeriesMeasurements> getSeries_measurments() {
		return series_measurments;
	}

	public void setSeries_measurments(Collection<SeriesMeasurements> series_measurments) {
		this.series_measurments = series_measurments;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getCombinedSeriesIds() {
		if (this.combinedSeriesIds == null || this.combinedSeriesIds.isEmpty())
			return null;

		List<String> result = new ArrayList<>();
		for (String field : this.combinedSeriesIds.split(",")) {
			result.add(field.trim());
		}
		return result;
	}

	public void setCombinedSeriesIds(String combinedSeriesIds) {
		this.combinedSeriesIds = combinedSeriesIds;
	}

	public int getIsArchive() {
		return isArchive;
	}

	public void setIsArchive(int isArchive) {
		this.isArchive = isArchive;
	}

	public String getIsPrivateBookmark() {
		return isPrivateBookmark;
	}

	public void setIsPrivateBookmark(String isPrivateBookmark) {
		this.isPrivateBookmark = isPrivateBookmark;
	}

	public void setDescription(String description) {
		this.description = description;

	}

	public String getDescription() {
		return description;
	}

}
