package com.jettech.api.solutions_clinic.observability;

import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UseCaseLoggingAspect {

    private final TenantContext tenantContext;

    @Around("execution(* com.jettech.api.solutions_clinic.model.usecase..Default*.execute(..))")
    public Object logExecution(ProceedingJoinPoint pjp) throws Throwable {
        String useCase = pjp.getTarget().getClass().getSimpleName();
        UUID tenantId = tenantContext.getClinicIdOrNull();
        UUID userId = tenantContext.getUserIdOrNull();
        long start = System.currentTimeMillis();

        log.info("[USE_CASE] START {} | tenantId={} | userId={}", useCase, tenantId, userId);

        try {
            Object result = pjp.proceed();
            log.info("[USE_CASE] SUCCESS {} | tenantId={} | userId={} | duration={}ms",
                    useCase, tenantId, userId, System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.error("[USE_CASE] ERROR {} | tenantId={} | userId={} | duration={}ms | exception={}",
                    useCase, tenantId, userId, System.currentTimeMillis() - start, e.getClass().getSimpleName(), e);
            throw e;
        }
    }
}
