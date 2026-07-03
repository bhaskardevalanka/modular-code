package com.techvedika.harmonycvi.gateway.aws;

import org.json.simple.JSONObject;

public interface S3UploadService {
	public JSONObject generatePreSignedUrl(String directoryName,String fileName,Integer totalParts,Long userId, Long orgId);
	
	public JSONObject uploadingtoS3Completed(JSONObject request);
	
	public JSONObject updateTransferStatus(Long Id,String studyId, boolean isTransferred);

	public JSONObject updateConfStudies(String studyId, Long userId, Long orgId);
}