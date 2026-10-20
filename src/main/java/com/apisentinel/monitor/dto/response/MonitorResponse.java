package com.apisentinel.monitor.dto.response;


import com.apisentinel.monitor.entity.Monitor;
import com.apisentinel.monitor.enums.HttpMethod;

import java.time.Instant;

public record MonitorResponse(
        Long id,
        String name,
        String url,
        HttpMethod method,
        Integer intervalSeconds,
        Integer timeoutMs,
        Integer expectedStatus,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    public static MonitorResponse from(Monitor m) {
        return new MonitorResponse(
                m.getId(),
                m.getName(),
                m.getUrl(),
                m.getHttpMethod(),
                m.getIntervalSeconds(),
                m.getTimeoutMs(),
                m.getExpectedStatus(),
                m.isEnabled(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }
}