package com.techvedika.harmonycvi.gateway.controller;

import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.dto.SaveAIOrgTagsDto;
import com.techvedika.harmonycvi.gateway.dto.StudyListRequest;
import com.techvedika.harmonycvi.gateway.service.StudyParameterService;
import com.techvedika.harmonycvi.gateway.service.VersionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/studyParameter")
public class StudyParameterController {
	private static final Logger LOG = LoggerFactory.getLogger(StudyParameterController.class);
	
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private StudyParameterService studyParameterService;

	@Autowired
	private VersionService versionService;

	@Autowired
	private HttpServletRequest request;
	
	@PostMapping(value = "/getStudyParam", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> getStudyParameter(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".getStudyParameter");
        ResponseEntity<JSONObject> response = studyParameterService.getParameter(json);
        System.out.println("End of " + this.getClass().getName() + ".getStudyParameter");
        return response;
    }

    @PostMapping(value = "/saveStudyParam", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> saveStudyParam(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".saveStudyParam");
        ResponseEntity<JSONObject> response = studyParameterService.saveParameter(json);
        System.out.println("End of " + this.getClass().getName() + ".saveStudyParam");
        return response;
    }

//    @Operation(
//    	    summary = "Update AI process status",
//    	    description = "This endpoint allows updating the AI process status",
//    	    security = { @SecurityRequirement(name = "jwtAuth") }
//    	)
//    	@ApiResponses(value = {
//    	    @ApiResponse(responseCode = "200", description = "AI process status updated successfully"),
//    	    @ApiResponse(responseCode = "400", description = "Validation error"),
//    	    @ApiResponse(responseCode = "404", description = "Study not found")
//    	})

    @PostMapping(value = "/updateStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> updateStatus(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".updateStatus");
//        Map<String, Object> jsonMap = objectMapper.convertValue(updateStatusDto, Map.class);
// 		JSONObject json = new JSONObject(jsonMap);
        ResponseEntity<JSONObject>response = studyParameterService.updateIsAiProccessed(json);
        System.out.println("End of " + this.getClass().getName() + ".updateStatus");
        return response;
    }

    @GetMapping(value = "/proccesseAI/{studyUID}/{orgId}/{isAll}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> proccesseAI(@PathVariable String studyUID, @PathVariable String orgId, @PathVariable String isAll) {
        System.out.println("Start of " + this.getClass().getName() + ".proccesseAI");
        System.out.println("StudyId - " + studyUID);
        System.out.println("OrgId - " + orgId);

        JSONObject versionResponse = versionService.checkVersion(request);
        if (versionResponse != null) {
        	return ResponseEntity.status(HttpStatus.OK).body(versionResponse);
        }
        ResponseEntity<JSONObject> response = studyParameterService.proccesseAI(studyUID, orgId, isAll,null);
        System.out.println("End of " + this.getClass().getName() + ".proccesseAI");
        return response;
    }
    
    @PostMapping(value = "/saveSummary", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject saveSummary(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".saveSummary");
        JSONObject response = studyParameterService.saveSummary(json);
        System.out.println("End of " + this.getClass().getName() + ".saveSummary");
        return response;
    }

    @PostMapping(value = "/saveAnnotation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject saveAnnotation(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".saveAnnotation");
        JSONObject response = studyParameterService.saveAnnotation(json);
        System.out.println("End of " + this.getClass().getName() + ".saveAnnotation");
        return response;
    }

    @PostMapping(value = "/deleteAnnotation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject deleteAnnotation(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".deleteAnnotation");
        JSONObject response = studyParameterService.deleteAnnotation(json);
        System.out.println("End of " + this.getClass().getName() + ".deleteAnnotation");
        return response;
    }

   
    @PostMapping(value = "/saveClassification", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject saveClassification(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".saveClassification");
        JSONObject response = studyParameterService.saveClassification(json);
        System.out.println("End of " + this.getClass().getName() + ".saveClassification");
        return response;
    }
    
//    @Operation(
//    	    summary = "Get Classification",
//    	    description = "This endpoint allows to get the study-level classification data",
//    	    security = { @SecurityRequirement(name = "jwtAuth") }
//    	)
//    	@ApiResponses(value = {
//    	    @ApiResponse(responseCode = "200", description = "Fetched study classification dats successfully"),
//    	    @ApiResponse(responseCode = "400", description = "Validation error")
//    	})
    @PostMapping(value = "/getClassification", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject getClassification(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".getClassification");
//        Map<String, Object> jsonMap = objectMapper.convertValue(deleteContoursRequest, Map.class);
// 		JSONObject json = new JSONObject(jsonMap);
        JSONObject response = studyParameterService.getClassification(json);
        System.out.println("End of " + this.getClass().getName() + ".getClassification");
        return response;
    }
    
