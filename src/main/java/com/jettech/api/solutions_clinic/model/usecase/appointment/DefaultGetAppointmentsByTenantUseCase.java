package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetAppointmentsByTenantUseCase implements GetAppointmentsByTenantUseCase {

    private final AppointmentRepository appointmentRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;
    private final AppointmentResponseMapper mapper;

    @Override
    public List<AppointmentResponse> execute(GetAppointmentsByTenantRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.tenantId());
        tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.tenantId()));

        List<Appointment> appointments;

        // Aplicar filtros
        if (request.startDate() != null && request.endDate() != null) {
            // Intervalo de datas (ex.: calendário semana/mês)
            LocalDateTime start = request.startDate().atStartOfDay();
            LocalDateTime end = request.endDate().atTime(LocalTime.MAX);
            if (request.status() != null) {
                appointments = appointmentRepository.findByTenantIdAndScheduledAtBetweenAndStatus(
                        request.tenantId(), start, end, request.status());
            } else {
                appointments = appointmentRepository.findByTenantIdAndScheduledAtBetween(
                        request.tenantId(), start, end);
            }
        } else if (request.date() != null && request.status() != null) {
            // Filtrar por data E status
            LocalDateTime startOfDay = request.date().atStartOfDay();
            LocalDateTime endOfDay = request.date().atTime(LocalTime.MAX);
            appointments = appointmentRepository.findByTenantIdAndScheduledAtBetweenAndStatus(
                    request.tenantId(),
                    startOfDay,
                    endOfDay,
                    request.status()
            );
        } else if (request.date() != null) {
            // Filtrar apenas por data
            LocalDateTime startOfDay = request.date().atStartOfDay();
            LocalDateTime endOfDay = request.date().atTime(LocalTime.MAX);
            appointments = appointmentRepository.findByTenantIdAndScheduledAtBetween(
                    request.tenantId(),
                    startOfDay,
                    endOfDay
            );
        } else if (request.status() != null) {
            // Filtrar apenas por status
            appointments = appointmentRepository.findByTenantIdAndStatus(
                    request.tenantId(),
                    request.status()
            );
        } else {
            // Sem filtros, retornar todos
            appointments = appointmentRepository.findByTenantId(request.tenantId());
        }

        // Aplicar ordenação
        appointments = applyOrdering(appointments, request.orderBy());

        return appointments.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    private List<Appointment> applyOrdering(List<Appointment> appointments, String orderBy) {
        if (orderBy == null || orderBy.isEmpty()) {
            // Ordenação padrão: por data agendada (mais recente primeiro)
            return appointments.stream()
                    .sorted(Comparator.comparing(Appointment::getScheduledAt).reversed())
                    .collect(Collectors.toList());
        }

        return switch (orderBy.toLowerCase()) {
            case "scheduledat", "scheduled_at", "scheduledat_asc", "scheduled_at_asc" -> appointments.stream()
                    .sorted(Comparator.comparing(Appointment::getScheduledAt))
                    .collect(Collectors.toList());
            case "scheduledat_desc", "scheduled_at_desc" -> appointments.stream()
                    .sorted(Comparator.comparing(Appointment::getScheduledAt).reversed())
                    .collect(Collectors.toList());
            case "createdat", "created_at" -> appointments.stream()
                    .sorted(Comparator.comparing(Appointment::getCreatedAt))
                    .collect(Collectors.toList());
            case "createdat_desc", "created_at_desc" -> appointments.stream()
                    .sorted(Comparator.comparing(Appointment::getCreatedAt).reversed())
                    .collect(Collectors.toList());
            case "patientname", "patient_name" -> appointments.stream()
                    .sorted(Comparator.comparing(a -> a.getPatient().getFirstName()))
                    .collect(Collectors.toList());
            case "professionalname", "professional_name" -> appointments.stream()
                    .sorted(Comparator.comparing(a -> a.getProfessional().getUser().getFirstName()))
                    .collect(Collectors.toList());
            default -> appointments.stream()
                    .sorted(Comparator.comparing(Appointment::getScheduledAt).reversed())
                    .collect(Collectors.toList());
        };
    }

}
