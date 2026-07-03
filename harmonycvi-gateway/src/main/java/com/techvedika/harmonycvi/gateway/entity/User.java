package com.techvedika.harmonycvi.gateway.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

@Entity
@Table(schema = "harmonycvi", name = "user_details")
public class User {

	/* ---------- Primary Key ---------- */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/* ---------- Basic Info ---------- */
	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password")
	private String password; // SHA‑256 hashed; empty when OTP used

	@Column(name = "onetime_password")
	private String onetimePassword;

	@Column(name = "jwt_token" ,columnDefinition = "text")
	private String jwtToken;

	/* ---------- Contact ---------- */
	@Column(name = "phone_no")
	private String phoneNo;

	@Column(name = "address_one")
	private String addressOne;

	@Column(name = "address_two")
	private String addressTwo;

	@Column(name = "city")
	private String city;

	@Column(name = "state")
	private String state;

	@Column(name = "pin_code")
	private String pinCode;

	/* ---------- Flags ---------- */
	@Column(name = "is_active", nullable = false, columnDefinition = "boolean default false")
	private Boolean active = false;

	@Column(name = "onetime_pwd_status", nullable = false, columnDefinition = "boolean default false")
	private Boolean onetimePwdStatus = false;

	@Column(name = "is_consultant", nullable = false, columnDefinition = "boolean default false")
	private Boolean isConsultant = false;
	
	@Column(name = "totp_secret")
	private String totpSecret;
	
	@Column(name = "is_mfa_enabled", nullable = false, columnDefinition = "boolean default false")
	private Boolean mfaEnabled = false;
	
	@Column(name = "login_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
	private Integer loginCount = 0;
	
	@Column(name = "token_hash", unique = true)
    private String tokenHash; // SHA-256 of token

	@Column(name = "device_info")
    private String deviceInfo; // user agent or fingerprint

	@Column(name = "created_at")
	private Instant createdAt;
    
	@Column(name = "expires_at")
	private Instant expiresAt;
    
    @Column(name = "revoked", nullable = false, columnDefinition = "boolean default false")
    private boolean revoked = false;
	
	/* ---------- Audit ---------- */
	@Column(name = "created_by")
	private Long createdBy;

	@Column(name = "last_updated_by")
	private Long lastUpdatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", updatable = false)
	private Date createdDt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_updated_time")
	private Date lastUpdatedDt;

	@Column(name = "upload_limit", nullable = false, columnDefinition = "integer default 5")
	private Integer uploadLimit = 5;

	/* ---------- Relationships ---------- */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	@ManyToMany(cascade = CascadeType.MERGE)
	@JoinTable(name = "user_organizations", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "org_id"))
	private Collection<Organization> orgs = new ArrayList<>();

	@ManyToMany(cascade = CascadeType.MERGE)
	@JoinTable(name = "user_devices", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "device_details_id"))
	private List<DeviceDetails> deviceDetails = new ArrayList<>();

	@ManyToMany
	@JoinTable(name = "user_centers", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "center_id"))
	private Collection<Center> centers = new HashSet<>();
	
	@Version
	@Column(name = "lock_version")
	private Long lockVersion;

	/* ---------- Getters & Setters (can be replaced with Lombok) ---------- */
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getOnetimePassword() {
		return onetimePassword;
	}

	public void setOnetimePassword(String onetimePassword) {
		this.onetimePassword = onetimePassword;
	}

	public String getJwtToken() {
		return jwtToken;
	}

	public void setJwtToken(String jwtToken) {
		this.jwtToken = jwtToken;
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

	public Boolean getOnetimePwdStatus() {
		return onetimePwdStatus;
	}

	public void setOnetimePwdStatus(Boolean onetimePwdStatus) {
		this.onetimePwdStatus = onetimePwdStatus;
	}

	public Boolean getConsultant() {
		return isConsultant;
	}

	public void setConsultant(Boolean consultant) {
		this.isConsultant = consultant;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Long getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(Long lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
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

	public Integer getUploadLimit() {
		return uploadLimit;
	}

	public void setUploadLimit(Integer uploadLimit) {
		this.uploadLimit = uploadLimit;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Collection<Organization> getOrgs() {
		return orgs;
	}

	public void setOrgs(Collection<Organization> orgs) {
		this.orgs = orgs;
	}

	public List<DeviceDetails> getDeviceDetails() {
		return deviceDetails;
	}

	public void setDeviceDetails(List<DeviceDetails> deviceDetails) {
		this.deviceDetails = deviceDetails;
	}

	public Collection<Center> getCenters() {
		return centers;
	}

	public void setCenters(Collection<Center> centers) {
		this.centers = centers;
	}

	public String getTotpSecret() {
		return totpSecret;
	}

	public void setTotpSecret(String totpSecret) {
		this.totpSecret = totpSecret;
	}

	public Boolean getMfaEnabled() {
		return mfaEnabled;
	}

	public void setMfaEnabled(Boolean mfaEnabled) {
		this.mfaEnabled = mfaEnabled;
	}

	public Integer getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(Integer loginCount) {
		this.loginCount = loginCount;
	}

	public String getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(String deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public void setRevoked(boolean revoked) {
		this.revoked = revoked;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public void setTokenHash(String tokenHash) {
		this.tokenHash = tokenHash;
	}
	
	
	

}
