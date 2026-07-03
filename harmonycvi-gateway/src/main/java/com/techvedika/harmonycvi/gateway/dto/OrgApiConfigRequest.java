package com.techvedika.harmonycvi.gateway.dto;

import java.util.Map;

public class OrgApiConfigRequest {

    private Long orgId;
    private String apiUrl;
    private String method;

    private String requestParamsType; // FORM_URL_ENCODED / JSON / QUERY

    private Map<String, String> requestParams; // dynamic
    private Map<String, String> headers;       // dynamic
    private String responseTokenField;

    
    private String userApiUrl;
    private String userMethod;

    private String userRequestParamsType; // FORM_URL_ENCODED / JSON / QUERY

    private Map<String, String> userRequestParams; // dynamic
    private Map<String, String> userHeaders;       // dynamic
    private String userResponseTokenField;

    
    // getters & setters

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
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

	public Map<String, String> getRequestParams() {
		return requestParams;
	}

	public void setRequestParams(Map<String, String> requestParams) {
		this.requestParams = requestParams;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
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

	public Map<String, String> getUserRequestParams() {
		return userRequestParams;
	}

	public void setUserRequestParams(Map<String, String> userRequestParams) {
		this.userRequestParams = userRequestParams;
	}

	public Map<String, String> getUserHeaders() {
		return userHeaders;
	}

	public void setUserHeaders(Map<String, String> userHeaders) {
		this.userHeaders = userHeaders;
	}

	public String getUserResponseTokenField() {
		return userResponseTokenField;
	}

	public void setUserResponseTokenField(String userResponseTokenField) {
		this.userResponseTokenField = userResponseTokenField;
	}

    
}
