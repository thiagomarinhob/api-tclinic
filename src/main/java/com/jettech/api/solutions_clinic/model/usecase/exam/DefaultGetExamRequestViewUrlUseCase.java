package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.exception.InvalidRequestException;
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

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetExamRequestViewUrlUseCase implements GetExamRequestViewUrlUseCase {

    private static final int VIEW_URL_EXPIRY_MINUTES = 15;

    private final ExamRepository examRepository;
    private final R2StorageService r2StorageService;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public ExamResultViewUrlResponse execute(UUID examId) throws AuthenticationFailedException {
        log.info("Gerando URL de visualização da solicitação - examId: {}", examId);
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exame", examId));
        UUID tenantId = tenantContext.getRequiredClinicId();
        if (!exam.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado à solicitação do exame {} - tenantId: {}", examId, tenantId);
            throw new ForbiddenException();
        }
        if (exam.getRequestFileKey() == null || exam.getRequestFileKey().isBlank()) {
            log.warn("Exame {} não possui solicitação anexada", examId);
            throw new InvalidRequestException("Exame não possui solicitação anexada.");
        }
        if (!r2StorageService.isConfigured()) {
            log.error("R2 storage não configurado - não é possível gerar URL de visualização de solicitação");
            throw new ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }
        String url = r2StorageService.createPresignedGetUrl(exam.getRequestFileKey(), VIEW_URL_EXPIRY_MINUTES);
        if (url == null) {
            log.error("Falha ao gerar URL de visualização de solicitação R2 para examId: {}", examId);
            throw new ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }
        log.info("URL de visualização de solicitação gerada - examId: {}, expiryMinutes: {}", examId, VIEW_URL_EXPIRY_MINUTES);
        return new ExamResultViewUrlResponse(url);
    }
}
