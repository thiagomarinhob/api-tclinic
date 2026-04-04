package com.jettech.api.solutions_clinic.model.usecase.professionalschedule;

import com.jettech.api.solutions_clinic.model.entity.ProfessionalSchedule;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultDeleteProfessionalScheduleUseCase implements DeleteProfessionalScheduleUseCase {

    private final ProfessionalScheduleRepository professionalScheduleRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public void execute(UUID id) throws AuthenticationFailedException {
        ProfessionalSchedule schedule = professionalScheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agenda", id));
        if (!schedule.getProfessional().getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        professionalScheduleRepository.delete(schedule);
    }
}
