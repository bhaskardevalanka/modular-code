package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class SaveSeriesParamsDto {

    @Schema(description = "Bookmark ID", example = "2")
    @NotBlank(message = "Bookmark ID is required")
    private String bookmarkId;

    @Schema(description = "Series type", example = "Qflow")
    private String seriesType;

    @Schema(description = "Access key", example = "-arc%2Faets%2FDCM4CHEE%2Fwado%3FrequestType%3DWADO%")
    private String accessKey;

    @Schema(description = "Study Instance UID")
    private String studyId;

    @Schema(description = "Series Instance UID")
    private String seriesId;

    @Schema(description = "Aorta data")
    private Object aorta;

    @Schema(description = "Pulmonary artery data")
    private Object pulmonaryArtery;

    @Schema(description = "Descriptive aorta data")
    private Object descAorta;

    @Schema(description = "Superior vena cava (SVC) data")
    private Object svc;

    @Schema(description = "Processing type", example = "preprocess")
    private String type;

    @Schema(description = "Phase Series Instance UID")
    private String phaseSeriesInstanceUid;

    @Schema(description = "Magnitude Series Instance UID")
    private String magnitudeSeriesInstanceUid;

    @Schema(description = "Series split info")
    private SeriesSplitDto seriesSplit;

    // --- Getters and Setters ---
    public String getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(String bookmarkId) {
        this.bookmarkId = bookmarkId;
    }

    public String getSeriesType() {
        return seriesType;
    }

    public void setSeriesType(String seriesType) {
        this.seriesType = seriesType;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public Object getAorta() {
        return aorta;
    }

    public void setAorta(Object aorta) {
        this.aorta = aorta;
    }

    public Object getPulmonaryArtery() {
        return pulmonaryArtery;
    }

    public void setPulmonaryArtery(Object pulmonaryArtery) {
        this.pulmonaryArtery = pulmonaryArtery;
    }

    public Object getDescAorta() {
        return descAorta;
    }

    public void setDescAorta(Object descAorta) {
        this.descAorta = descAorta;
    }

    public Object getSvc() {
        return svc;
    }

    public void setSvc(Object svc) {
        this.svc = svc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhaseSeriesInstanceUid() {
        return phaseSeriesInstanceUid;
    }

    public void setPhaseSeriesInstanceUid(String phaseSeriesInstanceUid) {
        this.phaseSeriesInstanceUid = phaseSeriesInstanceUid;
    }

    public String getMagnitudeSeriesInstanceUid() {
        return magnitudeSeriesInstanceUid;
    }

    public void setMagnitudeSeriesInstanceUid(String magnitudeSeriesInstanceUid) {
        this.magnitudeSeriesInstanceUid = magnitudeSeriesInstanceUid;
    }

    public SeriesSplitDto getSeriesSplit() {
        return seriesSplit;
    }

    public void setSeriesSplit(SeriesSplitDto seriesSplit) {
        this.seriesSplit = seriesSplit;
    }
}

// -------- Nested DTOs --------
class PulmonaryArteryDto {

    private Double minVelocity;
    private Double maxVelocity;
    private Double forwardVolume;
    private Double backwardVolume;
    private Double netVolume;
    private Double cardaiacOutput;
    private Double regurFrac;
    private Double heartRate;
    private GraphDto graph;

    // Getters and Setters
    public Double getMinVelocity() {
        return minVelocity;
    }

    public void setMinVelocity(Double minVelocity) {
        this.minVelocity = minVelocity;
    }

    public Double getMaxVelocity() {
        return maxVelocity;
    }

    public void setMaxVelocity(Double maxVelocity) {
        this.maxVelocity = maxVelocity;
    }

    public Double getForwardVolume() {
        return forwardVolume;
    }

    public void setForwardVolume(Double forwardVolume) {
        this.forwardVolume = forwardVolume;
    }

    public Double getBackwardVolume() {
        return backwardVolume;
    }

    public void setBackwardVolume(Double backwardVolume) {
        this.backwardVolume = backwardVolume;
    }

    public Double getNetVolume() {
        return netVolume;
    }

    public void setNetVolume(Double netVolume) {
        this.netVolume = netVolume;
    }

    public Double getCardaiacOutput() {
        return cardaiacOutput;
    }

    public void setCardaiacOutput(Double cardaiacOutput) {
        this.cardaiacOutput = cardaiacOutput;
    }

    public Double getRegurFrac() {
        return regurFrac;
    }

    public void setRegurFrac(Double regurFrac) {
        this.regurFrac = regurFrac;
    }

    public Double getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Double heartRate) {
        this.heartRate = heartRate;
    }

    public GraphDto getGraph() {
        return graph;
    }

    public void setGraph(GraphDto graph) {
        this.graph = graph;
    }
}

class GraphDto {
    private List<Double> x;
    private List<Double> y;
    private List<Double> z;

    // Getters and Setters
    public List<Double> getX() {
        return x;
    }

    public void setX(List<Double> x) {
        this.x = x;
    }

    public List<Double> getY() {
        return y;
    }

    public void setY(List<Double> y) {
        this.y = y;
    }

    public List<Double> getZ() {
        return z;
    }

    public void setZ(List<Double> z) {
        this.z = z;
    }
}

class SeriesSplitDto {
    private boolean magnitudeSplit;
    private boolean phaseSplit;

    // Getters and Setters
    public boolean isMagnitudeSplit() {
        return magnitudeSplit;
    }

    public void setMagnitudeSplit(boolean magnitudeSplit) {
        this.magnitudeSplit = magnitudeSplit;
    }

    public boolean isPhaseSplit() {
        return phaseSplit;
    }

    public void setPhaseSplit(boolean phaseSplit) {
        this.phaseSplit = phaseSplit;
    }
}
