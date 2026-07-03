package com.techvedika.harmonycvi.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Status of different processing stages")
public class StudyClassificationStatusRequest {

    @Schema(description = "Classification status", example = "In progress")
    @NotBlank(message = "classification key is required")
    @JsonProperty("Classification")
    private String classification;

    @Schema(description = "Qflow status", example = "not started")
    @NotBlank(message = "qflow key is required")
    @JsonProperty("Qflow")
    private String qflow;

    @Schema(description = "Ventricle Assessment status", example = "not started")
    @NotBlank(message = "ventricleAssessment key is required")
    @JsonProperty("VentricleAssessment")
    private String ventricleAssessment;

    @Schema(description = "GLS status", example = "not started")
    @NotBlank(message = "gls key is required")
    @JsonProperty("GLS")
    private String gls;

    @Schema(description = "DE status", example = "not started")
    @NotBlank(message = "de key is required")
    @JsonProperty("DE")
    private String de;

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getQflow() {
        return qflow;
    }

    public void setQflow(String qflow) {
        this.qflow = qflow;
    }

    public String getVentricleAssessment() {
        return ventricleAssessment;
    }

    public void setVentricleAssessment(String ventricleAssessment) {
        this.ventricleAssessment = ventricleAssessment;
    }

    public String getGls() {
        return gls;
    }

    public void setGls(String gls) {
        this.gls = gls;
    }

    public String getDe() {
        return de;
    }

    public void setDe(String de) {
        this.de = de;
    }
}
