package com.techvedika.harmonycvi.gateway.dicomweb;

public class StudyDTO {
    private String studyInstanceUID;
    private String studyDescription;
    private String studyDate;
    private String studyTime;
    private String patientName;
    private String patientSex;
    private String patientBirthDate;
    private String procedureCodes;
    private Integer seriesCount;

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public String getStudyDescription() {
        return studyDescription;
    }

    public void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
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

    public String getPatientSex() {
        return patientSex;
    }

    public void setPatientSex(String patientSex) {
        this.patientSex = patientSex;
    }

    public String getPatientBirthDate() {
        return patientBirthDate;
    }

    public void setPatientBirthDate(String patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    public String getProcedureCodes() {
        return procedureCodes;
    }

    public void setProcedureCodes(String procedureCodes) {
        this.procedureCodes = procedureCodes;
    }
    
    public Integer getSeriesCount() {
		return seriesCount;
	}

	public void setSeriesCount(Integer seriesCount) {
		this.seriesCount = seriesCount;
	}
    
}

