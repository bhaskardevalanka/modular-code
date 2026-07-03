package com.techvedika.harmonycvi.gateway.controller;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.pacsproxy.PacsProxyInstanceService;

@RestController
@RequestMapping("/api/instances")
public class InstancePacsController {
	
	@Autowired
	private PacsProxyInstanceService pacsProxyInstanceService;
	
	// Get instance by Study UID, Series UID, and Instance UID
    @GetMapping("/study/{studyUID}/series/{seriesUID}/instance/{instanceUID}")
    public JSONObject getInstanceByUID(
            @PathVariable String studyUID,
            @PathVariable String seriesUID,
            @PathVariable String instanceUID) {
        return pacsProxyInstanceService.fetchInstanceByUID(studyUID, seriesUID, instanceUID);
    }

}
