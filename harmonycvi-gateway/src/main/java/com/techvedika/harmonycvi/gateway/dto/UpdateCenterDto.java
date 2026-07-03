package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class UpdateCenterDto {

    @Schema(description = "Unique identifier", example = "655545")
    private Long id;

    @Schema(description = "Organization identifier", example = "655259")
    private Long orgId;

    @Schema(description = "Organization name", example = "HarmonyCVI-Org")
    private String orgName;

    @Schema(description = "Name of the entity/person", example = "test1")
    private String name;

    @Schema(description = "Phone number", example = "1234567899")
    private String phone;

    @Schema(description = "Active status", example = "true")
    private Boolean active;

    @Schema(description = "Primary address", example = "Hyderabad")
    private String address1;

    @Schema(description = "Secondary address", example = "Hyderabad1")
    private String address2;

    @Schema(description = "City", example = "Hyderabad")
    private String city;

    @Schema(description = "State", example = "Telangana")
    private String state;

    @Schema(description = "Country", example = "India")
    private String country;

    @Schema(description = "Area/Locality", example = "Hitech city")
    private String area;

    @Schema(description = "Pin/Zip code", example = "901823")
    private String pinCode;
    
    @Schema(description = "Status update status", example = "yes")
    private String isStatusUpdate;
    // ---------- Getters & Setters ---------- //

    public String getIsStatusUpdate() {
		return isStatusUpdate;
	}

	public void setIsStatusUpdate(String isStatusUpdate) {
		this.isStatusUpdate = isStatusUpdate;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }
}
