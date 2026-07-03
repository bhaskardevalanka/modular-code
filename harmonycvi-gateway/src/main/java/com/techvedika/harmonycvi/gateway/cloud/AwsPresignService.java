package com.techvedika.harmonycvi.gateway.cloud;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.dto.PresignUploadResponse;
import com.techvedika.harmonycvi.gateway.dto.UploadPart;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;

@Service
@ConditionalOnProperty(name = "cloud.provider", havingValue = "aws")
public class AwsPresignService implements StoragePresignService {
	private static final Logger LOG = LoggerFactory
			.getLogger(AwsPresignService.class);
	
    private final S3Presigner presigner;
    
    private final S3Client s3Client;
    
    @Value("${aws.region}")
    private String region;

    public AwsPresignService(S3Presigner presigner,S3Client s3Client) {
        this.presigner = presigner;
        this.s3Client = s3Client;
    }

    @Override
    public PresignUploadResponse presignUpload(
            String bucketName,
            String objectKey,
            String contentType,
            long expiryMinutes) {

        LOG.info("presignUpload called with bucket={}, key={}, contentType={}, expiry={}min",
                bucketName, objectKey, contentType, expiryMinutes);

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(presignBuilder ->
                        presignBuilder
                                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                                .putObjectRequest(putBuilder ->
                                        putBuilder
                                                .bucket(bucketName)
                                                .key(objectKey)
                                                .contentType(contentType)
                                )
                );

        String objectUrl = String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                region,
                objectKey
        );

        LOG.info("Generated presigned PUT URL: {}", presignedRequest.url());
        LOG.info("Generated object URL: {}", objectUrl);

