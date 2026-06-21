package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.model.entity.Exam;
import com.jettech.api.solutions_clinic.model.repository.ExamRepository;
import com.jettech.api.solutions_clinic.model.service.R2StorageService;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.exception.ServiceUnavailableException;
import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Gera URL pré-assinada para o frontend fazer upload do arquivo de resultado diretamente ao R2.
 * Só permite exames em REQUESTED ou PENDING_RESULT.
 */
@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetPresignedUploadUrlUseCase implements GetPresignedUploadUrlUseCase {

    private static final int PRESIGNED_EXPIRY_MINUTES = 5;

    private final ExamRepository examRepository;
    private final R2StorageService r2StorageService;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public PresignedUploadUrlResponse execute(GetPresignedUploadUrlRequest request) throws AuthenticationFailedException {
        if (!r2StorageService.isConfigured()) {
            log.error("R2 storage não configurado - não é possível gerar URL de upload");
            throw new ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }

        UUID tenantId = tenantContext.getRequiredClinicId();
        log.info("Gerando URL de upload de resultado - tenantId: {}, examId: {}", tenantId, request.examId());

        Exam exam = examRepository.findById(request.examId())
                .orElseThrow(() -> new EntityNotFoundException("Exame", request.examId()));
        if (!exam.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado - exame {} não pertence ao tenantId: {}", request.examId(), tenantId);
            throw new ForbiddenException();
        }
        if (exam.getStatus() == com.jettech.api.solutions_clinic.model.entity.ExamStatus.COMPLETED) {
            log.warn("Upload bloqueado - exame {} já está com status COMPLETED", request.examId());
            throw new com.jettech.api.solutions_clinic.exception.InvalidStateException(ApiError.INVALID_STATE);
        }

        String objectKey = "tenants/%s/exams/%s/%s"
                .formatted(tenantId, exam.getId(), sanitizeFileName(request.effectiveFileName()));

        String uploadUrl = r2StorageService.createPresignedPutUrl(objectKey, PRESIGNED_EXPIRY_MINUTES);
        if (uploadUrl == null) {
            log.error("Falha ao gerar URL de upload R2 para examId: {}", exam.getId());
            throw new ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }

        log.info("URL de upload gerada com sucesso - examId: {}, objectKey: {}, expiryMinutes: {}",
                exam.getId(), objectKey, PRESIGNED_EXPIRY_MINUTES);
        return new PresignedUploadUrlResponse(uploadUrl, objectKey, PRESIGNED_EXPIRY_MINUTES);
    }

    private static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "resultado.pdf";
        String safe = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        return safe.length() > 200 ? safe.substring(0, 200) : safe;
    }
}
