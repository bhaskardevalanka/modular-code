package com.techvedika.harmonycvi.gateway.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateOrganizationDto {

    @Schema(description = "User ID", example = "872687")
    private Long id;

    @Schema(description = "User name", example = "NVD")
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "User email", example = "n1@yopmail.com")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "Phone number", example = "1234567890")
    private String phoneNo;

    @Schema(description = "Address line 1", example = "xxx")
    private String addressOne;

    @Schema(description = "Address line 2", example = "xx")
    private String addressTwo;

    @Schema(description = "City", example = "xx")
    private String city;

    @Schema(description = "State", example = "xxxx")
    private String state;

    @Schema(description = "Pin code", example = "123456")
    private String pinCode;

    @Schema(description = "Is account active", example = "true")
    @NotNull
    private Boolean active;

    @Schema(description = "Is consultant", example = "false")
    @NotNull
    private Boolean isConsultant;
    
    @Schema(description = "Pacs url", example = "123456")
    private String pacsUrl;
    
    @Schema(description = "Validation url", example = "123456")
    private String validationUrl;
    
    @Schema(description = "check if it has external pacs", example = "true")
    private String hasExternalPacs;
    
    @Schema(description = "Organization status", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Key for status update", example = "yes")
    private String isStatusUpdate;

    @Schema(
            description = "Organization UI preferences",
            example = "{ \"upload_button\": true, \"reprocess\": true, \"delete\": false }"
        )
        private Map<String, Object> preferences;
    // ----------- Getters & Setters ----------- //

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getIsConsultant() {
		return isConsultant;
	}

	public void setIsConsultant(Boolean isConsultant) {
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

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getIsStatusUpdate() {
		return isStatusUpdate;
	}

	public void setIsStatusUpdate(String isStatusUpdate) {
		this.isStatusUpdate = isStatusUpdate;
	}

	
	
	
    
}
