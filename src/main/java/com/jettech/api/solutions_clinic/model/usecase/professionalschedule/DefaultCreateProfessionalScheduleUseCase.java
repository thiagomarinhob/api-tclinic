package com.jettech.api.solutions_clinic.model.usecase.professionalschedule;

import com.jettech.api.solutions_clinic.model.entity.Professional;
import com.jettech.api.solutions_clinic.model.entity.ProfessionalSchedule;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.DuplicateEntityException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateProfessionalScheduleUseCase implements CreateProfessionalScheduleUseCase {

    private final ProfessionalScheduleRepository professionalScheduleRepository;
    private final ProfessionalRepository professionalRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ProfessionalScheduleResponse execute(CreateProfessionalScheduleRequest request) throws AuthenticationFailedException {
        Professional professional = professionalRepository.findById(request.professionalId())
                .orElseThrow(() -> new EntityNotFoundException("Profissional", request.professionalId()));
        if (!professional.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        professionalScheduleRepository.findByProfessionalIdAndDayOfWeek(
                request.professionalId(), request.dayOfWeek())
                .ifPresent(schedule -> {
                    throw new DuplicateEntityException(ApiError.DUPLICATE_SCHEDULE, request.dayOfWeek());
                });

        ProfessionalSchedule schedule = new ProfessionalSchedule();
        schedule.setProfessional(professional);
        schedule.setDayOfWeek(request.dayOfWeek());

        schedule = professionalScheduleRepository.save(schedule);

        return new ProfessionalScheduleResponse(
                schedule.getId(),
                schedule.getProfessional().getId(),
                schedule.getDayOfWeek(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }
}
