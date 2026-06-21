package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetAppointmentsByProfessionalIdUseCase implements GetAppointmentsByProfessionalIdUseCase {

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalRepository professionalRepository;
    private final TenantContext tenantContext;
    private final AppointmentResponseMapper mapper;

    @Override
    public List<AppointmentResponse> execute(GetAppointmentsByProfessionalIdRequest request) throws AuthenticationFailedException {
        log.info("Listando agendamentos por profissional | professionalId={} | startDate={} | endDate={}",
                request.professionalId(), request.startDate(), request.endDate());

        var professional = professionalRepository.findById(request.professionalId())
                .orElseThrow(() -> new EntityNotFoundException("Profissional", request.professionalId()));
        if (!professional.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }

        List<Appointment> appointments;
        if (request.startDate() != null && request.endDate() != null) {
            appointments = appointmentRepository.findByProfessionalIdAndScheduledAtBetween(
                    request.professionalId(),
                    request.startDate().atStartOfDay(),
                    request.endDate().atTime(LocalTime.MAX)
            );
        } else {
            appointments = appointmentRepository.findByProfessionalId(request.professionalId());
        }

        log.info("Agendamentos retornados | professionalId={} | total={}", request.professionalId(), appointments.size());

        return appointments.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
