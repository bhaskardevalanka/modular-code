package com.techvedika.harmonycvi.gateway.pacsproxy;

import org.json.simple.JSONObject;

import com.techvedika.harmonycvi.gateway.dto.StudyDTO;

public interface PacsProxySeriesService {    
	StudyDTO fetchSeriesByStudyUID(String studyUID);
    JSONObject fetchSeriesByStudyAndSeriesUID(String studyUID, String seriesUID);
    JSONObject fetchSeriesMetadata(String studyUID, String seriesUID);
}
