package com.jettech.api.solutions_clinic.audit;

import com.jettech.api.solutions_clinic.model.usecase.medicalrecord.MedicalRecordResponse;
import com.jettech.api.solutions_clinic.model.usecase.patient.PatientResponse;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Intercepta chamadas de leitura nos use-cases de MedicalRecord e Patient,
 * registrando cada acesso bem-sucedido em {@code audit_access_logs}.
 *
 * Pointcuts nos use-cases (não nos controllers) garantem que somente
 * chamadas que retornaram dados reais sejam auditadas.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAccessAspect {

    private final AuditAccessLogRepository auditAccessLogRepository;
    private final TenantContext tenantContext;

    @AfterReturning(
            pointcut = "execution(* com.jettech.api.solutions_clinic.model.usecase.medicalrecord.GetMedicalRecordByIdUseCase.execute(..))",
            returning = "result"
    )
    public void logMedicalRecordRead(Object result) {
        if (!(result instanceof MedicalRecordResponse response)) {
            return;
        }
        try {
            AuditAccessLog log = AuditAccessLog.builder()
                    .tenantId(tenantContext.getClinicIdOrNull())
                    .userId(tenantContext.getUserIdOrNull())
                    .userEmail(resolveUserEmail())
                    .action("READ_MEDICAL_RECORD")
                    .entityType("MedicalRecord")
                    .entityId(response.id())
                    .patientId(response.patientId())
                    .ipAddress(resolveIpAddress())
                    .build();
            auditAccessLogRepository.save(log);
        } catch (Exception e) {
            // Log de auditoria nunca deve quebrar a operação principal
            log.warn("Falha ao registrar acesso a prontuário {}: {}", response.id(), e.getMessage());
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.jettech.api.solutions_clinic.model.usecase.patient.GetPatientByIdUseCase.execute(..))",
            returning = "result"
    )
    public void logPatientRead(Object result) {
        if (!(result instanceof PatientResponse response)) {
            return;
        }
        try {
            AuditAccessLog auditLog = AuditAccessLog.builder()
                    .tenantId(tenantContext.getClinicIdOrNull())
                    .userId(tenantContext.getUserIdOrNull())
                    .userEmail(resolveUserEmail())
                    .action("READ_PATIENT")
                    .entityType("Patient")
                    .entityId(response.id())
                    .patientId(response.id())
                    .ipAddress(resolveIpAddress())
                    .build();
            auditAccessLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("Falha ao registrar acesso a paciente {}: {}", response.id(), e.getMessage());
        }
    }

    private String resolveUserEmail() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                // Tenta claim "email" primeiro, depois "sub"
                String email = jwt.getClaimAsString("email");
                return email != null ? email : jwt.getSubject();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String resolveIpAddress() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getRemoteAddr();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
