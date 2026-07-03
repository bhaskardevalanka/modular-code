package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SaveStudyDto {

    @Schema(
        description = "Unique identifier of the study",
        example = "1.2.156.112605.66988331192476.250613023737.2.12060.16051"
    )
    @NotBlank(message = "Study ID is required")
    private String studyId;

    @Schema(description = "Organization ID", example = "13")
    @NotNull(message = "Organization ID is required")
    private Integer orgId;

    @Schema(description = "User ID", example = "19")
    @NotNull(message = "User ID is required")
    private Integer userId;

    // Getters and Setters
    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
