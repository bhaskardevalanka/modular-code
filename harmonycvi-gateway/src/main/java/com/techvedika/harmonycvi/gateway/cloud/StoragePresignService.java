package com.techvedika.harmonycvi.gateway.cloud;

import java.util.List;

import org.json.simple.JSONObject;

import com.techvedika.harmonycvi.gateway.dto.PresignUploadResponse;
import com.techvedika.harmonycvi.gateway.dto.UploadPart;

public interface StoragePresignService {

	PresignUploadResponse presignUpload(
            String bucketName,
            String objectKey,
            String contentType,
            long expiryMinutes
    );

    String presignDownload(
            String bucketName,
            String objectKey,
            long expiryMinutes
    );
    
    String getReportPresignedUrl(String basePath,String bucketName);

    String getStudyUploadUrl(String objectKey, long expiryMinutes,String bucketName);

    boolean uploadFile(String objectKey, String localPath,String bucketName);
    
    void completeMultipartUpload(String bucketName,String objectKey, String uploadId, List<UploadPart> parts);

    String generateSingleUploadUrl(
    		String bucketName,
            String objectKey,
            String contentType,
            long expiryMinutes
    );

    JSONObject generateMultipartUploadUrls(
    		String bucketName,
            String objectKey,
            int totalParts,
            long expiryMinutes
    );
    
    String generateDownloadUrl(
    		String bucketName,
            String objectKey,
            long expiryMinutes
    );
    
    long getObjectSize(String bucketName, String objectKey);
}
