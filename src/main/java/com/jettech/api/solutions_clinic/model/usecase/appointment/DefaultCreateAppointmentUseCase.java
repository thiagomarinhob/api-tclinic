package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.*;
import com.jettech.api.solutions_clinic.model.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AppointmentConflictException;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.service.AppointmentEmailService;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateAppointmentUseCase implements CreateAppointmentUseCase {

    private static final String PROFESSIONAL_CONFLICT_MESSAGE =
            "Já existe um agendamento para este profissional neste horário. Deseja agendar mesmo assim?";

    private final AppointmentRepository appointmentRepository;
    private final AvailabilityConflictChecker availabilityConflictChecker;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final ProfessionalScheduleValidator professionalScheduleValidator;
    private final ProcedureLoader procedureLoader;
    private final AppointmentResponseMapper appointmentResponseMapper;
    private final TenantContext tenantContext;
    private final AppointmentEmailService appointmentEmailService;

    @Override
    @Transactional
    public AppointmentResponse execute(CreateAppointmentRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente", request.patientId()));

        Professional professional = professionalRepository.findById(request.professionalId())
                .orElseThrow(() -> new EntityNotFoundException("Profissional", request.professionalId()));

        Room room = null;
        if (request.roomId() != null) {
            room = roomRepository.findById(request.roomId())
                    .orElseThrow(() -> new EntityNotFoundException("Sala", request.roomId()));
        }

        User createdBy = userRepository.findById(request.createdBy())
                .orElseThrow(() -> new EntityNotFoundException("Usuário", request.createdBy()));

        List<Procedure> procedures = new ArrayList<>();
        int calculatedDurationMinutes = request.durationMinutes();
        BigDecimal calculatedTotalValue = request.totalValue();

        if (request.procedureIds() != null && !request.procedureIds().isEmpty()) {
            ProcedureLoadResult loadResult = procedureLoader.loadAndValidate(request.procedureIds(), tenantId);
            procedures = loadResult.procedures();
            calculatedDurationMinutes = loadResult.totalDurationMinutes();
            if (request.totalValue() == null || request.totalValue().compareTo(BigDecimal.ZERO) == 0) {
                calculatedTotalValue = loadResult.totalValueFromProcedures();
            }
        }

        professionalScheduleValidator.validate(professional.getId(), request.scheduledAt(), calculatedDurationMinutes);

        String professionalConflict = availabilityConflictChecker.findConflict(
                request.scheduledAt(),
                calculatedDurationMinutes,
                null,
                (start, end) -> appointmentRepository.findByProfessionalIdAndScheduledAtBetweenAndStatusNot(
                        professional.getId(), start, end, AppointmentStatus.CANCELADO),
                PROFESSIONAL_CONFLICT_MESSAGE
        );
        if (professionalConflict != null && !request.forceSchedule()) {
            throw new AppointmentConflictException(professionalConflict);
        }

        Appointment appointment = new Appointment();
        appointment.setTenant(tenant);
        appointment.setPatient(patient);
        appointment.setProfessional(professional);
        appointment.setRoom(room);
        appointment.setScheduledAt(request.scheduledAt());
        appointment.setDurationMinutes(calculatedDurationMinutes);
        appointment.setStatus(AppointmentStatus.AGENDADO);
        appointment.setObservations(request.observations());
        appointment.setTotalValue(calculatedTotalValue);
        appointment.setPaymentStatus(PaymentStatus.PENDENTE);
        appointment.setCreatedBy(createdBy);

        appointment = appointmentRepository.save(appointment);

        if (!procedures.isEmpty()) {
            for (Procedure procedure : procedures) {
                AppointmentProcedure appointmentProcedure = new AppointmentProcedure();
                appointmentProcedure.setAppointment(appointment);
                appointmentProcedure.setProcedure(procedure);
                appointmentProcedure.setFinalPrice(procedure.getBasePrice());
                appointment.getProcedures().add(appointmentProcedure);
            }
            appointment = appointmentRepository.save(appointment);
        }

        appointmentEmailService.sendConfirmation(appointment);

        return appointmentResponseMapper.toResponse(appointment);
    }

}