    @Operation(
            summary = "Fetches study list",
            description = "This endpoint allows to fetch the study list",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "study list fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
    @PostMapping(value = "/getStudyList", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject getStudyList(@RequestBody StudyListRequest studyListRequest, @RequestHeader("Version") String version, @RequestHeader("Type") String type) {
        System.out.println("Start of " + this.getClass().getName() + ".getStudyList");

        if (version == null) {
            System.out.println("Version is missing");
            throw new IllegalArgumentException("Version is missing in headers");
        }

        JSONObject versionResponse = versionService.checkVersion(request);
        if (versionResponse != null)
            return versionResponse;
        Map<String, Object> jsonMap = objectMapper.convertValue(studyListRequest, Map.class);
		JSONObject json = new JSONObject(jsonMap);
        JSONObject response = studyParameterService.getStudyList(json);
        System.out.println("End of " + this.getClass().getName() + ".getStudyList");
        return response;
    }

    @PostMapping(value = "/updateStudyStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> updateStudyStatus(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".updateStatus");
        ResponseEntity<JSONObject> response = studyParameterService.updateStatus(json);
        System.out.println("End of " + this.getClass().getName() + ".updateStatus");
        return response;
    }

//    @Operation(
//    	    summary = "Update Study Patient Info",
//    	    description = "This endpoint allows updating patient information for a study",
//    	    security = { @SecurityRequirement(name = "jwtAuth") }
//    	)
//    	@ApiResponses(value = {
//    	    @ApiResponse(responseCode = "200", description = "Study patient info updated successfully"),
//    	    @ApiResponse(responseCode = "400", description = "Validation error")
//    	})

    @PostMapping(value = "/updateStudyPatientInfo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
//    public ResponseEntity<JSONObject> updateStudyPatientInfo(@RequestBody StudyPatientInfoDto studyPatientInfoDto ) {
    public ResponseEntity<JSONObject> updateStudyPatientInfo(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".updateStudyPatientInfo");
        JSONObject versionResponse = versionService.checkVersion(request);
        if (versionResponse != null)
            return ResponseEntity.status(HttpStatus.OK).body(versionResponse);
//        Map<String, Object> jsonMap = objectMapper.convertValue(studyPatientInfoDto, Map.class);
//		JSONObject json = new JSONObject(jsonMap);
        ResponseEntity<JSONObject> response = studyParameterService.updateStudyPatientInfo(json);
        System.out.println("End of " + this.getClass().getName() + ".updateStudyPatientInfo");
        return response;
    }

//    @Operation(
//    	    summary = "Save study volume Info",
//    	    description = "This endpoint allows to save study volume Info",
//    	    security = { @SecurityRequirement(name = "jwtAuth") }
//    	)
//    	@ApiResponses(value = {
//    	    @ApiResponse(responseCode = "200", description = "Study volume info saved successfully"),
//    	    @ApiResponse(responseCode = "400", description = "Validation error")
//    	})
    @PostMapping(value = "/saveStudyVolumeInfo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> saveStudyVolumeInfo(@RequestBody JSONObject json ) {
        System.out.println("Start of " + this.getClass().getName() + ".saveStudyVolumeInfo");
//        Map<String, Object> jsonMap = objectMapper.convertValue(saveStudyVolumeInfoDto, Map.class);
//		JSONObject json = new JSONObject(jsonMap);
        ResponseEntity<JSONObject> response = studyParameterService.saveStudyVolumeInfo(json);
        System.out.println("End of " + this.getClass().getName() + ".saveStudyVolumeInfo");
        return response;
    }

    @PostMapping(value = "/updateClassification", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> updateClassification(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".updateClassification");
        ResponseEntity<JSONObject> response = studyParameterService.updateClassification(json);
        System.out.println("End of " + this.getClass().getName() + ".updateClassification");
        return response;
    }

    @PostMapping(value = "/saveRadialStrain", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> saveRadialStrain(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".saveRadialStrain");
        ResponseEntity<JSONObject> response = studyParameterService.saveRadialStrain(json);
        System.out.println("End of " + this.getClass().getName() + ".saveRadialStrain");
        return response;
    }

    
    @PostMapping(value = "/saveAIProcessStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject saveAIProcessStatus(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".saveAIProcessStatus");
        JSONObject response = studyParameterService.saveAIProcessStatus(json);
        System.out.println("End of " + this.getClass().getName() + ".saveAIProcessStatus");
        return response;
    }

//    @Operation(
//            summary = "Get AI process status",
//            description = "This endpoint allows to get the AI process status",
//            security = { @SecurityRequirement(name = "jwtAuth") }
//        )
//        @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Fetched AI process status successfully"),
//            @ApiResponse(responseCode = "400", description = "Validation error")
//        })
    @PostMapping(value = "/getAIProcessStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject getAIProcessStatus(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".getAIProcessStatus");
//        Map<String, Object> jsonMap = objectMapper.convertValue(getAIProcessStatusDto, Map.class);
// 		JSONObject json = new JSONObject(jsonMap);
        JSONObject response = studyParameterService.getAIProcessStatus(json);
        System.out.println("End of " + this.getClass().getName() + ".getAIProcessStatus");
        return response;
    }

    @PostMapping(value = "/updateTags", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> updateTags(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".updateTags");
        ResponseEntity<JSONObject> response = studyParameterService.updateTags(json);
        System.out.println("End of " + this.getClass().getName() + ".updateTags");
        return response;
    }
    
//    @Operation(
//            summary = "Get end volume info",
//            description = "This endpoint allows to get the end volume info",
//            security = { @SecurityRequirement(name = "jwtAuth") }
//        )
//        @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Fetched end volume info successfully"),
//            @ApiResponse(responseCode = "400", description = "Validation error")
//        })
    @PostMapping(value = "/getEndVolumeInfo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> getEndVolumeInfo(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".getEndVolumeInfo");
//        Map<String, Object> jsonMap = objectMapper.convertValue(getEndVolumeInfoDto, Map.class);
// 		JSONObject json = new JSONObject(jsonMap);
        ResponseEntity<JSONObject> response = studyParameterService.getEndVolumeInfo(json);
        System.out.println("End of " + this.getClass().getName() + ".getEndVolumeInfo");
        return response;
    }

    @PostMapping(value = "/upload/{studyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject upload(@RequestParam("file") MultipartFile[] files, @PathVariable("studyId") String studyId) {
        System.out.println("Start of " + this.getClass().getName() + ".upload");
        
        for (MultipartFile file : files) {
            if (file.getSize() > 20 * 1024 * 1024) { // per file 20 MB
                throw new IllegalArgumentException("File " + file.getOriginalFilename() + " exceeds 20MB limit");
            }
        }
        
        JSONObject versionResponse = versionService.checkVersion(request);
        if (versionResponse != null) {
            return versionResponse;
        }
        JSONObject response = studyParameterService.upload(files, studyId);
        System.out.println("End of " + this.getClass().getName() + ".upload");
        return response;
    }

    @PostMapping(value = "/getClinicalDetailsFiles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject getClinicalDetailsFiles(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".getClinicalDetailsFiles");
        JSONObject versionResponse = versionService.checkVersion(request);
        if (versionResponse != null) {
            return versionResponse;
        }
        JSONObject response = studyParameterService.getClinicalDetailsFiles(json);
        System.out.println("End of " + this.getClass().getName() + ".getClinicalDetailsFiles");
        return response;
    }

    @PostMapping(value = "/saveClinicalDetailsComments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject saveClinicalDetailsComments(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".saveClinicalDetailsComments");
        JSONObject versionResponse = versionService.checkVersion(request);
        if (versionResponse != null) {
            return versionResponse;
        }
        JSONObject response = studyParameterService.saveClinicalDetailsComments(json);
        System.out.println("End of " + this.getClass().getName() + ".saveClinicalDetailsComments");
        return response;
    }

    @PostMapping(value = "/getClinicalDetailsComments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject getClinicalDetailsComments(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".getClinicalDetailsComments");
        JSONObject versionResponse = versionService.checkVersion(request);
        if (versionResponse != null) {
            return versionResponse;
        }
        JSONObject response = studyParameterService.getClinicalDetailsComments(json);
        System.out.println("End of " + this.getClass().getName() + ".getClinicalDetailsComments");
        return response;
    }

    @PostMapping(value = "/deleteClinicalDetailFile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject deleteClinicalDetailFile(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".deleteClinicalDetailFile");
        JSONObject versionResponse = versionService.checkVersion(request);
        if (versionResponse != null) {
            return versionResponse;
        }
        JSONObject response = studyParameterService.deleteClinicalDetailFile(json);
        System.out.println("End of " + this.getClass().getName() + ".deleteClinicalDetailFile");
        return response;
    }

    @PostMapping(value = "/deleteClinicalDetailComment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject deleteClinicalDetailComment(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".deleteClinicalDetailComment");
        JSONObject versionResponse = versionService.checkVersion(request);
        if (versionResponse != null) {
            return versionResponse;
        }
        JSONObject response = studyParameterService.deleteClinicalDetailComment(json);
        System.out.println("End of " + this.getClass().getName() + ".deleteClinicalDetailComment");
        return response;
    }

    @Operation(
    	    summary = "Save AI Org Tags",
    	    description = "This endpoint allows saving AI organization tags",
    	    security = { @SecurityRequirement(name = "jwtAuth") }
    	)
    	@ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "AI org tags saved successfully"),
    	    @ApiResponse(responseCode = "400", description = "Validation error")
    	})

    @PostMapping(value = "/AISaveOrgTags", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> aiSaveOrgTags(@Valid @RequestBody SaveAIOrgTagsDto json) {
        System.out.println("Start of " + this.getClass().getName() + ".aiSaveOrgTags");
        ResponseEntity<JSONObject> response = studyParameterService.aiSaveOrgTags(json);
        System.out.println("End of " + this.getClass().getName() + ".aiSaveOrgTags");
        return response;
    }

    
    @Operation(
    	    summary = "Get AI Org Tags",
    	    description = "This endpoint allows to fetch AI org tags",
    	    security = { @SecurityRequirement(name = "jwtAuth") }
    	)
    	@ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Fetched AI org tags successfully"),
    	    @ApiResponse(responseCode = "400", description = "Validation error")
    	})
    @PostMapping(value = "/getAIOrgTags", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> getAIOrgTags(@Valid @RequestBody SaveAIOrgTagsDto json ) {
        System.out.println("Start of " + this.getClass().getName() + ".getAIOrgTags");
        ResponseEntity<JSONObject> response = studyParameterService.getAIOrgTags(json);
        System.out.println("End of " + this.getClass().getName() + ".getAIOrgTags");
        return response;
    }

//    @Operation(
//    	    summary = "Get patient info for bootmark",
//    	    description = "This endpoint allows to fetch patient info",
//    	    security = { @SecurityRequirement(name = "jwtAuth") }
//    	)
//    	@ApiResponses(value = {
//    	    @ApiResponse(responseCode = "200", description = "Fetched patient info successfully"),
//    	    @ApiResponse(responseCode = "400", description = "Validation error")
//    	})
    @PostMapping(value = "/getStudyPatientInfoForBookmark", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> getStudyPatientInfoForBookmark(@RequestBody JSONObject json ) {
        System.out.println("Start of " + this.getClass().getName() + ".getStudyPatientInfoForBookmark");
//        Map<String, Object> jsonMap = objectMapper.convertValue(bookmarkRequestDto, Map.class);
// 		JSONObject json = new JSONObject(jsonMap);
        ResponseEntity<JSONObject> response = studyParameterService.getStudyPatientInfoForBookmark(json);
        System.out.println("End of " + this.getClass().getName() + ".getStudyPatientInfoForBookmark");
        return response;
    }

    @GetMapping(value = "/getStudyImagesCount/{studyId}/{orgId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject getStudyImagesCount(@PathVariable("studyId") String studyId,@PathVariable("orgId") String orgId) {
        System.out.println("Start of " + this.getClass().getName() + ".getStudyImagesCount");
        JSONObject response = studyParameterService.getStudyImagesCount(studyId,orgId);
        System.out.println("End of " + this.getClass().getName() + ".getStudyImagesCount");
        return response;
    }
    
    @GetMapping(value = "/getPreferences/{studyId}/{orgId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject getPreferences(@PathVariable("studyId") String studyId,@PathVariable("orgId") String orgId) {
        LOG.info("Start of " + this.getClass().getName() + ".getPreferences");
        JSONObject response = studyParameterService.getPreferences(studyId,orgId);
        LOG.info("End of " + this.getClass().getName() + ".getPreferences");
        return response;
    }

    @GetMapping(value = "/getPatientDetails/{studyId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> getPatientDetails(@PathVariable("studyId") String studyId) {
        System.out.println("Start of " + this.getClass().getName() + ".getPatientDetails");
        ResponseEntity<JSONObject> response = studyParameterService.getPatientDetails(studyId);
        System.out.println("End of " + this.getClass().getName() + ".getPatientDetails");
        return response;
    }

    @PostMapping(value = "/updateClassificationData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject updateClassificationData(@RequestBody JSONObject request) {
        System.out.println("Start of " + this.getClass().getName() + ".updateClassificationData");
        JSONObject response = studyParameterService.updateClassificationData(request);
        System.out.println("End of " + this.getClass().getName() + ".updateClassificationData");
        return response;
    }

    @PostMapping(value = "/getStudyFile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject getStudyFile(@RequestBody JSONObject request) {
        JSONObject response = new JSONObject();
        System.out.println("Service hit getStudyFile");
        try {
            response = studyParameterService.getStudyFile(request);
        } catch (Exception e) {
            System.out.println("Exception in reading request for getStudyFile");
            e.printStackTrace();
        }
        System.out.println("End of getStudyFile");
        return response;
    }

    @GetMapping(value = "/getStudyExists/{studyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject getStudyExists(@PathVariable("studyId") String studyId) {
        JSONObject response = new JSONObject();
        System.out.println("Service hit getStudyExists");
        try {
            response = studyParameterService.getStudyExists(studyId);
        } catch (Exception e) {
            System.out.println("Exception in reading request for getStudyExists");
            e.printStackTrace();
        }
        System.out.println("End of getStudyExists");
        return response;
    }
    
//    @Operation(
//    	    summary = "Save Study",
//    	    description = "This endpoint allows to save the study details",
//    	    security = { @SecurityRequirement(name = "jwtAuth") }
//    	)
//    	@ApiResponses(value = {
//    	    @ApiResponse(responseCode = "200", description = "Study saved successfully"),
//    	    @ApiResponse(responseCode = "400", description = "Validation error"),
//    	    @ApiResponse(responseCode = "404", description = "Study not found")
//    	})
    @PostMapping(value = "/saveStudy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject saveStudy(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".saveStudy");
//        Map<String, Object> jsonMap = objectMapper.convertValue(saveStudyDto, Map.class);
//		JSONObject json = new JSONObject(jsonMap);
        JSONObject response = studyParameterService.saveStudy(json);
        System.out.println("End of " + this.getClass().getName() + ".saveStudy");
        return response;
    }

    @PostMapping(value = "/processStudy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject processStudy(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".processStudy");
//        Map<String, Object> jsonMap = objectMapper.convertValue(saveStudyDto, Map.class);
//		JSONObject json = new JSONObject(jsonMap);
        JSONObject response = studyParameterService.processStudy(json);
        System.out.println("End of " + this.getClass().getName() + ".processStudy");
        return response;
    }

    @PostMapping(value = "/processStudyExt", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject processStudyExt(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".processStudyExt");
        JSONObject response = studyParameterService.processStudyExt(json);
        System.out.println("End of " + this.getClass().getName() + ".processStudyExt");
        return response;
    }
	
}
