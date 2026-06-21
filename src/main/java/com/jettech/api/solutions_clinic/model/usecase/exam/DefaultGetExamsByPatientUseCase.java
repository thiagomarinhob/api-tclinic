package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.model.entity.Exam;
import com.jettech.api.solutions_clinic.model.repository.ExamRepository;
import com.jettech.api.solutions_clinic.model.repository.PatientRepository;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetExamsByPatientUseCase implements GetExamsByPatientUseCase {

    private final ExamRepository examRepository;
    private final PatientRepository patientRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public Page<ExamResponse> execute(GetExamsByPatientRequest request) throws AuthenticationFailedException {
        var tenantId = tenantContext.getRequiredClinicId();
        log.info("Listando exames do paciente - tenantId: {}, patientId: {}, page: {}, size: {}",
                tenantId, request.patientId(), request.page(), request.size());

        if (!patientRepository.findById(request.patientId())
                .filter(p -> p.getTenant().getId().equals(tenantId))
                .isPresent()) {
            log.warn("Paciente {} não encontrado ou não pertence ao tenantId: {}", request.patientId(), tenantId);
            throw new EntityNotFoundException("Paciente", request.patientId());
        }

        Pageable pageable = PageRequest.of(request.page(), request.size(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Exam> page = examRepository.findByPatientIdAndTenantId(request.patientId(), tenantId, pageable);
        log.info("Exames encontrados para patientId: {} - total: {}", request.patientId(), page.getTotalElements());
        return page.map(DefaultCreateExamUseCase::toResponse);
    }
}
