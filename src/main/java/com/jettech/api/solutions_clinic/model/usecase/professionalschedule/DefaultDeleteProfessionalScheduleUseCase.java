package com.jettech.api.solutions_clinic.model.usecase.professionalschedule;

import com.jettech.api.solutions_clinic.model.entity.ProfessionalSchedule;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultDeleteProfessionalScheduleUseCase implements DeleteProfessionalScheduleUseCase {

    private final ProfessionalScheduleRepository professionalScheduleRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public void execute(UUID id) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        log.info("Deletando agenda do profissional - scheduleId: {}, tenantId: {}", id, tenantId);
        ProfessionalSchedule schedule = professionalScheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agenda", id));
        if (!schedule.getProfessional().getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado ao deletar agenda {} - tenantId: {}", id, tenantId);
            throw new ForbiddenException();
        }
        professionalScheduleRepository.delete(schedule);
        log.info("Agenda do profissional deletada - scheduleId: {}", id);
    }
}
