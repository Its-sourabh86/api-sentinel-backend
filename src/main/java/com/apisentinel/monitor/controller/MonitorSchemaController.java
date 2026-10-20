package com.apisentinel.monitor.controller;

import com.apisentinel.monitor.service.SchemaService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/monitors/{monitorId}/schema")
public class MonitorSchemaController {

    private final SchemaService schemaService;

    public MonitorSchemaController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @PutMapping
    public ResponseEntity<JsonNode> set(@PathVariable Long monitorId, @RequestBody JsonNode schema) {
        return ResponseEntity.ok(schemaService.set(monitorId, schema));
    }

    @GetMapping
    public ResponseEntity<JsonNode> get(@PathVariable Long monitorId) {
        return ResponseEntity.ok(schemaService.get(monitorId));
    }

    @DeleteMapping
    public ResponseEntity<Void> remove(@PathVariable Long monitorId) {
        schemaService.remove(monitorId);
        return ResponseEntity.noContent().build();
    }
}