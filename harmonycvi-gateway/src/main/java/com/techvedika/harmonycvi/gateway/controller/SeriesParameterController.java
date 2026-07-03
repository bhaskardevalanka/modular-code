package com.techvedika.harmonycvi.gateway.controller;

import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.dto.SaveGLSSeriesParamsDto;
import com.techvedika.harmonycvi.gateway.dto.SaveSeriesParamsDto;
import com.techvedika.harmonycvi.gateway.service.SeriesParameterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/seriesParameter")
public class SeriesParameterController {
	private static final Logger LOG = LoggerFactory.getLogger(CenterController.class);
	
	 private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SeriesParameterService seriesParameterService;

    @PostMapping(value = "/getSeriesParam", consumes = "application/json", produces = "application/json")
    public JSONObject getSeriesParameter(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".getSeriesParameter");
        JSONObject response = seriesParameterService.getParameter(json);
        System.out.println("End of " + this.getClass().getName() + ".getSeriesParameter");
        return response;
    }

    @Operation(
            summary = "Save series parameters",
            description = "This endpoint allows to save the series parameters",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Series parameters saved successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
    @PostMapping(value = "/saveSeriesParam", consumes = "application/json", produces = "application/json")
    public JSONObject saveSeriesParameter(@RequestBody SaveSeriesParamsDto saveSeriesParamsDto) {
        System.out.println("Start of " + this.getClass().getName() + ".saveSeriesParameter");
        Map<String, Object> jsonMap = objectMapper.convertValue(saveSeriesParamsDto, Map.class);
 		JSONObject json = new JSONObject(jsonMap);
        JSONObject response = seriesParameterService.saveParameter(json);
        System.out.println("End of " + this.getClass().getName() + ".saveSeriesParameter");
        return response;
    }

    @GetMapping(value = "/test", produces = "application/json")
    public JSONObject test() {
        System.out.println("Start of " + this.getClass().getName() + ".test");
        JSONObject response = new JSONObject();
        System.out.println("End of " + this.getClass().getName() + ".test");
        return response;
    }

    @Operation(
            summary = "Save GLS series parameters",
            description = "This endpoint allows to save the GLS series parameters",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "GLS series parameters saved successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
    @PostMapping(value = "/saveGLSSeriesParam", consumes = "application/json", produces = "application/json")
    public JSONObject saveGLSSeriesParameter(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".saveGLSSeriesParameter");
//        Map<String, Object> jsonMap = objectMapper.convertValue(saveGLSSeriesParamsDto, Map.class);
// 		JSONObject json = new JSONObject(jsonMap);
        JSONObject response = seriesParameterService.saveGLSSeriesParameter(json);
        System.out.println("End of " + this.getClass().getName() + ".saveGLSSeriesParameter");
        return response;
    }

    @PostMapping(value = "/getGLSSeriesParam", consumes = "application/json", produces = "application/json")
    public JSONObject getGLSSeriesParameter(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".getGLSSeriesParameter");
        JSONObject response = seriesParameterService.getGLSParameter(json);
        System.out.println("End of " + this.getClass().getName() + ".getGLSSeriesParameter");
        return response;
    }

    @PostMapping(value = "/saveDESeriesParam", consumes = "application/json", produces = "application/json")
    public JSONObject saveDESeriesParam(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".saveDESeriesParam");
        JSONObject response = seriesParameterService.saveDESeriesParam(json);
        System.out.println("End of " + this.getClass().getName() + ".saveDESeriesParam");
        return response;
    }

    @PostMapping(value = "/saveMultipleSeriesParams", consumes = "application/json", produces = "application/json")
    public JSONObject saveMultipleSeriesParams(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".saveMultipleSeriesParams");
        JSONObject response = seriesParameterService.saveMultipleSeriesParameters(json);
        System.out.println("End of " + this.getClass().getName() + ".saveMultipleSeriesParams");
        return response;
    }

    @PostMapping(value = "/getECVMapDetails", consumes = "application/json", produces = "application/json")
    public JSONObject getECVMapDetails(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".getECVMapDetails");
        JSONObject response = seriesParameterService.getECVMapDetails(json);
        System.out.println("End of " + this.getClass().getName() + ".getECVMapDetails");
        return response;
    }
}
