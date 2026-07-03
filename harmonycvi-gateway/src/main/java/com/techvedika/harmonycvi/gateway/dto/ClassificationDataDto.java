package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Classification data for each series")
public class ClassificationDataDto {

    @Schema(description = "Series Instance UID", 
            example = "1.2.156.112605.66988331192476.250613030429.3.12508.16201")
    @NotBlank(message = "seriesInstanceUid is required")
    private String seriesInstanceUid;

    @Schema(description = "Type of sequence (e.g., Functional, Morphology, Mapping)", 
            example = "Functional")
    @NotBlank(message = "SequenceType is required")
    private String sequenceType;

    @Schema(description = "Image plane (e.g., Four chamber view, Sagittal, Short Axis View)", 
            example = "Two chamber view")
    @NotBlank(message = "ImagePlane is required")
    private String imagePlane;

    @Schema(description = "Protocol name used for acquisition", 
            example = "bssfp_cine_2ch")
    @NotBlank(message = "ProtocolName is required")
    private String protocolName;

    // getters and setters
    public String getSeriesInstanceUid() {
        return seriesInstanceUid;
    }

    public void setSeriesInstanceUid(String seriesInstanceUid) {
        this.seriesInstanceUid = seriesInstanceUid;
    }

    public String getSequenceType() {
        return sequenceType;
    }

    public void setSequenceType(String sequenceType) {
        this.sequenceType = sequenceType;
    }

    public String getImagePlane() {
        return imagePlane;
    }

    public void setImagePlane(String imagePlane) {
        this.imagePlane = imagePlane;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }
}
