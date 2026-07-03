package com.techvedika.harmonycvi.gateway.cloud;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.techvedika.harmonycvi.gateway.dto.PresignUploadResponse;
import com.techvedika.harmonycvi.gateway.dto.UploadPart;

@Service
@ConditionalOnProperty(name = "cloud.provider", havingValue = "gcp")
public class GcpPresignService implements StoragePresignService {

	private static final Logger LOG = LoggerFactory
			.getLogger(GcpPresignService.class);
	
    private final Storage storage;

    public GcpPresignService(Storage storage) {
        this.storage = storage;
    }

    @Override
    public PresignUploadResponse presignUpload(
            String bucketName,
            String objectKey,
            String contentType,
            long expiryMinutes) {

        LOG.info("presignUpload called | bucket={} key={} contentType={} expiry={}min",
                bucketName, objectKey, contentType, expiryMinutes);

        BlobInfo blobInfo =
                BlobInfo.newBuilder(bucketName, objectKey)
                        .setContentType(contentType)
                        .build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                expiryMinutes,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withV4Signature()
        );

        String objectUrl =
                "https://storage.googleapis.com/"
                        + bucketName + "/" + objectKey;

        LOG.info("Generated PUT signedUrl={}", signedUrl);
        LOG.info("Generated objectUrl={}", objectUrl);

