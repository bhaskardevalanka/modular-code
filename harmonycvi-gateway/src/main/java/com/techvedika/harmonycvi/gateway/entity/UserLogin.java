package com.techvedika.harmonycvi.gateway.entity;

import java.util.Date;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(schema = "harmonycvi", name = "user_login")
public class UserLogin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // Changed to Long for consistency

	@Email
	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@NotNull
	@Size(min = 6) // Ensure password has at least 6 characters
	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", updatable = false)
	private Date createdTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_time")
	private Date updatedTime;

	// Lifecycle methods for automatic timestamp handling
	@PrePersist
	protected void onCreate() {
		Date now = new Date();
		if (createdTime == null) {
			createdTime = now;
		}
		if (updatedTime == null) {
			updatedTime = now;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		updatedTime = new Date();
	}

	// Utility method for setting password securely
	public void setEncodedPassword(String password) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		this.password = encoder.encode(password); // Hash the password before storing
	}

	// Method to check if password matches
	public boolean checkPassword(String rawPassword) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder.matches(rawPassword, this.password);
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}
}