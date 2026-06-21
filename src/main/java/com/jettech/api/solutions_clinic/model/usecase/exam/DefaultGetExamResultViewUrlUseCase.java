package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.exception.*;
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

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetExamResultViewUrlUseCase implements GetExamResultViewUrlUseCase {

    private static final int VIEW_URL_EXPIRY_MINUTES = 15;

    private final ExamRepository examRepository;
    private final R2StorageService r2StorageService;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public ExamResultViewUrlResponse execute(UUID examId) throws AuthenticationFailedException {
        log.info("Gerando URL de visualização do resultado - examId: {}", examId);
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exame", examId));
        UUID tenantId = tenantContext.getRequiredClinicId();
        if (!exam.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado ao resultado do exame {} - tenantId: {}", examId, tenantId);
            throw new ForbiddenException();
        }
        if (exam.getResultFileKey() == null || exam.getResultFileKey().isBlank()) {
            log.warn("Exame {} não possui resultado anexado", examId);
            throw new InvalidRequestException("Exame ainda não possui resultado anexado.");
        }
        if (!r2StorageService.isConfigured()) {
            log.error("R2 storage não configurado - não é possível gerar URL de visualização de resultado");
            throw new com.jettech.api.solutions_clinic.exception.ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }
        String url = r2StorageService.createPresignedGetUrl(exam.getResultFileKey(), VIEW_URL_EXPIRY_MINUTES);
        if (url == null) {
            log.error("Falha ao gerar URL de visualização R2 para examId: {}", examId);
            throw new com.jettech.api.solutions_clinic.exception.ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }
        log.info("URL de visualização de resultado gerada - examId: {}, expiryMinutes: {}", examId, VIEW_URL_EXPIRY_MINUTES);
        return new ExamResultViewUrlResponse(url);
    }
}
