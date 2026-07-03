package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookmarkRequestDto {

    @Schema(description = "Bookmark ID", example = "148499")
    @NotNull(message = "bookmark_id is required")
    private Long bookmark_id;

    @Schema(
        description = "Access key (WADO URL encoded string)",
        example = "-arc%2Faets%2FDCM4CHEE%2Fwado%3FrequestType%3DWADO%"
    )
    @NotBlank(message = "accessKey is required")
    private String accessKey;

    @Schema(
        description = "Study Instance UID",
        example = "1.3.46.670589.50.2.12893097632070188868.26705949604234894716"
    )
    @NotBlank(message = "studyInstanceUid is required")
    private String studyInstanceUid;

    @Schema(description = "Request type", example = "bookmark")
    @NotBlank(message = "type is required")
    private String type;

    // ----------- Getters & Setters ----------- //

    public Long getBookmark_id() {
        return bookmark_id;
    }

    public void setBookmark_id(Long bookmark_id) {
        this.bookmark_id = bookmark_id;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getStudyInstanceUid() {
        return studyInstanceUid;
    }

    public void setStudyInstanceUid(String studyInstanceUid) {
        this.studyInstanceUid = studyInstanceUid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
