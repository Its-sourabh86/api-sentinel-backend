package com.apisentinel.probe;

import com.apisentinel.monitor.enums.HttpMethod;

public record ProbeRequest(String url, HttpMethod method, int timeoutMs) {
}