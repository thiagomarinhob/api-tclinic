package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.exception.*;
import com.jettech.api.solutions_clinic.model.entity.Exam;
import com.jettech.api.solutions_clinic.model.repository.ExamRepository;
import com.jettech.api.solutions_clinic.model.service.R2StorageService;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exame", examId));
        if (!exam.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        if (exam.getResultFileKey() == null || exam.getResultFileKey().isBlank()) {
            throw new InvalidRequestException("Exame ainda não possui resultado anexado.");
        }
        if (!r2StorageService.isConfigured()) {
            throw new com.jettech.api.solutions_clinic.exception.ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }
        String url = r2StorageService.createPresignedGetUrl(exam.getResultFileKey(), VIEW_URL_EXPIRY_MINUTES);
        if (url == null) {
            throw new com.jettech.api.solutions_clinic.exception.ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }
        return new ExamResultViewUrlResponse(url);
    }
}
