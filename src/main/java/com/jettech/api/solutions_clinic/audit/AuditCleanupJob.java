package com.jettech.api.solutions_clinic.audit;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AuditCleanupJob {

    private final AuditAccessLogRepository accessLogRepository;

    @Scheduled(cron = "0 0 2 * * SUN")
    @Transactional
    public void purgeOldAccessLogs() {
        Instant cutoff = Instant.now().minus(365, ChronoUnit.DAYS);
        accessLogRepository.deleteByAccessedAtBefore(cutoff);
        // Tabelas *_AUD são dados clínicos — não deletar (retenção mínima 20 anos, CFM 1.821/07)
    }
}
