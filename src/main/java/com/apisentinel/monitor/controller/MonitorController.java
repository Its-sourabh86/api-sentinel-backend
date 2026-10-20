package com.apisentinel.monitor.controller;

import com.apisentinel.monitor.dto.request.CreateMonitorRequest;
import com.apisentinel.monitor.dto.request.UpdateMonitorRequest;
import com.apisentinel.monitor.dto.response.MonitorResponse;
import com.apisentinel.monitor.service.MonitorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/monitors")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @PostMapping
    public ResponseEntity<MonitorResponse> create(@Valid @RequestBody CreateMonitorRequest request) {
        MonitorResponse created = monitorService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonitorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(monitorService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<MonitorResponse>> getAll() {
        return ResponseEntity.ok(monitorService.getAll());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MonitorResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateMonitorRequest request) {
        return ResponseEntity.ok(monitorService.update(id, request));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<MonitorResponse> pause(@PathVariable Long id) {
        return ResponseEntity.ok(monitorService.pause(id));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<MonitorResponse> resume(@PathVariable Long id) {
        return ResponseEntity.ok(monitorService.resume(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        monitorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}