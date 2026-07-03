package com.techvedika.harmonycvi.gateway.dto;

public class PatientUpdateDTO {

    private String patientName;
    private String patientId;
    private Double patientSize;
    private Double patientWeight;
    private String institutionName;
    private String institutionId;
    private String stationName;
    private Integer forcePixelRepresentation;
	public String getPatientName() {
		return patientName;
	}
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	public String getPatientId() {
		return patientId;
	}
	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}
	public String getInstitutionName() {
		return institutionName;
	}
	public void setInstitutionName(String institutionName) {
		this.institutionName = institutionName;
	}
	public String getInstitutionId() {
		return institutionId;
	}
	public void setInstitutionId(String institutionId) {
		this.institutionId = institutionId;
	}
	public Double getPatientSize() {
		return patientSize;
	}
	public void setPatientSize(Double patientSize) {
		this.patientSize = patientSize;
	}
	public Double getPatientWeight() {
		return patientWeight;
	}
	public void setPatientWeight(Double patientWeight) {
		this.patientWeight = patientWeight;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public Integer getForcePixelRepresentation() {
		return forcePixelRepresentation;
	}
	public void setForcePixelRepresentation(Integer forcePixelRepresentation) {
		this.forcePixelRepresentation = forcePixelRepresentation;
	}
	
	
	
	

    // getters & setters
    
    
}

