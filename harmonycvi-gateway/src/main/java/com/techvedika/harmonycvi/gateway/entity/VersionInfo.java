package com.techvedika.harmonycvi.gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "harmonycvi", name = "version_info")
public class VersionInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "type", nullable = false, unique = true, length = 40)
	private String type;

	@Column(name = "version", nullable = false, length = 20)
	private String version;

	public Long getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getVersion() {
		return version;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}