package com.techvedika.harmonycvi.gateway.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.techvedika.harmonycvi.gateway.util.HashMapConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

@Entity
@Table(schema = "harmonycvi", name = "ai_org_tags")
public class AIOrgTags {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "org_id")
	private Long orgId;

//	@Convert(converter = HashMapConverter.class) // Use the custom converter
//	@Column(name = "tags_data")
//	private HashMap<String, Object> tagsData;
	
	@Column(name = "tags_data", columnDefinition = "text")
	@Convert(converter = HashMapConverter.class)
	private HashMap<String, Object> tagsData;


	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", updatable = false)
	private Date createdTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_time")
	private Date updatedTime;

//	@Convert(converter = HashMapConverter.class) // Use the custom converter
//	@Column(name = "image_data")
//	private HashMap<String, Object> imageData;

	@Column(name = "image_data", columnDefinition = "text")
	@Convert(converter = HashMapConverter.class)
	private HashMap<String, Object> imageData; 
	
	@Version
	@Column(name = "lock_version")
	private Long lockVersion;

	// Getters and Setters

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

//	public HashMap<String, Object> getTagsData() {
//		return tagsData;
//	}
//
//	public void setTagsData(HashMap<String, Object> tagsData) {
//		this.tagsData = tagsData;
//	}
	
	

	public Date getCreatedTime() {
		return createdTime;
	}

	public HashMap<String, Object> getTagsData() {
		return tagsData;
	}

	public void setTagsData(HashMap<String, Object> tagsData) {
		this.tagsData = tagsData;
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

	public HashMap<String, Object> getImageData() {
		return imageData;
	}

	public void setImageData(HashMap<String, Object> imageData) {
		this.imageData = imageData;
	}

//	public HashMap<String, Object> getImageData() {
//		return imageData;
//	}
//
//	public void setImageData(HashMap<String, Object> imageData) {
//		this.imageData = imageData;
//	}
	
}
