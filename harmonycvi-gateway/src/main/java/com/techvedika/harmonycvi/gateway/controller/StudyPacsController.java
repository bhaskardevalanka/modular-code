package com.techvedika.harmonycvi.gateway.controller;

import java.util.List;

//import org.apache.http.HttpStatus;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.dto.PatientUpdateDTO;
import com.techvedika.harmonycvi.gateway.pacsproxy.DeleteStudyResponse;
import com.techvedika.harmonycvi.gateway.pacsproxy.PacsProxyStudyService;
import com.techvedika.harmonycvi.gateway.util.ApiException;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/pacs/study")
public class StudyPacsController {
	
	@Autowired
	private UserUtils userUtils;
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyPacsController.class);
	
	@Autowired
	private PacsProxyStudyService pacsProxyService;
	
	// Fetch a study by UID
    @GetMapping("/studies/{studyUID}")
    public JSONObject getStudyByStudyUID(@PathVariable String studyUID) {
        return pacsProxyService.fetchStudyByUID(studyUID);
    }

    // Get study metadata
    @GetMapping("/{studyUID}/metadata")
    public JSONObject getStudyMetadata(@PathVariable String studyUID) {
        return pacsProxyService.fetchStudyMetadata(studyUID);
    }
    
    @PostMapping(value = "/studyupload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> studyupload(@RequestPart("file") MultipartFile file,  @RequestPart(value = "patientData", required = false) PatientUpdateDTO patientDto) {
    	return pacsProxyService.uploadDicom(file,patientDto)
            .onErrorResume(e ->
                Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body("Failed to store DICOM: " + e.getMessage())));
    }



    @PostMapping(value = "/bulk-upload" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> uploadBulkDicom(@RequestParam("files") List<MultipartFile> files) {
		try {
			ResponseEntity<String> response = pacsProxyService.uploadBulkDicom(files);
			return response;
		} catch (RuntimeException e) {
			return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Failed to store DICOMs: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
		}
	}
    
    @PostMapping("/studies/{studyInstanceUID}/reject")
    public ResponseEntity<String> rejectStudy(@PathVariable String studyInstanceUID) {
        try {
        	System.out.println("studyInstanceUID----------------"+studyInstanceUID);
        	pacsProxyService.rejectStudy(studyInstanceUID);
            return ResponseEntity.ok("Study rejected successfully.");
        } catch (RuntimeException e) {
        	return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Failed to reject study: " + e.getMessage());
        }
    }
    
    @DeleteMapping("delete/{studyInstanceUID}")
    public ResponseEntity<?> deleteStudy(@PathVariable String studyInstanceUID) {
    	JSONObject response = pacsProxyService.deleteStudy(studyInstanceUID);
    	return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("deleteStudyExtension/{studyInstanceUID}")
    public ResponseEntity<?> deleteStudyExtension(@PathVariable String studyInstanceUID) {
    	JSONObject response = pacsProxyService.deleteStudyExtension(studyInstanceUID);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{studyUID}/series/{seriesUID}/instance/{objectUID}/download")
    public ResponseEntity<byte[]> downloadDicomFile(@PathVariable String studyUID,@PathVariable String seriesUID,@PathVariable String objectUID) {

    	ResponseEntity<byte[]> dicomData = pacsProxyService.downloadDicom(studyUID, seriesUID, objectUID);
    	
    	byte[] dicomDataByte = dicomData.getBody();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/dicom"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(objectUID + ".dcm")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(dicomDataByte);
    }
    
    @PostMapping("/getKeycloakToken")
    public ResponseEntity<?> getToken(@RequestBody JSONObject json) throws Exception {
        try {
        	JSONObject response = pacsProxyService.getToken(new JSONObject(json));
            return ResponseEntity.ok(response);
        } catch (ApiException ae) {
			LOG.error("ApiException:", ae);
			return ResponseEntity 
					.status(ae.getStatus()).body(ae.getBody());

		}
    }
}
