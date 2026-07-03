package com.techvedika.harmonycvi.gateway.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(schema = "harmonycvi", name = "login_history")
public class LoginHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "ip_address")
	private String ipAddress;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "logout_time")
	private Date logoutTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "login_time")
	private Date loginTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", updatable = false)
	private Date createdDt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_updated_time")
	private Date lastUpdatedDt;

	// Getters and setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Date getLogoutTime() {
		return logoutTime;
	}

	public void setLogoutTime(Date logoutTime) {
		this.logoutTime = logoutTime;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public Date getCreatedDt() {
		return createdDt;
	}

	public void setCreatedDt(Date createdDt) {
		this.createdDt = createdDt;
	}

	public Date getLastUpdatedDt() {
		return lastUpdatedDt;
	}

	public void setLastUpdatedDt(Date lastUpdatedDt) {
		this.lastUpdatedDt = lastUpdatedDt;
	}

	// Automatically set timestamps before persisting
	@PrePersist
	protected void onCreate() {
		Date now = new Date();
		if (createdDt == null) {
			createdDt = now;
		}
		if (lastUpdatedDt == null) {
			lastUpdatedDt = now;
		}
	}

	// Automatically update lastUpdatedDt before updating
	@PreUpdate
	protected void onUpdate() {
		lastUpdatedDt = new Date();
	}
}