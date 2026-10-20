package com.apisentinel.validation;

import java.util.List;

public record ValidationReport(
        CheckStatus status,
        FailureCode failureCode,
        String reason,
        List<AssertionResult> assertions
) {
    public static ValidationReport passed(List<AssertionResult> assertions) {
        return new ValidationReport(CheckStatus.PASSED, null, null, assertions);
    }

    public static ValidationReport failed(FailureCode code, String reason,
                                          List<AssertionResult> assertions) {
        return new ValidationReport(CheckStatus.FAILED, code, reason, assertions);
    }
}