package com.jettech.api.solutions_clinic.model.usecase.professionalschedule;

import com.jettech.api.solutions_clinic.model.entity.ProfessionalSchedule;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetProfessionalSchedulesByProfessionalIdUseCase implements GetProfessionalSchedulesByProfessionalIdUseCase {

    private final ProfessionalScheduleRepository professionalScheduleRepository;
    private final ProfessionalRepository professionalRepository;
    private final TenantContext tenantContext;

    @Override
    public List<ProfessionalScheduleResponse> execute(UUID professionalId) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        log.info("Listando agendas do profissional - professionalId: {}, tenantId: {}", professionalId, tenantId);
        var professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new EntityNotFoundException("Profissional", professionalId));
        if (!professional.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado às agendas do profissional {} - tenantId: {}", professionalId, tenantId);
            throw new ForbiddenException();
        }

        List<ProfessionalSchedule> schedules = professionalScheduleRepository.findByProfessionalId(professionalId);
        log.info("Agendas encontradas para professionalId: {} - total: {}", professionalId, schedules.size());

        return schedules.stream()
                .map(schedule -> new ProfessionalScheduleResponse(
                        schedule.getId(),
                        schedule.getProfessional().getId(),
                        schedule.getDayOfWeek(),
                        schedule.getCreatedAt(),
                        schedule.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }
}
