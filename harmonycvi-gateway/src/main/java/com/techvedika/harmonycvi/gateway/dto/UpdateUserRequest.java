package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UpdateUserRequest {
	
	//{"id":2,"firstName":"test","lastName":"org1951","email":"testorg1951@yopmail.com","phoneNo":"9999999999","address1":"hyd","address2":"","city":"Hyderabad","state":"ts","pinCode":"678698","roleName":"ADMIN","roleId":2,"orgId":2,"orgName":"testorg1951","active":true}
	
	@Schema(description = "User Id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "user Id is required")
	private String id;
	
	
	@Schema(description = "User's firstName", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "FirstName is required")
	private String firstName;
	
	@Schema(description = "User's lastName", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "lastName is required")
	private String lastName;
	
	@Schema(description = "User's email address", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Email is required")
	@Email(message = "Email should be valid")
	private String email;
	

	@Schema(description = "User's phoneNumber", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "phoneNumber is required")
	private String phoneNo;
	
	@Schema(description = "User's address1", example = "Hyderabad", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "address is required")
	private String address1;
	
	@Schema(description = "User's address2", example = "Hyderabad", requiredMode = Schema.RequiredMode.REQUIRED)
	private String address2;
	
	
	@Schema(description = "User's city", example = "Hyderabad", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "city is required")
	private String city;
	
	@Schema(description = "User's state", example = "Telangana", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "state is required")
	private String state;
	
	@Schema(description = "User's status", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "status is required")
	private String isActive;
	
	@Schema(description = "User's orgId", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "orgId is required")
	private String orgId;
	
	@Schema(description = "User's role", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "roleId is required")
	private String roleId;
	
	@Schema(description = "User's pinCode", example = "786543", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "pinCode is required")
	private String pinCode;
	
	@Schema(description = "roleName", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "roleName is required")
	private String roleName;
	
	
	@Schema(description = "organization Name", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "organizatio name  is required")
	private String orgName;
	
	@Schema(description = "isStatusUpdate", example = "yes", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "isStatusUpdate is required")
	private String isStatusUpdate;
	
	@Schema(description = "isConsultant", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "isConsultant is required")
	private String isConsultant;
	
	@Schema(description = "centerId", example = "786543", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private String centerId;
	
	@Schema(description = "deviceId", example = "786543", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private String deviceId;

	public String getFirstName() {
		return firstName;
	}


	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}


	public String getLastName() {
		return lastName;
	}


	public void setLastName(String lastName) {
		this.lastName = lastName;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
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

	public String getOrgId() {
		return orgId;
	}


	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}


	public String getRoleId() {
		return roleId;
	}


	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}


	public String getPinCode() {
		return pinCode;
	}


	public void setPinCode(String pinCode) {
		this.pinCode = pinCode;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getPhoneNo() {
		return phoneNo;
	}


	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getIsActive() {
		return isActive;
	}


	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}


	public String getCenterId() {
		return centerId;
	}


	public void setCenterId(String centerId) {
		this.centerId = centerId;
	}


	public String getDeviceId() {
		return deviceId;
	}


	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}


	public String getRoleName() {
		return roleName;
	}


	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}


	public String getOrgName() {
		return orgName;
	}


	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}


	public String getIsStatusUpdate() {
		return isStatusUpdate;
	}


	public void setIsStatusUpdate(String isStatusUpdate) {
		this.isStatusUpdate = isStatusUpdate;
	}


	public String getIsConsultant() {
		return isConsultant;
	}


	public void setIsConsultant(String isConsultant) {
		this.isConsultant = isConsultant;
	}
	
	

	
}
