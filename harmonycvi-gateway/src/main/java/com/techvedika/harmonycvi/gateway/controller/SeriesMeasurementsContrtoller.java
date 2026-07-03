package com.techvedika.harmonycvi.gateway.controller;

import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.dto.DeleteContoursRequest;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;
import com.techvedika.harmonycvi.gateway.service.SeriesMeasurementsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/seriesMeasurement")
public class SeriesMeasurementsContrtoller {
	private static final Logger LOG = LoggerFactory.getLogger(CenterController.class);
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
    private SeriesMeasurementsService seriesMeasurementsService;

    @Autowired
    private CommonMethod commonMethod;

    @PostMapping(value = "/get", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject getSeriesMeasurementsInfo(@RequestBody JSONObject json) {
        System.out.println("Start of getSeriesMeasurementsInfo");
        JSONObject response = seriesMeasurementsService.getSeriesMeasurementsData(json);
        System.out.println("End of getSeriesMeasurementsInfo");
        return response;
    }

    @PostMapping(value = "/saveStudyMeasurements", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject saveStudyMeasurementsInfo(@RequestBody JSONObject json) {
        System.out.println("Start of saveStudyMeasurementsInfo");
        JSONObject response = seriesMeasurementsService.saveStudyMeasurementsInfo(json);
        System.out.println("End of saveStudyMeasurementsInfo");
        return response;
    }

    @PostMapping(value = "/saveFreeHandData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject saveSeriesFreeHandMeasurementsInfo(@RequestBody JSONObject json) {
        System.out.println("Start of saveSeriesFreeHandMeasurementsInfo");
        JSONObject response = seriesMeasurementsService.saveSeriesFreeHandMeasurementsInfo(json);
        System.out.println("End of saveSeriesFreeHandMeasurementsInfo");
        return response;
    }

    @PostMapping(value = "/getSeriesInfo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject getSeriesMeasurementsInfoById(@RequestBody JSONObject json) {
        System.out.println("Start of getSeriesMeasurementsInfoById");
        JSONObject response = seriesMeasurementsService.getSeriesMeasurementsDataBySeriesId(json);
        System.out.println("End of getSeriesMeasurementsInfoById");
        return response;
    }

    @PostMapping(value = "/updateSeriesMeasurement", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject updateSeriesMeasurement(@RequestBody JSONObject json) {
        System.out.println("Start of updateSeriesMeasurement");
        JSONObject response = seriesMeasurementsService.updateSeriesMeasurement(json);
        System.out.println("End of updateSeriesMeasurement");
        return response;
    }

    @PostMapping(value = "/saveSegments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject saveSegments(@RequestBody JSONObject json) {
        System.out.println("Start of saveSegments");
        JSONObject response = seriesMeasurementsService.saveSegments(json);
        System.out.println("End of saveSegments");
        return response;
    }

    @PostMapping(value = "/getSeriesSegments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject getSeriesSegments(@RequestBody JSONObject json) {
        System.out.println("Start of getSeriesSegments");
        JSONObject response = seriesMeasurementsService.getSeriesSegments(json);
        System.out.println("End of getSeriesSegments");
        return response;
    }

    @PostMapping(value = "/updateQflowContour", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject updateQflowContour(@RequestBody JSONObject json) {
        System.out.println("Start of updateQflowContour");
        JSONObject response = seriesMeasurementsService.updateQflowContour(json);
        System.out.println("End of updateQflowContour");
        return response;
    }

    @Operation(
            summary = "Delete contours",
            description = "This endpoint allows to delete the contours",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted Contours successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
    @PostMapping(value = "/deleteContours", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject deleteContours(@RequestBody DeleteContoursRequest deleteContoursRequest) {
        System.out.println("Start of deleteContours");
        Map<String, Object> jsonMap = objectMapper.convertValue(deleteContoursRequest, Map.class);
 		JSONObject json = new JSONObject(jsonMap);
        JSONObject response = seriesMeasurementsService.deleteContours(json);
        System.out.println("End of deleteContours");
        return response;
    }

    @PostMapping(value = "/updateGLSSeriesMeasurement", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject updateGLSSeriesMeasurement(@RequestBody JSONObject json) {
        System.out.println("Start of updateGLSSeriesMeasurement");
        JSONObject response = seriesMeasurementsService.updateGLSSeriesMeasurement(json);
        System.out.println("End of updateGLSSeriesMeasurement");
        return response;
    }

    @PostMapping(value = "/updateDESeriesMeasurement", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject updateDESeriesMeasurement(@RequestBody JSONObject json) {
        System.out.println("Start of updateDESeriesMeasurement");
        JSONObject response = seriesMeasurementsService.updateDESeriesMeasurement(json);
        System.out.println("End of updateDESeriesMeasurement");
        return response;
    }

    @PostMapping(value = "/shortAxisPropagation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject shortAxisPropagation(@RequestBody JSONObject json) {
        System.out.println("Start of shortAxisPropagation");
        JSONObject response = seriesMeasurementsService.shortAxisPropagation(json);
        System.out.println("End of shortAxisPropagation");
        return response;
    }

    @PostMapping(value = "/qFlowPropagation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject qFlowPropagation(@RequestBody JSONObject json) {
        System.out.println("Start of qFlowPropagation");
        JSONObject response = seriesMeasurementsService.qFlowPropagation(json);
        System.out.println("End of qFlowPropagation");
        return response;
    }
    
    @PostMapping(value = "/glsPropagation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject glsPropagation(@RequestBody JSONObject json) {
        System.out.println("Start of glsPropagation");
        JSONObject response = seriesMeasurementsService.glsPropagation(json);
        System.out.println("End of glsPropagation");
        return response;
    }
    
    @PostMapping(value = "/atrialPropagation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject atrialPropagation(@RequestBody JSONObject json) {
        System.out.println("Start of atrialPropagation");
        JSONObject response = seriesMeasurementsService.atrialPropagation(json);
        System.out.println("End of atrialPropagation");
        return response;
    }

}
