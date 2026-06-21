package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.model.entity.Exam;
import com.jettech.api.solutions_clinic.model.entity.ExamStatus;
import com.jettech.api.solutions_clinic.model.repository.ExamRepository;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultConfirmExamResultUploadUseCase implements ConfirmExamResultUploadUseCase {

    private final ExamRepository examRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ExamResponse execute(ConfirmExamResultRequest request) throws AuthenticationFailedException {
        var tenantId = tenantContext.getRequiredClinicId();
        log.info("Confirmando upload de resultado - tenantId: {}, examId: {}, objectKey: {}",
                tenantId, request.examId(), request.objectKey());

        Exam exam = examRepository.findById(request.examId())
                .orElseThrow(() -> new EntityNotFoundException("Exame", request.examId()));
        if (!exam.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado ao confirmar upload de resultado - exame {} não pertence ao tenantId: {}", request.examId(), tenantId);
            throw new ForbiddenException();
        }

        exam.setResultFileKey(request.objectKey());
        exam.setStatus(ExamStatus.COMPLETED);
        exam = examRepository.save(exam);

        log.info("Resultado de exame confirmado - examId: {}, status: {}, objectKey: {}",
                exam.getId(), exam.getStatus(), exam.getResultFileKey());
        return DefaultCreateExamUseCase.toResponse(exam);
    }
}
