package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AssignPatientRequest {

    @Schema(description = "User ID", example = "783474")
    @NotNull(message = "userId is required")
    private Long userId;

    @Schema(description = "List of Study Instance UIDs",
            example = "[\"1.3.12.2.1107.5.2.47.179769.30000025082707050195100000005\"]")
    private List<String> studyList;

    // ----------- Getters & Setters ----------- //

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<String> getStudyList() {
        return studyList;
    }

    public void setStudyList(List<String> studyList) {
        this.studyList = studyList;
    }
}
