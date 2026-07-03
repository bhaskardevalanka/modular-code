package com.techvedika.harmonycvi.gateway.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateOrganizationRequest {
	
	@Schema(description = "Organization's Name", example = "Medicover", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Organization name is required")
	private String name;
	
	
	@Schema(description = "Organization's email address", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Email is required")
	@Email(message = "Email should be valid")
	private String email;
	

	@Schema(description = "Organization's phoneNumber", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "phoneNumber is required")
	private String phoneNo;
	
	@Schema(description = "Organization's address one", example = "Hyderabad", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "address is required")
	private String addressOne;
	
	@Schema(description = "Organization's address two", example = "Hyderabad", requiredMode = Schema.RequiredMode.REQUIRED)
	private String addressTwo;
	
	
	@Schema(description = "Organization's city", example = "Hyderabad", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "city is required")
	private String city;
	
	@Schema(description = "Organization's state", example = "Telangana", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "state is required")
	private String state;
	
	@Schema(description = "Organization's status", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "status is required")
	private String active;
	
	@Schema(description = "User's pinCode", example = "786543", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "pinCode is required")
	private String pinCode;
	
	@Schema(description = "check for whether user is consultant or not", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "isConsultant is required")
	private String isConsultant;
	
	@Schema(description = "Pacs url", example = "123456")
    private String pacsUrl;
    
    @Schema(description = "Validation url", example = "123456")
    private String validationUrl;
    
    @Schema(description = "check if it has external pacs", example = "true")
    private String hasExternalPacs;
    

    @Schema(
        description = "Organization UI preferences",
        example = "{ \"upload_button\": true, \"reprocess\": true, \"delete\": false }"
    )
    private Map<String, Object> preferences;

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getPhoneNo() {
		return phoneNo;
	}


	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}


	public String getAddressOne() {
		return addressOne;
	}


	public void setAddressOne(String addressOne) {
		this.addressOne = addressOne;
	}


	public String getAddressTwo() {
		return addressTwo;
	}


	public void setAddressTwo(String addressTwo) {
		this.addressTwo = addressTwo;
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


	public String getIsConsultant() {
		return isConsultant;
	}


	public void setIsConsultant(String isConsultant) {
		this.isConsultant = isConsultant;
	}


	public String getPacsUrl() {
		return pacsUrl;
	}


	public void setPacsUrl(String pacsUrl) {
		this.pacsUrl = pacsUrl;
	}


	public String getValidationUrl() {
		return validationUrl;
	}


	public void setValidationUrl(String validationUrl) {
		this.validationUrl = validationUrl;
	}


	public String getHasExternalPacs() {
		return hasExternalPacs;
	}


	public void setHasExternalPacs(String hasExternalPacs) {
		this.hasExternalPacs = hasExternalPacs;
	}


	public Map<String, Object> getPreferences() {
		return preferences;
	}


	public void setPreferences(Map<String, Object> preferences) {
		this.preferences = preferences;
	}



}
