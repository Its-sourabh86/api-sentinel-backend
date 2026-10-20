package com.apisentinel.result.dto.response;

import com.apisentinel.probe.ProbeOutcome;
import com.apisentinel.result.entity.CheckResult;
import com.apisentinel.result.enums.TriggerType;
import com.apisentinel.validation.CheckStatus;
import com.apisentinel.validation.FailureCode;

import java.time.Instant;

public record CheckResultResponse(
        Long id,
        Instant startedAt,
        long durationMs,
        Integer httpStatus,
        CheckStatus status,
        FailureCode failureCode,
        String reason,
        ProbeOutcome outcome,
        TriggerType trigger
) {
    public static CheckResultResponse from(CheckResult r) {
        return new CheckResultResponse(r.getId(), r.getStartedAt(), r.getDurationMs(),
                r.getHttpStatus(), r.getResultStatus(), r.getFailureCode(),
                r.getFailureReason(), r.getProbeOutcome(), r.getTriggerType());
    }
}