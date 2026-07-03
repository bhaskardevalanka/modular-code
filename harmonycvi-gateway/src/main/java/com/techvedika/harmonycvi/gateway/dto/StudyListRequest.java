package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class StudyListRequest {
	
	@Schema(description = "Organization Id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Organization Id is required")
	private String orgId;
	
	
	@Schema(description = "Access Key for security", example = "DCM4CHE", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Access key is required")
	private String accessKey;
	
	@Schema(description = "Page number for paination", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Page numberis required")
	private String pageNumber;
	
	@Schema(description = "Page size for pagination", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Page sizeis required")
	private String pageSize;
	
	@Schema(description = "Search by word", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
	private String search="";
	
	@Schema(description = "Start Date", example = "01-01-2026", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private String startDate;
	
	@Schema(description = "End Date", example = "01-05-2026", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private String endDate;


	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(String pageNumber) {
		this.pageNumber = pageNumber;
	}

	public String getPageSize() {
		return pageSize;
	}

	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
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
	
}