        return new PresignUploadResponse(
                signedUrl.toString(),
                objectUrl
        );
    }

    @Override
    public String presignDownload(
            String bucketName,
            String objectKey,
            long expiryMinutes) {

        LOG.info("presignDownload called | bucket={} key={} expiry={}min",
                bucketName, objectKey, expiryMinutes);

        BlobInfo blobInfo =
                BlobInfo.newBuilder(bucketName, objectKey)
                        .build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                expiryMinutes,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                Storage.SignUrlOption.withV4Signature()
        );

        LOG.info("Generated GET signedUrl={}", signedUrl);

        return signedUrl.toString();
    }
    
    @Override
    public String getReportPresignedUrl(String basePath, String bucketName) {

        LOG.info("getReportPresignedUrl called | basePath={} bucket={}",
                basePath, bucketName);

        String pdfKey = basePath + "/preview.pdf";
        String htmlKey = basePath + "/report.html";

        LOG.info("Checking existence for pdfKey={}", pdfKey);

        if (objectExists(pdfKey, bucketName)) {
            LOG.info("PDF found. Generating download URL.");
            return presignDownload(bucketName, pdfKey, 10);
        }

        LOG.info("PDF not found. Checking htmlKey={}", htmlKey);

        if (objectExists(htmlKey, bucketName)) {
            LOG.info("HTML found. Generating download URL.");
            return presignDownload(bucketName, htmlKey, 10);
        }

        LOG.warn("Neither preview.pdf nor report.html exists for basePath={}", basePath);
        return "";
    }
    
    public boolean objectExists(String objectKey, String bucketName) {

        LOG.info("objectExists called | bucket={} key={}", bucketName, objectKey);
        Blob blob = storage.get(bucketName, objectKey);
        boolean exists = blob != null && blob.exists();
        LOG.info("objectExists result for key={} : {}", objectKey, exists);
        return exists;
    }

    @Override
    public boolean uploadFile(String objectKey, String localPath, String bucketName) {

        LOG.info("uploadFile called | bucket={} key={} localPath={}",
                bucketName, objectKey, localPath);

        try {
            BlobInfo blobInfo =
                    BlobInfo.newBuilder(bucketName, objectKey)
                            .build();

            LOG.info("File exists? {}", new File(localPath).exists());
            
            byte[] fileBytes = Files.readAllBytes(Paths.get(localPath));

            LOG.info("Read file from disk. Size={} bytes", fileBytes.length);

            storage.create(blobInfo, fileBytes);

            LOG.info("File successfully uploaded to GCS");
            return true;

        } catch (Exception e) {
            LOG.error("Failed to upload file to GCS", e);
            return false;
        }
    }

    @Override
    public String getStudyUploadUrl(
            String objectKey,
            long expiryMinutes,
            String bucketName) {

        LOG.info("getStudyUploadUrl called | bucket={} key={} expiry={}min",
                bucketName, objectKey, expiryMinutes);

        BlobInfo blobInfo =
                BlobInfo.newBuilder(bucketName, objectKey)
                        .setContentType("application/octet-stream")
                        .build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                expiryMinutes,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withV4Signature()
        );

        LOG.info("Generated study upload signedUrl={}", signedUrl);

        return signedUrl.toString();
    }
    
    @Override
    public String generateSingleUploadUrl(
            String bucketName,
            String objectKey,
            String contentType,
            long expiryMinutes) {

        LOG.info("generateSingleUploadUrl called | bucket={} key={} contentType={} expiry={}min",
                bucketName, objectKey, contentType, expiryMinutes);

        BlobInfo blobInfo =
                BlobInfo.newBuilder(bucketName, objectKey)
                        .setContentType(contentType)
                        .build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                expiryMinutes,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withV4Signature()
        );

        LOG.info("Generated single upload signedUrl={}", signedUrl);

        return signedUrl.toString();
    }
    
    @Override
    public JSONObject generateMultipartUploadUrls(
            String bucketName,
            String objectKey,
            int totalParts,
            long expiryMinutes) {

        LOG.info("generateMultipartUploadUrls called | bucket={} key={} expiry={}min",
                bucketName, objectKey, expiryMinutes);

        JSONObject response = new JSONObject();

        BlobInfo blobInfo =
                BlobInfo.newBuilder(bucketName, objectKey)
                        .build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                expiryMinutes,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.POST),
                Storage.SignUrlOption.withV4Signature(),
                Storage.SignUrlOption.withExtHeaders(
                        Map.of("x-goog-resumable", "start")
                )
        );

        LOG.info("Generated resumable session URL={}", signedUrl);

        response.put("resumableSessionUrl", signedUrl.toString());
        response.put("uploadType", "RESUMABLE");

        return response;
    }
    

    @Override
    public void completeMultipartUpload(
    		String bucketName,
            String objectKey,
            String uploadId,
            List<UploadPart> parts
    ) {
        // NO-OP for GCP
        // GCP resumable upload completes automatically
    }
    
    @Override
    public String generateDownloadUrl(
            String bucketName,
            String objectKey,
            long expiryMinutes) {

        LOG.info("generateDownloadUrl called | bucket={} key={} expiry={}min",
                bucketName, objectKey, expiryMinutes);

        try {

            Blob blob = storage.get(bucketName, objectKey);

            if (blob == null) {
                LOG.warn("Blob not found for key={}", objectKey);
                return "";
            }

            BlobInfo blobInfo =
                    BlobInfo.newBuilder(bucketName, objectKey)
                            .build();

            URL signedUrl = storage.signUrl(
                    blobInfo,
                    expiryMinutes,
                    TimeUnit.MINUTES,
                    Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                    Storage.SignUrlOption.withV4Signature()
            );

            LOG.info("Generated download signedUrl={}", signedUrl);

            return signedUrl.toString();

        } catch (Exception e) {
            LOG.error("Failed to generate download URL", e);
            return "";
        }
    }
    
    @Override
    public long getObjectSize(String bucketName, String objectKey) {
        LOG.info("getObjectSize called | bucket={} key={}", bucketName, objectKey);
        try {
            Blob blob = storage.get(bucketName, objectKey);
            if (blob == null) {
                LOG.warn("Blob not found for key={}", objectKey);
                return 0L;
            }
            long size = blob.getSize();
            LOG.info("Object size for key={}: {} bytes", objectKey, size);
            return size;
        } catch (Exception e) {
            LOG.error("Failed to get object size for bucket={}, key={}", bucketName, objectKey, e);
            return 0L;
        }
    }
}
