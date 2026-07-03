package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateCenterRequest {
	
	@Schema(description = "Organization's Name", example = "Medicover", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Organization name is required")
	private String orgName;
	
	@Schema(description = "Center's Name", example = "Medicover", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Center name is required")
	private String name;
	
	
	@Schema(description = "Organization's Id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Organization Id is required")
	private String orgId;

	@Schema(description = "Center's phoneNumber", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "phoneNumber is required")
	private String phone;
	
	@Schema(description = "Organization's address one", example = "Hyderabad", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "address is required")
	private String address1;
	
	@Schema(description = "Organization's address two", example = "Hyderabad", requiredMode = Schema.RequiredMode.REQUIRED)
	private String address2;
	
	
	@Schema(description = "Organization's city", example = "Hyderabad", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "city is required")
	private String city;
	
	@Schema(description = "Organization's state", example = "Telangana", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "state is required")
	private String state;
	
	@Schema(description = "Country of the center", example = "India", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "country is required")
	private String country;
	
	@Schema(description = "Area of the center", example = "Hitech-city", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "area is required")
	private String area;
	
	@Schema(description = "Organization's status", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "status is required")
	private String active;
	
	@Schema(description = "User's pinCode", example = "786543", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "pinCode is required")
	private String pinCode;

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

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
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

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getPinCode() {
		return pinCode;
	}

	public void setPinCode(String pinCode) {
		this.pinCode = pinCode;
	}

	
	
}
