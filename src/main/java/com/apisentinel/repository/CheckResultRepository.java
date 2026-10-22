package com.apisentinel.repository;
import com.apisentinel.entity.CheckResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {

    Page<CheckResult> findByMonitorId(Long monitorId, Pageable pageable);
}
