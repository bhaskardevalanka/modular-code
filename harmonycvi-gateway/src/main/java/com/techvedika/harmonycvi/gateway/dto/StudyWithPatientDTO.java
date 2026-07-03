package com.techvedika.harmonycvi.gateway.dto;

public class StudyWithPatientDTO {
    private Long studyId;
    private String studyInstanceUID;
    private Long patientId;
    private String patientName;
    private Double patientHeight;
    private Double patientWeight;

    public StudyWithPatientDTO(Long studyId, String studyInstanceUID, Long patientId,
                               String patientName, Double patientHeight, Double patientWeight) {
        this.studyId = studyId;
        this.studyInstanceUID = studyInstanceUID;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientHeight = patientHeight;
        this.patientWeight = patientWeight;
    }

	public Long getStudyId() {
		return studyId;
	}

	public String getStudyInstanceUID() {
		return studyInstanceUID;
	}

	public Long getPatientId() {
		return patientId;
	}

	public String getPatientName() {
		return patientName;
	}

	public Double getPatientHeight() {
		return patientHeight;
	}

	public Double getPatientWeight() {
		return patientWeight;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	public void setStudyInstanceUID(String studyInstanceUID) {
		this.studyInstanceUID = studyInstanceUID;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public void setPatientHeight(Double patientHeight) {
		this.patientHeight = patientHeight;
	}

	public void setPatientWeight(Double patientWeight) {
		this.patientWeight = patientWeight;
	}
}