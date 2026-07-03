package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

	@Schema(description = "User's email address", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Email is required")
	@Email(message = "Email should be valid")
	private String email;

	@Schema(description = "User's password", example = "P@ssw0rd", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Password is required")
	private String password;
	
	@Schema(description = "Admin check", example = "yes", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "isAdmin is required")
	private String isAdmin;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(String isAdmin) {
		this.isAdmin = isAdmin;
	}
	

}
