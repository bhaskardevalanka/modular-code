package com.techvedika.harmonycvi.gateway.entity;

import java.util.Date;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

@Entity
@Table(schema = "harmonycvi", name = "organization")
public class Organization {

	/* ---------- Primary Key ---------- */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/* ---------- Basic Columns ---------- */
	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "is_active", nullable = false, columnDefinition = "boolean default false")
	private Boolean active = false;
	
	@Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
	private Boolean isDeleted = false;

	@Column(name = "is_consultant", nullable = false, columnDefinition = "boolean default false")
	private Boolean consultant = false;

	@Column(name = "last_updated_by")
	private Long lastUpdatedBy;

	@Column(name = "created_by")
	private Long createdBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", updatable = false)
	private Date createdDt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_updated_time")
	private Date lastUpdatedDt;

	/* ---------- Contact / Address ---------- */
	@Column(name = "email")
	private String email;

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

	@Column(name = "upload_limit", nullable = false)
	private Integer uploadLimit = 10;
	
	@Version
	@Column(name = "lock_version")
	private Long lockVersion;
	
	@Column(name = "pacs_url" ,columnDefinition = "text")
	private String pacsUrl;
	
	@Column(name = "validation_url" ,columnDefinition = "text")
	private String validationUrl;
	
	@Column(name = "has_external_pacs")
	private Boolean hasExternalPacs;
	
	@Column(name = "preferences",columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> preferences;

	/* ---------- Getters & Setters ---------- */
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

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getConsultant() {
		return consultant;
	}

	public void setConsultant(Boolean c) {
		this.consultant = c;
	}

	public Long getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(Long v) {
		this.lastUpdatedBy = v;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long v) {
		this.createdBy = v;
	}

	public Date getCreatedDt() {
		return createdDt;
	}

	public void setCreatedDt(Date d) {
		this.createdDt = d;
	}

	public Date getLastUpdatedDt() {
		return lastUpdatedDt;
	}

	public void setLastUpdatedDt(Date d) {
		this.lastUpdatedDt = d;
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

	public void setAddressOne(String a1) {
		this.addressOne = a1;
	}

	public String getAddressTwo() {
		return addressTwo;
	}

	public void setAddressTwo(String a2) {
		this.addressTwo = a2;
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

	public Integer getUploadLimit() {
		return uploadLimit;
	}

	public void setUploadLimit(Integer ul) {
		this.uploadLimit = ul;
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

	public Boolean getHasExternalPacs() {
		return hasExternalPacs;
	}

	public void setHasExternalPacs(Boolean hasExternalPacs) {
		this.hasExternalPacs = hasExternalPacs;
	}

	public Map<String, Object> getPreferences() {
		return preferences;
	}

	public void setPreferences(Map<String, Object> preferences) {
		this.preferences = preferences;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	
	
	
}