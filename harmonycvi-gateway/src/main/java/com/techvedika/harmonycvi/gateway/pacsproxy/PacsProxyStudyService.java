package com.techvedika.harmonycvi.gateway.pacsproxy;

import java.util.List;

import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.techvedika.harmonycvi.gateway.dto.PatientUpdateDTO;
import reactor.core.publisher.Mono;

public interface PacsProxyStudyService {
	public JSONObject fetchStudyMetadata(String studyUID);
    public JSONObject fetchStudies();
    public JSONObject fetchStudyByUID(String studyUID);
    public ResponseEntity<String> uploadBulkDicom(List<MultipartFile> files);
    public void rejectStudy(String studyInstanceUID);
    public JSONObject deleteStudy(String studyInstanceUID);
    public JSONObject deleteStudyExtension(String studyInstanceUID);
    public ResponseEntity<byte[]> downloadDicom(String studyUID, String seriesUID, String objectUID);
	public JSONObject getToken(JSONObject json) throws Exception;
	public Mono<ResponseEntity<String>> uploadDicom(MultipartFile file,PatientUpdateDTO patientDto);
}
