package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetAppointmentsByProfessionalIdUseCase implements GetAppointmentsByProfessionalIdUseCase {

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalRepository professionalRepository;
    private final TenantContext tenantContext;
    private final AppointmentResponseMapper mapper;

    @Override
    public List<AppointmentResponse> execute(GetAppointmentsByProfessionalIdRequest request) throws AuthenticationFailedException {
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

        return appointments.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
