package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.Exam;
import com.jettech.api.solutions_clinic.model.repository.ExamRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultConfirmExamRequestUploadUseCase implements ConfirmExamRequestUploadUseCase {

    private final ExamRepository examRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ExamResponse execute(ConfirmExamRequestUploadRequest request) throws AuthenticationFailedException {
        var tenantId = tenantContext.getRequiredClinicId();
        Exam exam = examRepository.findById(request.examId())
                .orElseThrow(() -> new EntityNotFoundException("Exame", request.examId()));
        if (!exam.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException();
        }

        exam.setRequestFileKey(request.objectKey());
        exam = examRepository.save(exam);

        return DefaultCreateExamUseCase.toResponse(exam);
    }
}
