package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class SaveAIProcessStatusRequest {
	
	@Schema(description = "Access Key for security", example = "DCM4CHE", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Access key is required")
	private String accessKey;
	
	@Schema(description = "Study Id to save the classification status", example = "1.2.156.112605.66988331192476.250613023737.2.12060.16051", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Study Id is required")
	private String studyId;
	
	
	 @Schema(description = "Processing status of the study")
	 @NotBlank(message = "Status is required")
	 private StudyClassificationStatusRequest status;


	public String getAccessKey() {
		return accessKey;
	}


	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}


	public String getStudyId() {
		return studyId;
	}


	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}


	public StudyClassificationStatusRequest getStatus() {
		return status;
	}


	public void setStatus(StudyClassificationStatusRequest status) {
		this.status = status;
	}
	 
	 

}

