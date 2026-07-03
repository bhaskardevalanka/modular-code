package com.techvedika.harmonycvi.gateway.controller;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.aws.S3UploadService;

@RestController
@RequestMapping("/studyUpload")
public class UploadController {
	private static final Logger LOG = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private S3UploadService s3Service;

    @GetMapping("/generatePresignedUrl")
    public ResponseEntity<JSONObject> generatePresignedUrl(
            @RequestParam("directoryName") String directoryName,
            @RequestParam("fileName") String fileName,
            @RequestParam("totalParts") Integer totalParts,
            @RequestParam("userId") Long userId,
            @RequestParam("orgId") Long orgId) {

        JSONObject response = s3Service.generatePreSignedUrl(directoryName, fileName, totalParts, userId, orgId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateS3CompletedStatus")
    public ResponseEntity<JSONObject> uploadingToS3Completed(@RequestBody JSONObject request) {
        JSONObject response = s3Service.uploadingtoS3Completed(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateTransferStatus/{id}/{study_id}/{is_transferred}")
    public ResponseEntity<JSONObject> updateTransferStatus(
            @PathVariable("id") Long id,
            @PathVariable("study_id") String studyId,
            @PathVariable("is_transferred") boolean isTransferred) {

        JSONObject response = s3Service.updateTransferStatus(id, studyId, isTransferred);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/updateConfStudies/{study_id}/{user_id}/{org_id}")
    public ResponseEntity<JSONObject> updateConfStudies(
            @PathVariable("study_id") String studyId,
            @PathVariable("user_id") Long userId,
            @PathVariable("org_id") Long orgId) {

        JSONObject response = s3Service.updateConfStudies(studyId, userId, orgId);
        return ResponseEntity.ok(response);
    }
    
}
