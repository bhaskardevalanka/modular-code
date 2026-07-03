package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class GetEndVolumeInfoDto {

    @Schema(
        description = "Access key (WADO URL encoded string)",
        example = "-arc%2Faets%2FDCM4CHEE%2Fwado%3FrequestType%3DWADO%"
    )
    @NotBlank(message = "accessKey is required")
    private String accessKey;

    @Schema(
        description = "Study Instance UID",
        example = "1.3.12.2.1107.5.2.52.196352.30000024070110085478400000025"
    )
    @NotBlank(message = "studyInstanceUid is required")
    private String studyInstanceUid;

    @Schema(
        description = "Type of process (e.g., preprocess)",
        example = "preprocess"
    )
    @NotBlank(message = "type is required")
    private String type;

    @Schema(
        description = "List of series information"
    )
    private List<SeriesInfoDto> info;

    // Getters and Setters
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

    public List<SeriesInfoDto> getInfo() {
        return info;
    }
    public void setInfo(List<SeriesInfoDto> info) {
        this.info = info;
    }

    // -------- Inner DTOs --------

    public static class SeriesInfoDto {

        @Schema(
            description = "Series Instance UID",
            example = "1.3.12.2.1107.5.2.52.196352.2024070114022551247364046.5.0.0"
        )
        private String seriesInstanceUid;

        @Schema(
            description = "Indicates whether the series is updated",
            example = "No"
        )
        private String updated;

        @Schema(
            description = "List of volume information"
        )
        private List<VolumeInfoDto> volume_info;

        // Getters and Setters
        public String getSeriesInstanceUid() {
            return seriesInstanceUid;
        }
        public void setSeriesInstanceUid(String seriesInstanceUid) {
            this.seriesInstanceUid = seriesInstanceUid;
        }

        public String getUpdated() {
            return updated;
        }
        public void setUpdated(String updated) {
            this.updated = updated;
        }

        public List<VolumeInfoDto> getVolume_info() {
            return volume_info;
        }
        public void setVolume_info(List<VolumeInfoDto> volume_info) {
            this.volume_info = volume_info;
        }
    }

    public static class VolumeInfoDto {

        @Schema(description = "End-diastolic frame (LV)", example = "1.3.12..._24")
        private String EDV_frame_lv;

        @Schema(description = "End-systolic frame (LV)", example = "1.3.12..._12")
        private String ESV_frame_lv;

        @Schema(description = "End-diastolic frame (RV)", example = "1.3.12..._1")
        private String EDV_frame_rv;

        @Schema(description = "End-systolic frame (RV)", example = "1.3.12..._11")
        private String ESV_frame_rv;

        @Schema(description = "Area measurement", example = "1001.7421875")
        private Double area;

        @Schema(description = "Slice location", example = "-2.09452")
        private Double slice_location;

        // Getters and Setters
        public String getEDV_frame_lv() {
            return EDV_frame_lv;
        }
        public void setEDV_frame_lv(String EDV_frame_lv) {
            this.EDV_frame_lv = EDV_frame_lv;
        }

        public String getESV_frame_lv() {
            return ESV_frame_lv;
        }
        public void setESV_frame_lv(String ESV_frame_lv) {
            this.ESV_frame_lv = ESV_frame_lv;
        }

        public String getEDV_frame_rv() {
            return EDV_frame_rv;
        }
        public void setEDV_frame_rv(String EDV_frame_rv) {
            this.EDV_frame_rv = EDV_frame_rv;
        }

        public String getESV_frame_rv() {
            return ESV_frame_rv;
        }
        public void setESV_frame_rv(String ESV_frame_rv) {
            this.ESV_frame_rv = ESV_frame_rv;
        }

        public Double getArea() {
            return area;
        }
        public void setArea(Double area) {
            this.area = area;
        }

        public Double getSlice_location() {
            return slice_location;
        }
        public void setSlice_location(Double slice_location) {
            this.slice_location = slice_location;
        }
    }
}

