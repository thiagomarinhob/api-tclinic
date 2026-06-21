package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.exception.ServiceUnavailableException;
import com.jettech.api.solutions_clinic.model.entity.Exam;
import com.jettech.api.solutions_clinic.model.repository.ExamRepository;
import com.jettech.api.solutions_clinic.model.service.R2StorageService;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Gera URL pré-assinada para upload da solicitação/prescrição médica diretamente ao R2.
 * Não exige status específico do exame — a solicitação pode ser anexada a qualquer momento.
 */
@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetExamRequestUploadUrlUseCase implements GetExamRequestUploadUrlUseCase {

    private static final int PRESIGNED_EXPIRY_MINUTES = 5;

    private final ExamRepository examRepository;
    private final R2StorageService r2StorageService;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public PresignedUploadUrlResponse execute(GetPresignedUploadUrlRequest request) throws AuthenticationFailedException {
        if (!r2StorageService.isConfigured()) {
            log.error("R2 storage não configurado - não é possível gerar URL de upload de solicitação");
            throw new ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }

        UUID tenantId = tenantContext.getRequiredClinicId();
        log.info("Gerando URL de upload de solicitação - tenantId: {}, examId: {}", tenantId, request.examId());

        Exam exam = examRepository.findById(request.examId())
                .orElseThrow(() -> new EntityNotFoundException("Exame", request.examId()));
        if (!exam.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado - exame {} não pertence ao tenantId: {}", request.examId(), tenantId);
            throw new ForbiddenException();
        }

        String objectKey = "tenants/%s/exams/%s/solicitacao_%s"
                .formatted(tenantId, exam.getId(), sanitizeFileName(request.effectiveFileName()));

        String uploadUrl = r2StorageService.createPresignedPutUrl(objectKey, PRESIGNED_EXPIRY_MINUTES);
        if (uploadUrl == null) {
            log.error("Falha ao gerar URL de upload de solicitação R2 para examId: {}", exam.getId());
            throw new ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }

        log.info("URL de upload de solicitação gerada - examId: {}, objectKey: {}", exam.getId(), objectKey);
        return new PresignedUploadUrlResponse(uploadUrl, objectKey, PRESIGNED_EXPIRY_MINUTES);
    }

    private static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "solicitacao.pdf";
        String safe = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        return safe.length() > 200 ? safe.substring(0, 200) : safe;
    }
}
