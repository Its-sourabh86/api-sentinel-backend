package com.apisentinel.monitor.dto.response;

import com.apisentinel.monitor.enums.AssertionOperator;
import com.apisentinel.monitor.enums.AssertionType;
import com.apisentinel.monitor.entity.MonitorAssertion;
import java.time.Instant;

public record AssertionResponse(
        Long id,
        AssertionType type,
        String path,
        AssertionOperator operator,
        String expectedValue,
        Instant createdAt
) {
    public static AssertionResponse from(MonitorAssertion a) {
        return new AssertionResponse(a.getId(), a.getAssertionType(), a.getJsonPath(),
                a.getOperator(), a.getExpectedValue(), a.getCreatedAt());
    }
}