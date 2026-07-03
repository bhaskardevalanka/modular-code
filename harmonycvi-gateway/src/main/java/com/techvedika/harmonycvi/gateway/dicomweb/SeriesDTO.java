package com.techvedika.harmonycvi.gateway.dicomweb;

public class SeriesDTO {
    private String seriesInstanceUID;
    private String modality;
    private String institutionName;
    private String department;
    private String physicianName;
    private String stationName;
    private String ppsStartDate;
    private String ppsStartTime;

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public void setSeriesInstanceUID(String seriesInstanceUID) {
        this.seriesInstanceUID = seriesInstanceUID;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhysicianName() {
        return physicianName;
    }

    public void setPhysicianName(String physicianName) {
        this.physicianName = physicianName;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getPpsStartDate() {
        return ppsStartDate;
    }

    public void setPpsStartDate(String ppsStartDate) {
        this.ppsStartDate = ppsStartDate;
    }

    public String getPpsStartTime() {
        return ppsStartTime;
    }

    public void setPpsStartTime(String ppsStartTime) {
        this.ppsStartTime = ppsStartTime;
    }
    
}
