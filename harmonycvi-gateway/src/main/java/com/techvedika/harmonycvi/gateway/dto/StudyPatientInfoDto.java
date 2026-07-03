package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class StudyPatientInfoDto {

    @Schema(description = "Access key for retrieving study data", 
            example = "-arc%2Faets%2FDCM4CHEE%2Fwado%3FrequestType%3DWADO%")
    @NotBlank(message = "Access key is required")
    private String accessKey;

    @Schema(description = "Patient weight in kilograms", example = "90.0")
    @NotBlank(message = "Patient weight is required")
    private String patient_weight;

    @Schema(description = "Unique identifier of the study", 
            example = "1.3.12.2.1107.5.2.30.25638.30000025061309062828100000001")
    @NotBlank(message = "StudyInstanceUid is required")
    private String studyInstanceUid;

    @Schema(description = "Patient height in centimeters", example = "160")
    @NotBlank(message = "Patient height is required")
    private String patient_height;

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getPatient_weight() {
		return patient_weight;
	}

	public void setPatient_weight(String patient_weight) {
		this.patient_weight = patient_weight;
	}

	public String getStudyInstanceUid() {
		return studyInstanceUid;
	}

	public void setStudyInstanceUid(String studyInstanceUid) {
		this.studyInstanceUid = studyInstanceUid;
	}

	public String getPatient_height() {
		return patient_height;
	}

	public void setPatient_height(String patient_height) {
		this.patient_height = patient_height;
	}
    
    
}
