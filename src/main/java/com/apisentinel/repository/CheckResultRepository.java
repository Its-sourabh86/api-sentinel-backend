package com.apisentinel.repository;
import com.apisentinel.entity.CheckResult;
import com.apisentinel.enums.TriggerType;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {

    Page<CheckResult> findByMonitorId(Long monitorId, Pageable pageable);

    List<CheckResult> findByMonitorIdOrderByStartedAtDescIdDesc(Long monitorId, Pageable pageable);

    List<CheckResult> findByMonitorIdAndTriggerTypeOrderByStartedAtDescIdDesc(
        Long monitorId, TriggerType triggerType, Pageable pageable);
}
