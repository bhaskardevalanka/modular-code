package com.techvedika.harmonycvi.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.dto.StudyDTO;
import com.techvedika.harmonycvi.gateway.pacsproxy.PacsProxySeriesService;

@RestController
@RequestMapping("/pacs/series")
public class SeriesPacsController {
	private static final Logger LOG = LoggerFactory.getLogger(CenterController.class);

	@Autowired
	private PacsProxySeriesService pacsProxyService;
	
	// Get series by Study UID
	@GetMapping("/study/{studyUID}")
	public StudyDTO getSeriesByStudyUID(@PathVariable String studyUID) {
		return pacsProxyService.fetchSeriesByStudyUID(studyUID);
	}

	// Add more series-related methods as needed

}
