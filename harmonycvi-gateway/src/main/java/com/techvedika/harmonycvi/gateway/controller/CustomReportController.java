
package com.techvedika.harmonycvi.gateway.controller;

import com.techvedika.harmonycvi.gateway.service.CustomReportService;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/customreport")  
public class CustomReportController {

  private CustomReportService customReportService;
  
  public CustomReportController(CustomReportService customReportService) {
	  this.customReportService = customReportService;
  }

  private static final Logger LOG = LoggerFactory
			.getLogger(CustomReportController.class);
  
  @PostMapping(value = "/getReport", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public JSONObject getReport(@RequestBody JSONObject request) { 
      JSONObject response = new JSONObject();
      LOG.info("Service hit getReport");
      try {
          response = customReportService.getCustomReport(request);  
      } catch (Exception e) {  
    	  LOG.error("Exception in reading request");
          e.printStackTrace();
      }
      LOG.info("End of getReport"); 
      return response;
  }  
  
}