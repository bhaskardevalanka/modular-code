package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class ResetPasswordRequest {
	
	@Schema(description = "User's oldPassword", example = "oldPassword123", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Old Password is required")
	private String oldPassword;
	
	@Schema(description = "User's newPassword", example = "newPassword123", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "New Password is required")
	private String newPassword;

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
	
	
}
