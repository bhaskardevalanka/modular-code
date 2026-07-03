package com.techvedika.harmonycvi.gateway.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

@Entity
@Table(name = "study_extension", schema = "harmonycvi")
public class StudyExtension {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "study_id")
    private String studyInstanceUID;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "is_ai_processed")
    private Boolean isAIProcessed;

    @Column(name = "status", columnDefinition = "varchar(255) default 'Pending'")
    private String status;

    @Column(name = "no_of_images", nullable = false)
    private String noOfImages;

    @Column(name = "ai_process_status",columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> aiProcessStatus;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

	@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ai_process_time", nullable = false)
    private Date aiProcessTime;

    @Column(name = "qflow_status")
    private String qflowStatus;

    @Column(name = "ventricle_assessment_status")
    private String ventricleAssessmentStatus;

    @Column(name = "classification_status")
    private String classificationStatus;

    @Column(name = "dicom_images_count")
    private Long dicomImagesCount;
    
    @Column(name="patient_weight")
    private String patientWeight;

    @Column(name="patient_height")
    private String patientHeight;
    
    @Version
	@Column(name = "lock_version")
	private Long lockVersion;
    
    @Basic(optional = false)
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", updatable = false)
	private Date createdTime;

	@Basic(optional = false)
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_time")
	private Date updatedTime;
	
	@Basic(optional = false)
	@Column(name = "study_date")
	private String studyDate;

	@Basic(optional = false)
	@Column(name = "study_time")
	private String studyTime;
	
	@Column(name = "patient_name")
	private String patientName;
	
	@Column(name = "pat_sex")
	private String patSex;

	// Getters and setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNoOfImages() {
		return noOfImages;
	}

	public void setNoOfImages(String noOfImages) {
		this.noOfImages = noOfImages;
	}

	
	public void setAiProcessStatus(HashMap<String, Object> existingStatus) {
		this.aiProcessStatus = existingStatus;
	}

	public Date getAiProcessTime() {
		return aiProcessTime;
	}

	public void setAiProcessTime(Date aiProcessTime) {
		this.aiProcessTime = aiProcessTime;
	}

	public String getQflowStatus() {
		return qflowStatus;
	}

	public void setQflowStatus(String qflowStatus) {
		this.qflowStatus = qflowStatus;
	}

	public String getVentricleAssessmentStatus() {
		return ventricleAssessmentStatus;
	}

	public void setVentricleAssessmentStatus(String ventricleAssessmentStatus) {
		this.ventricleAssessmentStatus = ventricleAssessmentStatus;
	}

	public String getClassificationStatus() {
		return classificationStatus;
	}

	public void setClassificationStatus(String classificationStatus) {
		this.classificationStatus = classificationStatus;
	}

	public Long getDicomImagesCount() {
		return dicomImagesCount;
	}

	public void setDicomImagesCount(Long dicomImagesCount) {
		this.dicomImagesCount = dicomImagesCount;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	public String getPatientWeight() {
		return patientWeight;
	}

	public String getPatientHeight() { 
		return patientHeight;
	}
	
	public void setPatientWeight(String patientWeight) {
		this.patientWeight = patientWeight;
	}

	public void setPatientHeight(String patientHeight) {
		this.patientHeight = patientHeight;
	}
	
	public Boolean getIsAIProcessed() {
		return isAIProcessed;
	}

	public Map<String, Object> getAiProcessStatus() {
		return aiProcessStatus;
	}

	public void setIsAIProcessed(Boolean isAIProcessed) {
		this.isAIProcessed = isAIProcessed;
	}


	public void setAiProcessStatus(Map<String, Object> aiProcessStatus) {
		this.aiProcessStatus = aiProcessStatus;
	}
	
	public Date getCreatedTime() {
		return createdTime;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}
	
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}

	public String getStudyInstanceUID() {
		return studyInstanceUID;
	}

	public void setStudyInstanceUID(String studyInstanceUID) {
		this.studyInstanceUID = studyInstanceUID;
	}

	public Long getLockVersion() {
		return lockVersion;
	}

	public void setLockVersion(Long lockVersion) {
		this.lockVersion = lockVersion;
	}

	public String getStudyDate() {
		return studyDate;
	}

	public void setStudyDate(String studyDate) {
		this.studyDate = studyDate;
	}

	public String getStudyTime() {
		return studyTime;
	}

	public void setStudyTime(String studyTime) {
		this.studyTime = studyTime;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getPatSex() {
		return patSex;
	}

	public void setPatSex(String patSex) {
		this.patSex = patSex;
	}
	
	
}