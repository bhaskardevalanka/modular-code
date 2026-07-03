package com.techvedika.harmonycvi.gateway.dto;

public class StudyDTO {

    private String studyInstanceUID;
    private String studyID;
    private String studyDate;
    private String studyTime;
    private String accessionNumber;
    private String admissionID;
    private String studyDescription;
    private String status;
    private String noOfImages;
    private String studyCustomAttribute1;
    private String studyCustomAttribute2;
    private String studyCustomAttribute3;
    private String accessControlID;
    private String rejectionState;
    private String expirationState;
    private String expirationDate;
    private String expirationExporterID;
    private String externalRetrieveAET;
    private long size;
    private String referringPhysicianName;
    private Boolean isAIProccessed;
    private String qflowStatus;
    private String ventricleAssessmentStatus;
    private String classificationStatus;
    private Long orgId;
    private Long dicomImagesCount;

    // Constructor
    public StudyDTO() {}

    // Getters and Setters

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public String getStudyID() {
        return studyID;
    }

    public void setStudyID(String studyID) {
        this.studyID = studyID;
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

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getAdmissionID() {
        return admissionID;
    }

    public void setAdmissionID(String admissionID) {
        this.admissionID = admissionID;
    }

    public String getStudyDescription() {
        return studyDescription;
    }

    public void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
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

    public String getStudyCustomAttribute1() {
        return studyCustomAttribute1;
    }

    public void setStudyCustomAttribute1(String studyCustomAttribute1) {
        this.studyCustomAttribute1 = studyCustomAttribute1;
    }

    public String getStudyCustomAttribute2() {
        return studyCustomAttribute2;
    }

    public void setStudyCustomAttribute2(String studyCustomAttribute2) {
        this.studyCustomAttribute2 = studyCustomAttribute2;
    }

    public String getStudyCustomAttribute3() {
        return studyCustomAttribute3;
    }

    public void setStudyCustomAttribute3(String studyCustomAttribute3) {
        this.studyCustomAttribute3 = studyCustomAttribute3;
    }

    public String getAccessControlID() {
        return accessControlID;
    }

    public void setAccessControlID(String accessControlID) {
        this.accessControlID = accessControlID;
    }

    public String getRejectionState() {
        return rejectionState;
    }

    public void setRejectionState(String rejectionState) {
        this.rejectionState = rejectionState;
    }

    public String getExpirationState() {
        return expirationState;
    }

    public void setExpirationState(String expirationState) {
        this.expirationState = expirationState;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getExpirationExporterID() {
        return expirationExporterID;
    }

    public void setExpirationExporterID(String expirationExporterID) {
        this.expirationExporterID = expirationExporterID;
    }

    public String getExternalRetrieveAET() {
        return externalRetrieveAET;
    }

    public void setExternalRetrieveAET(String externalRetrieveAET) {
        this.externalRetrieveAET = externalRetrieveAET;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getReferringPhysicianName() {
        return referringPhysicianName;
    }

    public void setReferringPhysicianName(String referringPhysicianName) {
        this.referringPhysicianName = referringPhysicianName;
    }

    public Boolean getIsAIProccessed() {
        return isAIProccessed;
    }

    public void setIsAIProccessed(Boolean isAIProccessed) {
        this.isAIProccessed = isAIProccessed;
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

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getDicomImagesCount() {
        return dicomImagesCount;
    }

    public void setDicomImagesCount(Long dicomImagesCount) {
        this.dicomImagesCount = dicomImagesCount;
    }
}
