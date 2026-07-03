package com.techvedika.harmonycvi.gateway.cloud;

import java.time.OffsetDateTime;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.techvedika.harmonycvi.gateway.dto.PresignUploadResponse;
import com.techvedika.harmonycvi.gateway.dto.UploadPart;

@Service
@ConditionalOnProperty(name = "cloud.provider", havingValue = "azure")
public class AzurePresignService implements StoragePresignService {
    private static final Logger LOG = LoggerFactory.getLogger(AzurePresignService.class);

    private final BlobServiceClient blobServiceClient;

    public AzurePresignService(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    @Override
    public PresignUploadResponse presignUpload(String bucketName, String objectKey, String contentType, long expiryMinutes) {
        LOG.info("presignUpload invoked: bucket={}, key={}, contentType={}, expiry={}min", bucketName, objectKey, contentType, expiryMinutes);
        
        LOG.info("Ensuring container exists: {}", bucketName);
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(bucketName);
        try {
            if (!containerClient.exists()) {
                LOG.info("Container {} does not exist. Creating it...", bucketName);
                containerClient.create();
            }
        } catch (Exception e) {
            LOG.warn("Error while ensuring container exists (it might already exist or be in a transition state): {}", e.getMessage());
        }

        LOG.info("Getting blob client for: {}", objectKey);
        BlobClient blobClient = containerClient.getBlobClient(objectKey);

        LOG.info("Defining SAS permissions: write, create, add");
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plusMinutes(expiryMinutes),
                new BlobSasPermission().setWritePermission(true).setCreatePermission(true).setAddPermission(true)
        );
        sasValues.setContentType(contentType);

        LOG.info("Generating SAS token...");
        String sasToken = blobClient.generateSas(sasValues);
        String uploadUrl = blobClient.getBlobUrl() + "?" + sasToken;
        
        LOG.info("Generated Azure SAS upload URL: {}", uploadUrl);
        LOG.info("Blob base URL: {}", blobClient.getBlobUrl());

        return new PresignUploadResponse(uploadUrl, blobClient.getBlobUrl());
    }

    @Override
    public String presignDownload(String bucketName, String objectKey, long expiryMinutes) {
        LOG.info("presignDownload invoked: bucket={}, key={}, expiry={}min", bucketName, objectKey, expiryMinutes);
        
        LOG.info("Getting container client for: {}", bucketName);
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(bucketName);
        LOG.info("Getting blob client for: {}", objectKey);
        BlobClient blobClient = containerClient.getBlobClient(objectKey);

        LOG.info("Defining SAS permissions: read");
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plusMinutes(expiryMinutes),
                new BlobSasPermission().setReadPermission(true)
        );

        LOG.info("Generating SAS token for download...");
        String sasToken = blobClient.generateSas(sasValues);
        String downloadUrl = blobClient.getBlobUrl() + "?" + sasToken;
        
