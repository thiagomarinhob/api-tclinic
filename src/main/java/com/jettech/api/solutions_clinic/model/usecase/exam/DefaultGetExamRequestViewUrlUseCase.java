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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exame", examId));
        if (!exam.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        if (exam.getRequestFileKey() == null || exam.getRequestFileKey().isBlank()) {
            throw new InvalidRequestException("Exame não possui solicitação anexada.");
        }
        if (!r2StorageService.isConfigured()) {
            throw new ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }
        String url = r2StorageService.createPresignedGetUrl(exam.getRequestFileKey(), VIEW_URL_EXPIRY_MINUTES);
        if (url == null) {
            throw new ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }
        return new ExamResultViewUrlResponse(url);
    }
}
