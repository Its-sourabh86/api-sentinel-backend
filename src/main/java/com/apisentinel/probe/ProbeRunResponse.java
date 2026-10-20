package com.apisentinel.probe;

import com.apisentinel.validation.AssertionResult;
import com.apisentinel.validation.CheckStatus;
import com.apisentinel.validation.FailureCode;

import java.util.List;

public record ProbeRunResponse(
        Long monitorId,
        CheckStatus status,
        FailureCode failureCode,
        String reason,
        ProbeOutcome outcome,
        Integer httpStatus,
        long durationMs,
        List<AssertionResult> assertions
) {
}