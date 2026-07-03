package com.techvedika.harmonycvi.gateway.service;

import java.io.IOException;


import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.techvedika.harmonycvi.gateway.dto.SaveAIOrgTagsDto;


public interface StudyParameterService {
	
	ResponseEntity<JSONObject> getParameter(JSONObject jsonRequest);

	ResponseEntity<JSONObject> saveParameter(JSONObject json);

	ResponseEntity<JSONObject> updateIsAiProccessed(JSONObject json);

	ResponseEntity<JSONObject> proccesseAI(String studyUID,String orgId,String isAll,String authorization);

	JSONObject mapStudyUser(String orgId, String userId, String studyUID);

	JSONObject saveSummary(JSONObject json);

	JSONObject saveAnnotation(JSONObject json);

	JSONObject deleteAnnotation(JSONObject json);

	JSONObject saveClassification(JSONObject json);

	JSONObject getClassification(JSONObject json);

	JSONObject getStudyList(JSONObject json);

	ResponseEntity<JSONObject> updateStatus(JSONObject json);

	ResponseEntity<JSONObject> updateStudyPatientInfo(JSONObject json);

	ResponseEntity<JSONObject> saveStudyVolumeInfo(JSONObject json);

	ResponseEntity<JSONObject> updateClassification(JSONObject json);

	ResponseEntity<JSONObject> saveRadialStrain(JSONObject json);

	JSONObject saveAIProcessStatus(JSONObject json);

	JSONObject getAIProcessStatus(JSONObject json);

	ResponseEntity<JSONObject> updateTags(JSONObject json);

	ResponseEntity<JSONObject> getEndVolumeInfo(JSONObject json);

	JSONObject upload(MultipartFile[] input, String studyId);

	JSONObject getClinicalDetailsFiles(JSONObject json);

	JSONObject saveClinicalDetailsComments(JSONObject json);

	JSONObject getClinicalDetailsComments(JSONObject json);

	JSONObject deleteClinicalDetailFile(JSONObject json);

	JSONObject deleteClinicalDetailComment(JSONObject json);

	ResponseEntity<JSONObject> aiSaveOrgTags(SaveAIOrgTagsDto json);

	ResponseEntity<JSONObject> getAIOrgTags(SaveAIOrgTagsDto json);

    ResponseEntity<JSONObject> getStudyPatientInfoForBookmark(JSONObject jsonRequest);

	JSONObject prepareResponse(String studyId, String parameterJson) throws ParseException;
	
	JSONObject getStudyImagesCount(String studyId, String orgId);

	byte[] getLocalPdf(String relativePath) throws IOException;
	
	void deleteClassification(String studyUID);
	
	ResponseEntity<JSONObject> getPatientDetails(String studyId);
	
	JSONObject updateClassificationData(JSONObject jsonRequest);
	
	JSONObject getStudyFile(JSONObject request);
	
	JSONObject getStudyExists(String studyId);
	
	public JSONObject reprocessAI(JSONObject jsonRequest);

	JSONObject saveStudy(JSONObject json);

	JSONObject processStudy(JSONObject request);

	JSONObject processStudyExt(JSONObject request);
	
	JSONObject getPreferences(String studyId, String orgId);
}
