package com.apisentinel.probe;

import com.apisentinel.monitor.entity.Monitor;
import com.apisentinel.monitor.repository.MonitorAssertionRepository;
import com.apisentinel.monitor.repository.MonitorRepository;
import com.apisentinel.result.enums.TriggerType;
import com.apisentinel.result.service.CheckResultService;
import com.apisentinel.validation.AssertionSpec;
import com.apisentinel.validation.CheckStatus;
import com.apisentinel.validation.ResponseValidator;
import com.apisentinel.validation.ValidationReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ProbeService {

    private static final Logger log = LoggerFactory.getLogger(ProbeService.class);

    private final MonitorRepository monitorRepository;
    private final MonitorAssertionRepository assertionRepository;
    private final HttpProbeExecutor probeExecutor;
    private final ResponseValidator responseValidator;
    private final CheckResultService checkResultService;

    public ProbeService(MonitorRepository monitorRepository,
                        MonitorAssertionRepository assertionRepository,
                        HttpProbeExecutor probeExecutor,
                        ResponseValidator responseValidator,
                        CheckResultService checkResultService) {
        this.monitorRepository = monitorRepository;
        this.assertionRepository = assertionRepository;
        this.probeExecutor = probeExecutor;
        this.responseValidator = responseValidator;
        this.checkResultService = checkResultService;
    }

    public ProbeRunResponse runNow(Long monitorId) {
        return execute(monitorId, TriggerType.MANUAL);
    }

    // Jaan-boojh kar @Transactional nahi: HTTP call ke dauran DB connection khula nahi rakhna
    public ProbeRunResponse execute(Long monitorId, TriggerType trigger) {
        MDC.put("monitorId", String.valueOf(monitorId));
        try {
            Monitor monitor = monitorRepository.getByIdOrThrow(monitorId);

            List<AssertionSpec> specs = assertionRepository.findByMonitorIdOrderByIdAsc(monitorId)
                    .stream()
                    .map(a -> new AssertionSpec(a.getJsonPath(), a.getOperator(), a.getExpectedValue()))
                    .toList();

            log.debug("Starting check trigger={} assertions={}", trigger, specs.size());

            Instant startedAt = Instant.now();
            ProbeResult result = probeExecutor.execute(new ProbeRequest(
                    monitor.getUrl(), monitor.getHttpMethod(), monitor.getTimeoutMs()));

            ValidationReport report =
                    responseValidator.validate(result, monitor.getExpectedStatus(),monitor.getResponseSchema(), specs);

            checkResultService.record(monitorId, startedAt, result, report, trigger);

            if (report.status() == CheckStatus.PASSED) {
                log.info("Check PASSED httpStatus={} durationMs={} trigger={}",
                        result.httpStatus(), result.durationMs(), trigger);
            } else {
                log.warn("Check FAILED code={} httpStatus={} durationMs={} trigger={} reason={}",
                        report.failureCode(), result.httpStatus(), result.durationMs(),
                        trigger, report.reason());
            }

            return new ProbeRunResponse(monitor.getId(), report.status(), report.failureCode(),
                    report.reason(), result.outcome(), result.httpStatus(),
                    result.durationMs(), report.assertions());
        } finally {
            MDC.remove("monitorId");
        }
    }
}