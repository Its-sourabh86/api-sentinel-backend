package com.apisentinel.validation;

import com.apisentinel.monitor.enums.AssertionOperator;

public record AssertionSpec(String path, AssertionOperator operator, String expectedValue) {
}