        LOG.info("Generated Azure SAS download URL: {}", downloadUrl);
        return downloadUrl;
    }

    @Override
    public String getReportPresignedUrl(String basePath, String bucketName) {
        LOG.info("getReportPresignedUrl invoked with basePath: {} and bucketName: {}", basePath, bucketName);
        String pdfKey = basePath + "/preview.pdf";
        String htmlKey = basePath + "/report.html";

        LOG.info("Checking for PDF existence at key: {}", pdfKey);
        if (objectExists(pdfKey, bucketName)) {
            LOG.info("PDF found, generating presigned download URL for: {}", pdfKey);
            return presignDownload(bucketName, pdfKey, 10);
        }

        LOG.info("PDF not found, checking for HTML existence at key: {}", htmlKey);
        if (objectExists(htmlKey, bucketName)) {
            LOG.info("HTML found, generating presigned download URL for: {}", htmlKey);
            return presignDownload(bucketName, htmlKey, 10);
        }

        LOG.warn("Neither PDF nor HTML report found for basePath: {}", basePath);
        return "";
    }

    private boolean objectExists(String objectKey, String bucketName) {
        LOG.info("Checking if object exists in Azure: bucket={}, key={}", bucketName, objectKey);
        try {
            boolean exists = blobServiceClient.getBlobContainerClient(bucketName).getBlobClient(objectKey).exists();
            LOG.info("Object existence result for key {}: {}", objectKey, exists);
            return exists;
        } catch (Exception e) {
            LOG.error("Exception occurred while checking object existence for key: " + objectKey, e);
            return false;
        }
    }

    @Override
    public String getStudyUploadUrl(String objectKey, long expiryMinutes, String bucketName) {
        LOG.info("getStudyUploadUrl invoked: bucket={}, key={}, expiry={}min", bucketName, objectKey, expiryMinutes);
        String url = presignUpload(bucketName, objectKey, "application/octet-stream", expiryMinutes).getUploadUrl();
        LOG.info("Returning study upload URL: {}", url);
        return url;
    }

    @Override
    public boolean uploadFile(String objectKey, String localPath, String bucketName) {
        LOG.info("uploadFile invoked: bucket={}, key={}, localPath={}", bucketName, objectKey, localPath);
        try {
            LOG.info("Ensuring container exists for upload: {}", bucketName);
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(bucketName);
            if (!containerClient.exists()) {
                containerClient.create();
            }

            LOG.info("Getting blob client for upload...");
            BlobClient blobClient = containerClient.getBlobClient(objectKey);
            
            LOG.info("Starting file upload from: {}", localPath);
            blobClient.uploadFromFile(localPath, true);
            LOG.info("File upload to Azure completed successfully.");
            return true;
        } catch (Exception e) {
            LOG.error("Failed to upload file to Azure for key: " + objectKey, e);
            return false;
        }
    }

    @Override
    public void completeMultipartUpload(String bucketName, String objectKey, String uploadId, List<UploadPart> parts) {
        LOG.info("completeMultipartUpload invoked: bucket={}, key={}, uploadId={}", bucketName, objectKey, uploadId);
        LOG.info("Total parts to commit: {}", (parts != null ? parts.size() : 0));
        
        // Note: For Azure Block Blobs, the client typically sends the block list directly.
        // If the server needs to commit it, we'd use blobClient.commitBlockList(blockIds).
        LOG.info("Azure Block Blob commit logic would be triggered here if required.");
    }

    @Override
    public String generateSingleUploadUrl(String bucketName, String objectKey, String contentType, long expiryMinutes) {
        LOG.info("generateSingleUploadUrl invoked: bucket={}, key={}, contentType={}, expiry={}min", bucketName, objectKey, contentType, expiryMinutes);
        String url = presignUpload(bucketName, objectKey, contentType, expiryMinutes).getUploadUrl();
        LOG.info("Returning single upload URL: {}", url);
        return url;
    }

    @Override
    public JSONObject generateMultipartUploadUrls(String bucketName, String objectKey, int totalParts, long expiryMinutes) {
        LOG.info("generateMultipartUploadUrls (Azure) invoked: bucket={}, key={}, totalParts={}, expiry={}min", bucketName, objectKey, totalParts, expiryMinutes);
        
        JSONObject response = new JSONObject();
        JSONArray partsArray = new JSONArray();

        LOG.info("Getting blob client for multipart upload URL generation...");
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(bucketName);
        BlobClient blobClient = containerClient.getBlobClient(objectKey);

        LOG.info("Generating SAS token with write/create/add permissions for blocks...");
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plusMinutes(expiryMinutes),
                new BlobSasPermission().setWritePermission(true).setCreatePermission(true).setAddPermission(true)
        );
        String sasToken = blobClient.generateSas(sasValues);

        for (int i = 1; i <= totalParts; i++) {
            JSONObject partJson = new JSONObject();
            partJson.put("partNumber", i);
            // The client will append &comp=block&blockid=... to this URL
            String partUrl = blobClient.getBlobUrl() + "?" + sasToken;
            partJson.put("url", partUrl);
            partsArray.add(partJson);
        }

        response.put("bucket", bucketName);
        response.put("key", objectKey);
        response.put("uploadType", "AZURE_BLOCK_BLOB");
        response.put("parts", partsArray);
        
        LOG.info("Generated {} part URLs for Azure Block Blob upload.", totalParts);
        return response;
    }

    @Override
    public String generateDownloadUrl(String bucketName, String objectKey, long expiryMinutes) {
        LOG.info("generateDownloadUrl invoked: bucket={}, key={}, expiry={}min", bucketName, objectKey, expiryMinutes);
        String url = presignDownload(bucketName, objectKey, expiryMinutes);
        LOG.info("Returning download URL: {}", url);
        return url;
    }
    
    @Override
    public long getObjectSize(String bucketName, String objectKey) {
        LOG.info("getObjectSize invoked: bucket={}, key={}", bucketName, objectKey);
        try {
            BlobClient blobClient = blobServiceClient
                    .getBlobContainerClient(bucketName)
                    .getBlobClient(objectKey);
            long size = blobClient.getProperties().getBlobSize();
            LOG.info("Object size for key={}: {} bytes", objectKey, size);
            return size;
        } catch (Exception e) {
            LOG.error("Failed to get object size for bucket={}, key={}", bucketName, objectKey, e);
            return 0L;
        }
    }
}
