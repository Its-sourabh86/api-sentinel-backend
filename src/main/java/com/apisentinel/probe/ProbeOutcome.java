package com.apisentinel.probe;

public enum ProbeOutcome {
    RESPONSE_RECEIVED,
    TIMEOUT,
    CONNECTION_FAILED,
    INVALID_URL,
    RESPONSE_TOO_LARGE,
    ERROR
}