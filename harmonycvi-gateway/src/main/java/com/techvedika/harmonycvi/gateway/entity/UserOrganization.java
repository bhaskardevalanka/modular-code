package com.techvedika.harmonycvi.gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(schema = "harmonycvi", name = "user_organizations")
public class UserOrganization {

	@Id
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "org_id")
	private Long orgId;
	
	@Column(name = "status", nullable = false)
	private boolean status = true;
	
	@Version
	@Column(name = "lock_version")
	private Long lockVersion;
	
	@Column(name = "user_group")
	private String userGroup;

	public String getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(String userGroup) {
		this.userGroup = userGroup;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
}