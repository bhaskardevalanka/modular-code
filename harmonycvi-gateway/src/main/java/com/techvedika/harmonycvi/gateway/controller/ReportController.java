package com.techvedika.harmonycvi.gateway.controller;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.service.StudyParameterService;
 
import jakarta.servlet.http.HttpServletRequest;
 
@RestController
@RequestMapping("/report")
public class ReportController {
	
	private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);
	
	private StudyParameterService studyParameterService;
	
	@Value("#{environment['server.gateway-url'] + '/report/'}")
	String reportRetrivalPath;

	@Value("#{environment['app.data-dir'] + '/report/'}")
	String reportStoragePath;
	
	public ReportController(StudyParameterService studyParameterService) {
		this.studyParameterService = studyParameterService;
	}
	
    @GetMapping("/**")
    public ResponseEntity<byte[]> getLocalReport(HttpServletRequest request) {
        try {
        	String requestURI = request.getRequestURI();
        	int index = requestURI.indexOf("/report/");
        	String relativePath = requestURI.substring(index + "/report/".length());

        	Path fullPath = Paths.get(reportStoragePath).resolve(relativePath);
        	File reportFile = fullPath.toFile();

        	LOG.info("Resolved path: {}", reportFile.getAbsolutePath());
            
            LOG.info("fullPath::{}",fullPath);
            // Read file bytes
            byte[] fileData = studyParameterService.getLocalPdf(relativePath);
 
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename(relativePath).build());
 
            LOG.info("End of getLocalReport");
            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
 
        } catch (Exception e) {
        	LOG.error("Internal server error:{}",e.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
 
}