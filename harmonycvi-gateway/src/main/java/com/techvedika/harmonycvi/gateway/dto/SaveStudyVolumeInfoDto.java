package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class SaveStudyVolumeInfoDto {

    @Schema(
        description = "Access key (WADO URL encoded string)", 
        example = "-arc%2Faets%2FDCM4CHEE%2Fwado%3FrequestType%3DWADO%"
    )
    @NotBlank(message = "accessKey is required")
    private String accessKey;

    @Schema(
        description = "Study Instance UID", 
        example = "1.2.156.112605.66988331192476.250613023737.2.12060.16051"
    )
    @NotBlank(message = "studyInstanceUid is required")
    private String studyInstanceUid;

    @Schema(
        description = "Processing type", 
        example = "preprocess"
    )
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


    // ===== Inner DTOs =====

    public static class SeriesInfoDto {
        @Schema(
            description = "Series Instance UID", 
            example = "1.2.156.112605.66988331192476.250613032507.3.12508.15652"
        )
        private String seriesInstanceUid;

        @Schema(
            description = "Updated flag", 
            example = "No"
        )
        private String updated;

        @Schema(
            description = "Volume information list"
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
        @Schema(description = "End Diastolic Volume frame LV", example = "1.2.156.112605.66988331192476.250613033546.4.16052.310935")
        private String EDV_frame_lv;

        @Schema(description = "End Systolic Volume frame LV", example = "1.2.156.112605.66988331192476.250613033519.4.16052.256308")
        private String ESV_frame_lv;

        @Schema(description = "End Diastolic Volume frame RV", example = "")
        private String EDV_frame_rv;

        @Schema(description = "End Systolic Volume frame RV", example = "")
        private String ESV_frame_rv;

        @Schema(description = "Area measurement", example = "105.7018558274222")
        private Double area;

        @Schema(description = "Slice location", example = "-80.0590286")
        private Double slice_location;

        public String getEDV_frame_lv() {
            return EDV_frame_lv;
        }
        public void setEDV_frame_lv(String eDV_frame_lv) {
            EDV_frame_lv = eDV_frame_lv;
        }

        public String getESV_frame_lv() {
            return ESV_frame_lv;
        }
        public void setESV_frame_lv(String eSV_frame_lv) {
            ESV_frame_lv = eSV_frame_lv;
        }

        public String getEDV_frame_rv() {
            return EDV_frame_rv;
        }
        public void setEDV_frame_rv(String eDV_frame_rv) {
            EDV_frame_rv = eDV_frame_rv;
        }

        public String getESV_frame_rv() {
            return ESV_frame_rv;
        }
        public void setESV_frame_rv(String eSV_frame_rv) {
            ESV_frame_rv = eSV_frame_rv;
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