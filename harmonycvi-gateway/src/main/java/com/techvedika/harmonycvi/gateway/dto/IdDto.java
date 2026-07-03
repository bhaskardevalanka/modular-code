package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class IdDto {

    @Schema(description = "Unique identifier", example = "872687")
    @NotBlank(message = "Id is required")
    private String id;

    // -------- Getters & Setters -------- //

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
