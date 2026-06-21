package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.model.entity.Exam;
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

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetExamByIdUseCase implements GetExamByIdUseCase {

    private final ExamRepository examRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public ExamResponse execute(UUID id) throws AuthenticationFailedException {
        log.info("Buscando exame por id: {}", id);
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exame", id));
        UUID tenantId = tenantContext.getRequiredClinicId();
        if (!exam.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado ao exame {} - tenantId do contexto: {}", id, tenantId);
            throw new ForbiddenException();
        }
        log.info("Exame {} encontrado - status: {}", id, exam.getStatus());
        return DefaultCreateExamUseCase.toResponse(exam);
    }
}
