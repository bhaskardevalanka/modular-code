package com.techvedika.harmonycvi.gateway.entity;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

@Entity
@Table(schema = "harmonycvi", name = "centers")
public class Center {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", length = 200)
	private String name;

	@Column(name = "address", length = 300)
	private String address;

	@Column(name = "country")
	private String country;

	@Column(name = "address1", length = 300)
	private String address1;

	@Column(name = "address2", length = 200)
	private String address2;

	@Column(name = "state", length = 300)
	private String state;

	@Column(name = "area", length = 200)
	private String area;

	@Column(name = "pin_code", length = 300)
	private String pinCode;

	@Column(name = "city", length = 300)
	private String city;

	// @Column(name = "time_zone", columnDefinition = "varchar(255) default
	// 'Asia/Calcutta'")
	// private String timeZone;

	@Column(name = "time_zone", length = 255)
	private String timeZone = "Asia/Calcutta";

	// @Column(name = "offset_value", columnDefinition = "varchar(255) default
	// '-330'")
	// private String offsetValue;

	@Column(name = "offset_value", length = 255)
	private String offsetValue = "-330";

	@Column(name = "latitude", length = 100)
	private String latitude;

	@Column(name = "phone", length = 100)
	private String phone;

	@Column(name = "longitude", length = 50)
	private String longitude;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "center")
	private List<DeviceDetails> deviceDetails;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", nullable = false)
	@JsonIgnore
	private Organization organization;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "centers")
	@JsonIgnore
	private List<User> users;

	@Column(name = "is_active", nullable = false)
	private Boolean active = false;

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
	
	@Version
	@Column(name = "lock_version")
	private Long lockVersion;

	// Getters and Setters

	public List<DeviceDetails> getDeviceDetails() {
		return deviceDetails;
	}

	public void setDeviceDetails(List<DeviceDetails> deviceDetails) {
		this.deviceDetails = deviceDetails;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */

	public void setId(Long id) {
		this.id = id;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
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

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getPinCode() {
		return pinCode;
	}

	public void setPinCode(String pinCode) {
		this.pinCode = pinCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getOffsetValue() {
		return offsetValue;
	}

	public void setOffsetValue(String offsetValue) {
		this.offsetValue = offsetValue;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Long getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(Long lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
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
}