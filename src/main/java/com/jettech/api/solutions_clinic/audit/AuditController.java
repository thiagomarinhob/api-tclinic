package com.jettech.api.solutions_clinic.audit;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints de auditoria — histórico de mutações (Envers) e log de acessos (AOP).
 *
 * Acesso restrito a usuários autenticados. O controle granular de roles é feito
 * na camada de segurança global; endpoints de access-log requerem perfil ADMIN.
 */
@RestController
@RequestMapping("/v1/audit")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AuditController {

    private final EntityManager entityManager;
    private final AuditAccessLogRepository auditAccessLogRepository;

    // =========================================================================
    // DTOs de resposta
    // =========================================================================

    /**
     * Representa uma revisão Envers — estado da entidade em um dado momento.
     *
     * @param rev         Número sequencial da revisão.
     * @param timestamp   Instante da revisão.
     * @param revType     0=ADD, 1=MOD, 2=DEL.
     * @param userEmail   E-mail do usuário que realizou a operação.
     * @param ipAddress   IP da requisição (pode ser null em jobs).
     * @param snapshot    Estado da entidade naquele ponto no tempo.
     */
    public record RevisionHistoryResponse(
            int rev,
            Instant timestamp,
            short revType,
            String userEmail,
            String ipAddress,
            Object snapshot
    ) {}

    /**
     * Representa uma entrada do log de acessos de leitura.
     */
    public record AccessLogResponse(
            UUID id,
            String userEmail,
            String action,
            String entityType,
            UUID entityId,
            String ipAddress,
            Instant accessedAt
    ) {}

    // =========================================================================
    // GET /v1/audit/patients/{id}/history
    // =========================================================================

    @GetMapping("/patients/{id}/history")
    public ResponseEntity<List<RevisionHistoryResponse>> patientHistory(
            @PathVariable UUID id
    ) throws AuthenticationFailedException {

        AuditReader reader = AuditReaderFactory.get(entityManager);

        @SuppressWarnings("unchecked")
        List<Object[]> results = reader.createQuery()
                .forRevisionsOfEntity(
                        com.jettech.api.solutions_clinic.model.entity.Patient.class,
                        false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();

        List<RevisionHistoryResponse> history = results.stream()
                .map(row -> toRevisionResponse(row))
                .toList();

        return ResponseEntity.ok(history);
    }

    // =========================================================================
    // GET /v1/audit/medical-records/{id}/history
    // =========================================================================

    @GetMapping("/medical-records/{id}/history")
    public ResponseEntity<List<RevisionHistoryResponse>> medicalRecordHistory(
            @PathVariable UUID id
    ) throws AuthenticationFailedException {

        AuditReader reader = AuditReaderFactory.get(entityManager);

        @SuppressWarnings("unchecked")
        List<Object[]> results = reader.createQuery()
                .forRevisionsOfEntity(
                        com.jettech.api.solutions_clinic.model.entity.MedicalRecord.class,
                        false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();

        List<RevisionHistoryResponse> history = results.stream()
                .map(row -> toRevisionResponse(row))
                .toList();

        return ResponseEntity.ok(history);
    }

    // =========================================================================
    // GET /v1/audit/patients/{id}/access-log
    // =========================================================================

    @GetMapping("/patients/{id}/access-log")
    public ResponseEntity<Page<AccessLogResponse>> patientAccessLog(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) throws AuthenticationFailedException {

        Pageable pageable = PageRequest.of(page, size);

        Page<AccessLogResponse> result = auditAccessLogRepository
                .findByPatientIdOrderByAccessedAtDesc(id, pageable)
                .map(log -> new AccessLogResponse(
                        log.getId(),
                        log.getUserEmail(),
                        log.getAction(),
                        log.getEntityType(),
                        log.getEntityId(),
                        log.getIpAddress(),
                        log.getAccessedAt()
                ));

        return ResponseEntity.ok(result);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Converte uma linha do resultado Envers {@code [entity, revisionEntity, revType]}
     * num {@link RevisionHistoryResponse}.
     *
     * <p>Usa {@link DefaultRevisionEntity} para acessar {@code getId()} e
     * {@code getTimestamp()} herdados pela {@link ClinicRevisionEntity}.
     */
    private RevisionHistoryResponse toRevisionResponse(Object[] row) {
        Object entity                    = row[0];
        DefaultRevisionEntity baseRev    = (DefaultRevisionEntity) row[1];
        ClinicRevisionEntity clinicRev   = (ClinicRevisionEntity)  row[1];
        RevisionType revType             = (RevisionType) row[2];

        return new RevisionHistoryResponse(
                baseRev.getId(),
                Instant.ofEpochMilli(baseRev.getTimestamp()),
                revType.getRepresentation(),
                clinicRev.getUserEmail(),
                clinicRev.getIpAddress(),
                entity
        );
    }
}
