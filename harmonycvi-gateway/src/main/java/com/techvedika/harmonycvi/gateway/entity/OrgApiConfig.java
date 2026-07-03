package com.techvedika.harmonycvi.gateway.entity;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "org_api_config")
public class OrgApiConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign Key
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization org;

    @Column(name = "api_url", nullable = false)
    private String apiUrl;

    @Column(name = "method", nullable = false)
    private String method;

    @Column(name = "request_params_type", nullable = false)
    private String requestParamsType;

    @Column(name = "headers", columnDefinition = "text")
    private String headers;

    @Column(name = "request_params", columnDefinition = "text")
    private String requestParams;

    @Column(name = "response_token_field", nullable = false)
    private String responseTokenField;
    
    @Column(name = "user_api_url")
    private String userApiUrl;

    @Column(name = "user_method")
    private String userMethod;

    @Column(name = "user_request_params_type")
    private String userRequestParamsType;

    @Column(name = "user_headers", columnDefinition = "text")
    private String userHeaders;

    @Column(name = "user_request_params", columnDefinition = "text")
    private String userRequestParams;
    
    @Column(name = "user_response_token_field", nullable = false)
    private String userResponseTokenField;

    // Getters and Setters
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Organization getOrg() {
		return org;
	}

	public void setOrg(Organization org) {
		this.org = org;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getRequestParamsType() {
		return requestParamsType;
	}

	public void setRequestParamsType(String requestParamsType) {
		this.requestParamsType = requestParamsType;
	}

	public String getRequestParams() {
		return requestParams;
	}

	public void setRequestParams(String requestParams) {
		this.requestParams = requestParams;
	}

	public String getHeaders() {
		return headers;
	}

	public void setHeaders(String headers) {
		this.headers = headers;
	}

	public String getResponseTokenField() {
		return responseTokenField;
	}

	public void setResponseTokenField(String responseTokenField) {
		this.responseTokenField = responseTokenField;
	}

	public String getUserApiUrl() {
		return userApiUrl;
	}

	public void setUserApiUrl(String userApiUrl) {
		this.userApiUrl = userApiUrl;
	}

	public String getUserMethod() {
		return userMethod;
	}

	public void setUserMethod(String userMethod) {
		this.userMethod = userMethod;
	}

	public String getUserRequestParamsType() {
		return userRequestParamsType;
	}

	public void setUserRequestParamsType(String userRequestParamsType) {
		this.userRequestParamsType = userRequestParamsType;
	}

	public String getUserHeaders() {
		return userHeaders;
	}

	public void setUserHeaders(String userHeaders) {
		this.userHeaders = userHeaders;
	}

	public String getUserRequestParams() {
		return userRequestParams;
	}

	public void setUserRequestParams(String userRequestParams) {
		this.userRequestParams = userRequestParams;
	}

	public String getUserResponseTokenField() {
		return userResponseTokenField;
	}

	public void setUserResponseTokenField(String userResponseTokenField) {
		this.userResponseTokenField = userResponseTokenField;
	}
    
}
