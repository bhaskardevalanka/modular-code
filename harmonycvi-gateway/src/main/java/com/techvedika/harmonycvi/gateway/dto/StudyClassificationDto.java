package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Study classification details DTO")
public class StudyClassificationDto {

    @Schema(description = "Study Instance UID", 
            example = "1.2.156.112605.66988331192476.250613023737.2.12060.16051")
    @NotBlank(message = "studyInstanceUid is required")
    private String studyInstanceUid;

    @Schema(description = "Access key (WADO URL encoded string)", 
            example = "-arc%2Faets%2FDCM4CHEE%2Fwado%3FrequestType%3DWADO%")
    @NotBlank(message = "accessKey is required")
    private String accessKey;

    @Schema(description = "Classification data for series")
    @NotNull(message = "classification_data is required")
    @Valid
    private List<ClassificationDataDto> classification_data;

    @Schema(description = "Total image count", example = "1307")
    private int img_count;

    @Schema(description = "Total series count", example = "40")
    private int series_count;

    @Schema(description = "Total DICOM files count", example = "1307")
    private int dicomFilesCount;

    @Schema(description = "Patient height in cm", example = "0.0")
    private double patient_height;

    @Schema(description = "Patient weight in kg", example = "0.0")
    private double patient_weight;

    // getters and setters
    public String getStudyInstanceUid() {
        return studyInstanceUid;
    }

    public void setStudyInstanceUid(String studyInstanceUid) {
        this.studyInstanceUid = studyInstanceUid;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public List<ClassificationDataDto> getClassification_data() {
        return classification_data;
    }

    public void setClassification_data(List<ClassificationDataDto> classification_data) {
        this.classification_data = classification_data;
    }

    public int getImg_count() {
        return img_count;
    }

    public void setImg_count(int img_count) {
        this.img_count = img_count;
    }

    public int getSeries_count() {
        return series_count;
    }

    public void setSeries_count(int series_count) {
        this.series_count = series_count;
    }

    public int getDicomFilesCount() {
        return dicomFilesCount;
    }

    public void setDicomFilesCount(int dicomFilesCount) {
        this.dicomFilesCount = dicomFilesCount;
    }

    public double getPatient_height() {
        return patient_height;
    }

    public void setPatient_height(double patient_height) {
        this.patient_height = patient_height;
    }

    public double getPatient_weight() {
        return patient_weight;
    }

    public void setPatient_weight(double patient_weight) {
        this.patient_weight = patient_weight;
    }
}
