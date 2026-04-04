package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.model.entity.Exam;
import com.jettech.api.solutions_clinic.model.repository.ExamRepository;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetExamsByTenantUseCase implements GetExamsByTenantUseCase {

    private final ExamRepository examRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public Page<ExamListResponse> execute(GetExamsByTenantRequest request) throws AuthenticationFailedException {
        var tenantId = tenantContext.getRequiredClinicId();
        Pageable pageable = PageRequest.of(request.page(), request.size(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Exam> page = examRepository.findByTenantIdWithFilters(
                tenantId,
                request.patientId(),
                request.status(),
                pageable
        );
        return page.map(this::toListResponse);
    }

    private ExamListResponse toListResponse(Exam exam) {
        return new ExamListResponse(
                exam.getId(),
                exam.getTenant().getId(),
                exam.getPatient().getId(),
                exam.getPatient().getFirstName(),
                exam.getAppointment() != null ? exam.getAppointment().getId() : null,
                exam.getName(),
                exam.getClinicalIndication(),
                exam.getStatus(),
                exam.getResultFileKey(),
                exam.getCreatedAt(),
                exam.getUpdatedAt()
        );
    }
}
