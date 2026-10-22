package com.apisentinel.utils;


import com.apisentinel.dto.request.ProbeRequest;
import com.apisentinel.enums.ProbeOutcome;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class HttpProbeExecutor {

    private static final int MAX_BODY_BYTES = 1_048_576; // 1 MB

    private final HttpClient httpClient;

    public HttpProbeExecutor(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public ProbeResult execute(ProbeRequest request) {
        long start = System.nanoTime();

        HttpRequest httpRequest;
        try {
            httpRequest = buildRequest(request);
        } catch (IllegalArgumentException e) {
            return ProbeResult.failure(ProbeOutcome.INVALID_URL, elapsedMs(start),
                    "Invalid URL: " + e.getMessage());
        }

        try {
            HttpResponse<InputStream> response =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream in = response.body()) {
                byte[] bytes = in.readNBytes(MAX_BODY_BYTES + 1);
                long duration = elapsedMs(start);

                if (bytes.length > MAX_BODY_BYTES) {
                    return ProbeResult.failure(ProbeOutcome.RESPONSE_TOO_LARGE, duration,
                            "Response body exceeds " + MAX_BODY_BYTES + " bytes");
                }
                String body = new String(bytes, StandardCharsets.UTF_8);
                return ProbeResult.responseReceived(response.statusCode(), duration, body);
            }

        } catch (HttpTimeoutException e) {
            return ProbeResult.failure(ProbeOutcome.TIMEOUT, elapsedMs(start),
                    "No response within " + request.timeoutMs() + " ms");
        } catch (ConnectException e) {
            return ProbeResult.failure(ProbeOutcome.CONNECTION_FAILED, elapsedMs(start),
                    "Could not connect to target");
        } catch (IOException e) {
            return ProbeResult.failure(ProbeOutcome.ERROR, elapsedMs(start),
                    "I/O error: " + e.getClass().getSimpleName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // interrupt flag wapas set karo
            return ProbeResult.failure(ProbeOutcome.ERROR, elapsedMs(start), "Probe interrupted");
        }
    }

    private HttpRequest buildRequest(ProbeRequest request) {
        URI uri = URI.create(request.url());
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("only http and https are allowed");
        }
        if (uri.getHost() == null) {
            throw new IllegalArgumentException("host is missing");
        }

        return HttpRequest.newBuilder(uri)
                .timeout(Duration.ofMillis(request.timeoutMs()))
                .header("User-Agent", "API-Sentinel/1.0")
                .method(request.method().name(), HttpRequest.BodyPublishers.noBody())
                .build();
    }

    private long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
