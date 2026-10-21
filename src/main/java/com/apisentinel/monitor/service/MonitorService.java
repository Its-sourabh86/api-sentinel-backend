package com.apisentinel.monitor.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.apisentinel.common.exception.ResourceNotFoundException;
import com.apisentinel.monitor.dto.request.CreateMonitorRequest;
import com.apisentinel.monitor.dto.request.UpdateMonitorRequest;
import com.apisentinel.monitor.dto.response.MonitorResponse;
import com.apisentinel.monitor.entity.Monitor;
import com.apisentinel.monitor.enums.HttpMethod;
import com.apisentinel.monitor.repository.MonitorRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class MonitorService {

    private final MonitorRepository monitorRepository;

    public MonitorService(MonitorRepository monitorRepository) {
        this.monitorRepository = monitorRepository;
    }

    @Transactional
    public MonitorResponse create(CreateMonitorRequest request) {
        HttpMethod method = request.method() != null ? request.method() : HttpMethod.GET;
        Integer expectedStatus = request.expectedStatus() != null ? request.expectedStatus() : 200;

        Monitor monitor = new Monitor(
                request.name(),
                request.url(),
                method,
                request.intervalSeconds(),
                request.timeoutMs(),
                expectedStatus

        );

        Monitor saved = monitorRepository.save(monitor);
        return MonitorResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public MonitorResponse getById(Long id) {
        Monitor monitor = monitorRepository.getByIdOrThrow(id);
        return MonitorResponse.from(monitor);
    }

    @Transactional(readOnly = true)
    public List<MonitorResponse> getAll() {
        return monitorRepository.findAll().stream().map(MonitorResponse::from).toList();
    }

    @Transactional
    public MonitorResponse update(Long id, UpdateMonitorRequest request) {
        Monitor monitor = monitorRepository.getByIdOrThrow(id);

        if (request.name() != null)
            monitor.setName(request.name());
        if (request.url() != null)
            monitor.setUrl(request.url());
        if (request.method() != null)
            monitor.setHttpMethod(request.method());
        if (request.intervalSeconds() != null) {
            monitor.setIntervalSeconds(request.intervalSeconds());
            monitor.rescheduleNow(); // 1 ghante se 30s kiya toh ghante bhar wait na karna pade
        }
        if (request.timeoutMs() != null)
            monitor.setTimeoutMs(request.timeoutMs());
        if (request.expectedStatus() != null)
            monitor.setExpectedStatus(request.expectedStatus());

        Monitor saved = monitorRepository.save(monitor);
        return MonitorResponse.from(saved);
    }

    @Transactional
    public MonitorResponse pause(Long id) {
        Monitor monitor = monitorRepository.getByIdOrThrow(id);
        monitor.setEnabled(false);
        Monitor saved = monitorRepository.save(monitor);
        return MonitorResponse.from(saved);
    }

    @Transactional
    public MonitorResponse resume(Long id) {
        Monitor monitor = monitorRepository.getByIdOrThrow(id);
        monitor.setEnabled(true);
        monitor.rescheduleNow(); // pause ke dauran next_run_at purana ho chuka hai
        Monitor saved = monitorRepository.save(monitor);
        return MonitorResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Monitor monitor = monitorRepository.getByIdOrThrow(id);
        monitorRepository.delete(monitor);
    }
}
