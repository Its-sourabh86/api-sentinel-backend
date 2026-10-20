package com.apisentinel.monitor.repository;

import com.apisentinel.common.exception.ResourceNotFoundException;
import com.apisentinel.monitor.entity.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitorRepository extends JpaRepository<Monitor, Long> {

        default Monitor getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitor", id));
    }
}