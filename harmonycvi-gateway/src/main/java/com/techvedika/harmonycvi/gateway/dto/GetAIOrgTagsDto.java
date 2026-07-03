package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class GetAIOrgTagsDto {

    @Schema(
        description = "Access key for the study or resource",
        example = "-arc%2Faets%2FDCM4CHEE%2Fwado%3FrequestType%3DWADO%"
    )
    @NotBlank(message = "Access key is required")
    private String accessKey;

    @Schema(description = "Organization ID", example = "30")
    @NotNull(message = "Organization ID is required")
    private Integer orgId;

    // Getters and Setters
    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }
}