        return new PresignUploadResponse(
                presignedRequest.url().toString(),
                objectUrl
        );
    }

    @Override
    public String presignDownload(
            String bucketName,
            String objectKey,
            long expiryMinutes) {

        LOG.info("presignDownload called with bucket={}, key={}, expiry={}min",
                bucketName, objectKey, expiryMinutes);

        PresignedGetObjectRequest presignedRequest =
                presigner.presignGetObject(presignBuilder ->
                        presignBuilder
                                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                                .getObjectRequest(getBuilder ->
                                        getBuilder
                                                .bucket(bucketName)
                                                .key(objectKey)
                                )
                );

        LOG.info("Generated presigned GET URL: {}", presignedRequest.url());

        return presignedRequest.url().toString();
    }
    
    @Override
    public String getReportPresignedUrl(String basePath, String bucketName) {

        LOG.info("getReportPresignedUrl called with basePath={}, bucket={}",
                basePath, bucketName);

        String pdfKey = basePath + "/preview.pdf";
        String htmlKey = basePath + "/report.html";

        LOG.info("Checking existence for pdfKey={}", pdfKey);

        if (objectExists(pdfKey, bucketName)) {
            LOG.info("PDF exists. Generating download URL.");
            return presignDownload(bucketName, pdfKey, 10);
        }

        LOG.info("PDF not found. Checking htmlKey={}", htmlKey);

        if (objectExists(htmlKey, bucketName)) {
            LOG.info("HTML exists. Generating download URL.");
            return presignDownload(bucketName, htmlKey, 10);
        }

        LOG.warn("Neither PDF nor HTML exists for basePath={}", basePath);
        return "";
    }
    
    private boolean objectExists(String objectKey, String bucketName) {

        LOG.info("Checking object existence. bucket={}, key={}", bucketName, objectKey);

        try {
            s3Client.headObject(builder ->
                    builder.bucket(bucketName)
                           .key(objectKey)
            );

            LOG.info("Object exists: {}", objectKey);
            return true;

        } catch (NoSuchKeyException e) {
            LOG.info("Object does not exist (NoSuchKey): {}", objectKey);
            return false;

        } catch (S3Exception e) {
        	if (e.statusCode() == 404) {
                LOG.info("Object does not exist (404): {}", objectKey);
                return false;
            }

            String message = String.format(
                    "Failed to check object existence in S3. bucket=%s, key=%s",
                    bucketName,
                    objectKey
            );

            LOG.error(message, e);

            throw new IllegalStateException(message, e);
        }
    }
    
    @Override
    public String getStudyUploadUrl(
            String objectKey,
            long expiryMinutes,
            String bucketName) {

        LOG.info("getStudyUploadUrl called with bucket={}, key={}, expiry={}min",
                bucketName, objectKey, expiryMinutes);

        String url = presignUpload(
                bucketName,
                objectKey,
                "application/octet-stream",
                expiryMinutes
        ).getUploadUrl();

        LOG.info("Generated study upload URL: {}", url);

        return url;
    }

    @Override
    public boolean uploadFile(String objectKey, String localPath, String bucketName) {

        LOG.info("uploadFile called with bucket={}, key={}, localPath={}",
                bucketName, objectKey, localPath);

        LOG.info("File exists? {}", new File(localPath).exists());
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build(),
                    RequestBody.fromFile(Paths.get(localPath))
            );

            LOG.info("File successfully uploaded to S3");
            return true;

        } catch (Exception e) {
            LOG.error("Failed to upload file to S3", e);
            return false;
        }
    }
    
    @Override
    public void completeMultipartUpload(
            String bucketName,
            String objectKey,
            String uploadId,
            List<UploadPart> parts) {

        LOG.info("completeMultipartUpload called with bucket={}, key={}, uploadId={}",
                bucketName, objectKey, uploadId);

        LOG.info("Total parts received: {}", parts.size());

        List<CompletedPart> completedParts = parts.stream()
                .map(p -> CompletedPart.builder()
                        .partNumber(p.getPartNumber())
                        .eTag(p.geteTag())
                        .build())
                .toList();
        
        s3Client.completeMultipartUpload(builder -> builder
                .bucket(bucketName)
                .key(objectKey)
                .uploadId(uploadId)
                .multipartUpload(mu -> mu.parts(completedParts))
        );

        LOG.info("Multipart upload completed successfully");
    }
    
    @Override
    public String generateDownloadUrl(
            String bucketName,
            String objectKey,
            long expiryMinutes) {

        LOG.info("generateDownloadUrl called with bucket={}, key={}, expiry={}min",
                bucketName, objectKey, expiryMinutes);

        try {

            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build()
            );

            LOG.info("Object exists. Generating download URL.");

            PresignedGetObjectRequest presignedRequest =
                    presigner.presignGetObject(builder ->
                            builder.signatureDuration(Duration.ofMinutes(expiryMinutes))
                                   .getObjectRequest(getBuilder ->
                                           getBuilder.bucket(bucketName)
                                                     .key(objectKey)
                                   )
                    );

            LOG.info("Generated download URL: {}", presignedRequest.url());

            return presignedRequest.url().toString();

        } catch (Exception e) {
            LOG.error("Failed to generate download URL", e);
            return "";
        }
    }

    @Override
    public String generateSingleUploadUrl(
            String bucketName,
            String objectKey,
            String contentType,
            long expiryMinutes) {

        LOG.info("generateSingleUploadUrl called with bucket={}, key={}, contentType={}, expiry={}min",
                bucketName, objectKey, contentType, expiryMinutes);

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(builder ->
                        builder.signatureDuration(Duration.ofMinutes(expiryMinutes))
                               .putObjectRequest(putBuilder ->
                                       putBuilder.bucket(bucketName)
                                                 .key(objectKey)
                                                 .contentType(contentType)
                               )
                );

        LOG.info("Generated single upload URL: {}", presignedRequest.url());

        return presignedRequest.url().toString();
    }
    
    @Override
    public JSONObject generateMultipartUploadUrls(
    		String bucketName,
            String objectKey,
            int totalParts,
            long expiryMinutes
    ) {

    	LOG.info("generateMultipartUploadUrls called with bucket={}, key={}, totalParts={}, expiry={}min",
                bucketName, objectKey, totalParts, expiryMinutes);
    	
        JSONObject response = new JSONObject();

        CreateMultipartUploadResponse createResponse =
                s3Client.createMultipartUpload(
                        CreateMultipartUploadRequest.builder()
                                .bucket(bucketName)
                                .key(objectKey)
                                .build()
                );

        String uploadId = createResponse.uploadId();
        LOG.info("Created multipart upload with uploadId={}", uploadId);

        JSONArray partsArray = new JSONArray();

        for (int partNumber = 1; partNumber <= totalParts; partNumber++) {
            LOG.info("Generating presigned URL for part {}", partNumber);
            
            int finalPartNumber = partNumber;

            PresignedUploadPartRequest presignedPart =
                    presigner.presignUploadPart(builder ->
                            builder.signatureDuration(Duration.ofMinutes(expiryMinutes))
                                   .uploadPartRequest(uploadBuilder ->
                                           uploadBuilder.bucket(bucketName)
                                                        .key(objectKey)
                                                        .uploadId(uploadId)
                                                        .partNumber(finalPartNumber)
                                   )
                    );

            JSONObject partJson = new JSONObject();
            partJson.put("partNumber", partNumber);
            partJson.put("url", presignedPart.url().toString());

            partsArray.add(partJson);
        }

        response.put("bucket", bucketName);
        response.put("key", objectKey);
        response.put("uploadId", uploadId);
        response.put("parts", partsArray);
        LOG.info("Generated {} multipart presigned URLs", totalParts);
        return response;
    }
    
    @Override
    public long getObjectSize(String bucketName, String objectKey) {
        try {
            HeadObjectResponse headResponse = s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build()
            );
            long size = headResponse.contentLength();
            LOG.info("Object size for key={}: {} bytes", objectKey, size);
            return size;
        } catch (Exception e) {
            LOG.error("Failed to get object size for bucket={}, key={}", bucketName, objectKey, e);
            return 0L;
        }
    }
    
}
