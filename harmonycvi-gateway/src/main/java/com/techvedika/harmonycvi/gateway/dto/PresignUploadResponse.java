package com.techvedika.harmonycvi.gateway.dto;

public class PresignUploadResponse {

    private String uploadUrl;
    private String objectUrl;

    public PresignUploadResponse(String uploadUrl, String objectUrl) {
        this.uploadUrl = uploadUrl;
        this.objectUrl = objectUrl;
    }

    public String getUploadUrl() { return uploadUrl; }
    public String getObjectUrl() { return objectUrl; }
}
