package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ClassificationRangeRequestDto {

    @Schema(description = "Organization ID", example = "1")
    @NotNull(message = "Organization ID is required")
    private Integer orgId;

    @Schema(description = "Start date for filtering (YYYY-MM-DD)", example = "2025-09-01")
    @NotBlank(message = "Start date is required")
    private String startDate;

    @Schema(description = "End date for filtering (YYYY-MM-DD)", example = "2025-09-02")
    @NotBlank(message = "End date is required")
    private String endDate;

    @Schema(description = "Classification range", example = "classification")
    @NotBlank(message = "Classification range is required")
    private String classificationRange;

    @Schema(description = "Access key for retrieving study data", 
            example = "-arc%2Faets%2FDCM4CHEE%2Fwado%3FrequestType%3DWADO%")
    @NotBlank(message = "Access key is required")
    private String accessKey;

	public Integer getOrgId() {
		return orgId;
	}

	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getClassificationRange() {
		return classificationRange;
	}

	public void setClassificationRange(String classificationRange) {
		this.classificationRange = classificationRange;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
    
    
}
