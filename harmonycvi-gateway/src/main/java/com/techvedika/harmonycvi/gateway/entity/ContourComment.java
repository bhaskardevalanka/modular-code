package com.techvedika.harmonycvi.gateway.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
@NamedQueries({
	@NamedQuery(
		    name= ContourComment.FIND_BY_STUDY_ID,
		    query="select sm from ContourComment sm " +"where sm.studyId = :studyId")
})


@Entity
@Table(schema = "harmonycvi", name= "contour_comment")
public class ContourComment {
	
	public static final String FIND_BY_STUDY_ID = "ContourComment.getStudyParameterByStudyId";

	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	private Long id;
	
	@Column(name="study_id")
	private String studyId;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", updatable = false)
	private Date createdTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_time")
	private Date updatedTime;
	
	@Column(name="comment")
	private String comment;

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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}