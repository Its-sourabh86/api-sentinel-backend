package com.apisentinel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.apisentinel.dto.response.IncidentResponse;
import com.apisentinel.dto.response.PageResponse;
import com.apisentinel.enums.IncidentStatus;
import com.apisentinel.service.IncidentService;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<IncidentResponse>> list(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) Long monitorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(incidentService.list(status, monitorId, page, size));
    }
}
