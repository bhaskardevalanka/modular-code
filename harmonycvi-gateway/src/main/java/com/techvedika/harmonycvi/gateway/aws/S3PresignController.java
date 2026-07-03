package com.techvedika.harmonycvi.gateway.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.techvedika.harmonycvi.gateway.cloud.StoragePresignService;
import com.techvedika.harmonycvi.gateway.dto.PresignUploadResponse;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;

@RestController
@RequestMapping("/api/s3")
public class S3PresignController {

	private static final Logger LOG = LoggerFactory
			.getLogger(S3PresignController.class);
//<<<<<<< HEAD
    
    @Value("${report.to-s3}")
    private String reportToS3;

    @Value("${report.bucket-name}")
    private String bucketName;
    
    private CommonMethod commonMethod;
    
    private StoragePresignService storageService;

    public S3PresignController(StoragePresignService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/pdf/upload-url")
    public ResponseEntity<Map<String, String>> getUploadUrl(
            @RequestBody Map<String, String> req) {

        LOG.info("Entered /pdf/upload-url with payload: {}", req);

        String orgId = req.get("orgId");
        String userId = req.get("userId");
        String studyId = req.get("studyId");

        LOG.info("Extracted orgId={}, userId={}, studyId={}", orgId, userId, studyId);

        String key = orgId + "/" + userId + "/" + studyId + "/preview.pdf";
        LOG.info("Generated S3 object key: {}", key);

        PresignUploadResponse response =
                storageService.presignUpload(
                        bucketName,
                        key,
                        "application/pdf",
                        10
                );

        LOG.info("Generated uploadUrl: {}", response.getUploadUrl());

        Map<String, String> res = Map.of(
                "uploadUrl", response.getUploadUrl(),
                "objectKey", key
        );

        LOG.info("Returning response: {}", res);
        return ResponseEntity.ok(res);
    }
    
    private String generateFileName(String orgId, String userId, String studyId) {

        LOG.info("Generating filename for orgId={}, userId={}, studyId={}",
                orgId, userId, studyId);

        String fileName = String.join(
                "/",
                orgId,
                userId,
                studyId,
                "preview.pdf"
        );

        LOG.info("Generated fileName: {}", fileName);
        return fileName;
    }
    
    @PostMapping(
            value = "/pdf/upload-local",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Object> uploadPdfLocal(
            @RequestParam("file") MultipartFile file,
            @RequestParam String orgId,
            @RequestParam String userId,
            @RequestParam String studyId) {

        LOG.info("Entered /pdf/upload-local");
        LOG.info("Received file: {}, size={} bytes",
                file.getOriginalFilename(), file.getSize());

        try {
            byte[] pdfBytes = file.getBytes();
            Object result = uploadToLocalStorage(pdfBytes, orgId, userId, studyId);
            LOG.info("Local PDF upload successful");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            LOG.error("Failed to upload PDF locally", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private byte[] uploadToLocalStorage(
            byte[] pdfData,
            String orgId,
            String userId,
            String studyId) {

        LOG.info("Uploading to local storage for orgId={}, userId={}, studyId={}",
                orgId, userId, studyId);

        String fileName = generateFileName(orgId, userId, studyId);

        String targetPath = commonMethod.getTargetPath() + fileName;
        LOG.info("Resolved targetPath: {}", targetPath);

        File targetFile = new File(targetPath);
        File parentDirectory = targetFile.getParentFile();

        if (parentDirectory != null && !parentDirectory.exists()) {
            LOG.info("Creating parent directories: {}", parentDirectory.getAbsolutePath());
            parentDirectory.mkdirs();
        }

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(pdfData);
            LOG.info("PDF successfully stored at {}", targetFile.getAbsolutePath());
        } catch (Exception e) {
            LOG.error("Exception while writing file to local storage", e);
        }

        return pdfData;
    }
    
    @CrossOrigin(origins = {"*"})
    @PostMapping("/presign-html-upload")
    public ResponseEntity<Map<String, String>> presignHtmlUpload(
            @RequestParam String orgId,
            @RequestParam String userId,
            @RequestParam String studyId) {

        LOG.info("Entered /presign-html-upload with orgId={}, userId={}, studyId={}",
                orgId, userId, studyId);

        String key = orgId + "/" + userId + "/" + studyId + "/report.html";
        LOG.info("Generated HTML S3 key: {}", key);

        boolean reporttoS3 = Boolean.parseBoolean(this.reportToS3);
        LOG.info("reportToS3 flag value: {}", reporttoS3);

        Map<String, String> res = new HashMap<>();
        res.put("report_to_s3", String.valueOf(reporttoS3));

        if (!reporttoS3) {

            String localPath = "file:///C:/weasis-cache/reports/" + studyId + "/report.html";
            LOG.info("S3 disabled. Returning local path: {}", localPath);

            res.put("htmlLocation", localPath);
            LOG.info("Response payload: {}", res);

            return ResponseEntity.ok(res);
        }

        PresignUploadResponse presigned =
                storageService.presignUpload(
                        bucketName,
                        key,
                        "text/html",
                        60
                );

        LOG.info("Generated presigned uploadUrl: {}", presigned.getUploadUrl());
        LOG.info("Generated objectUrl: {}", presigned.getObjectUrl());

        res.put("uploadUrl", presigned.getUploadUrl());
        res.put("htmlLocation", presigned.getObjectUrl());

        LOG.info("Final response payload: {}", res);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/pdf/view-url")
    public ResponseEntity<Map<String, String>> getPdfViewUrl(
            @RequestBody Map<String, String> req) {

        LOG.info("Entered /pdf/view-url with payload: {}", req);

        String objectKey = req.get("objectKey");
        LOG.info("Generating presigned download URL for objectKey={}", objectKey);

        String viewUrl =
                storageService.presignDownload(
                        bucketName,
                        objectKey,
                        5
                );

        LOG.info("Generated viewUrl: {}", viewUrl);

        return ResponseEntity.ok(
                Map.of("viewUrl", viewUrl)
        );
    }
}