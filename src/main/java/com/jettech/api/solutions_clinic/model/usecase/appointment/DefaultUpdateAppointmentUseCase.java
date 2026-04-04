package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentProcedure;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.entity.PaymentStatus;
import com.jettech.api.solutions_clinic.model.entity.Procedure;
import com.jettech.api.solutions_clinic.model.entity.Professional;
import com.jettech.api.solutions_clinic.model.entity.Room;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.PatientRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalScheduleRepository;
import com.jettech.api.solutions_clinic.model.repository.RoomRepository;
import com.jettech.api.solutions_clinic.model.service.AppointmentEmailService;
import com.jettech.api.solutions_clinic.model.service.FinancialSyncService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AppointmentConflictException;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.InvalidStateException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultUpdateAppointmentUseCase implements UpdateAppointmentUseCase {

    private static final String PROFESSIONAL_CONFLICT_MESSAGE =
            "Já existe um agendamento para este profissional neste horário. Deseja agendar mesmo assim?";

    private final AppointmentRepository appointmentRepository;
    private final AvailabilityConflictChecker availabilityConflictChecker;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final RoomRepository roomRepository;
    private final ProfessionalScheduleRepository professionalScheduleRepository;
    private final FinancialSyncService financialSyncService;
    private final TenantContext tenantContext;
    private final AppointmentEmailService appointmentEmailService;
    private final ProcedureLoader procedureLoader;
    private final AppointmentResponseMapper mapper;

    @Override
    @Transactional
    public AppointmentResponse execute(UpdateAppointmentRequest request) throws AuthenticationFailedException {
        Appointment appointment = appointmentRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", request.id()));
        if (!appointment.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        // Não permitir atualização de agendamentos cancelados ou finalizados
        if (appointment.getStatus() == AppointmentStatus.CANCELADO || 
            appointment.getStatus() == AppointmentStatus.FINALIZADO) {
            throw new InvalidStateException(ApiError.INVALID_STATE_APPOINTMENT_STATUS);
        }

        // Atualizar paciente se fornecido
        if (request.patientId() != null) {
            Patient patient = patientRepository.findById(request.patientId())
                    .orElseThrow(() -> new EntityNotFoundException("Paciente", request.patientId()));
            appointment.setPatient(patient);
        }

        // Atualizar profissional se fornecido
        if (request.professionalId() != null) {
            Professional professional = professionalRepository.findById(request.professionalId())
                    .orElseThrow(() -> new EntityNotFoundException("Profissional", request.professionalId()));
            appointment.setProfessional(professional);
        }

        // Atualizar sala se fornecido
        if (request.roomId() != null) {
            Room room = roomRepository.findById(request.roomId())
                    .orElseThrow(() -> new EntityNotFoundException("Sala", request.roomId()));
            appointment.setRoom(room);
        }

        // Atualizar horário se fornecido
        LocalDateTime scheduledAt = request.scheduledAt() != null ? request.scheduledAt() : appointment.getScheduledAt();
        int durationMinutes = request.durationMinutes() != null ? request.durationMinutes() : appointment.getDurationMinutes();
        UUID professionalId = request.professionalId() != null ? request.professionalId() : appointment.getProfessional().getId();
        UUID roomId = request.roomId() != null ? request.roomId() : (appointment.getRoom() != null ? appointment.getRoom().getId() : null);

        // Validar horário disponível se horário ou profissional mudou
        if (request.scheduledAt() != null || request.professionalId() != null || request.durationMinutes() != null) {
            validateProfessionalSchedule(professionalId, scheduledAt, durationMinutes);
            
            // Verificar conflito de horário com outros agendamentos do profissional
            String professionalConflict = availabilityConflictChecker.findConflict(
                    scheduledAt,
                    durationMinutes,
                    appointment.getId(),
                    (start, end) -> appointmentRepository.findByProfessionalIdAndScheduledAtBetweenAndStatusNot(
                            professionalId, start, end, AppointmentStatus.CANCELADO),
                    PROFESSIONAL_CONFLICT_MESSAGE
            );
            if (professionalConflict != null && !request.forceSchedule()) {
                throw new AppointmentConflictException(professionalConflict);
            }
        }

        appointment.setScheduledAt(scheduledAt);
        appointment.setDurationMinutes(durationMinutes);

        // Atualizar observações se fornecido
        if (request.observations() != null) {
            appointment.setObservations(request.observations());
        }

        // Atualizar procedimentos se fornecido e recalcular totalValue
        if (request.procedureIds() != null) {
            UUID tenantId = tenantContext.getRequiredClinicId();
            appointment.getProcedures().clear();

            if (!request.procedureIds().isEmpty()) {
                ProcedureLoadResult loadResult = procedureLoader.loadAndValidate(request.procedureIds(), tenantId);
                for (Procedure procedure : loadResult.procedures()) {
                    AppointmentProcedure ap = new AppointmentProcedure();
                    ap.setAppointment(appointment);
                    ap.setProcedure(procedure);
                    ap.setFinalPrice(procedure.getBasePrice());
                    appointment.getProcedures().add(ap);
                }
                // Recalcular totalValue a partir dos procedimentos (salvo override explícito)
                if (request.totalValue() == null) {
                    appointment.setTotalValue(loadResult.totalValueFromProcedures());
                }
            } else {
                // Lista vazia: zera os procedimentos mas mantém totalValue se explicitamente fornecido
                if (request.totalValue() == null) {
                    appointment.setTotalValue(BigDecimal.ZERO);
                }
            }
        }

        // Atualizar valor total se fornecido explicitamente (override dos procedimentos)
        if (request.totalValue() != null) {
            appointment.setTotalValue(request.totalValue());
        }

        // Atualizar status de pagamento se fornecido
        PaymentStatus oldPaymentStatus = appointment.getPaymentStatus();
        if (request.paymentStatus() != null) {
            appointment.setPaymentStatus(request.paymentStatus());
            
            // Se foi marcado como PAGO, definir paidAt
            if (request.paymentStatus() == PaymentStatus.PAGO && appointment.getPaidAt() == null) {
                appointment.setPaidAt(LocalDateTime.now());
            }
            
            // Se foi cancelado, limpar paidAt
            if (request.paymentStatus() == PaymentStatus.CANCELADO) {
                appointment.setPaidAt(null);
            }
        }

        // Atualizar método de pagamento se fornecido
        if (request.paymentMethod() != null) {
            appointment.setPaymentMethod(request.paymentMethod());
        }

        appointment = appointmentRepository.save(appointment);

        appointmentEmailService.sendUpdate(appointment);

        // Sincronizar transação financeira se o status de pagamento mudou para PAGO
        if (request.paymentStatus() != null && 
            request.paymentStatus() == PaymentStatus.PAGO && 
            oldPaymentStatus != PaymentStatus.PAGO) {
            financialSyncService.syncAppointmentPayment(appointment);
        }

        return mapper.toResponse(appointment);
    }

    private void validateProfessionalSchedule(UUID professionalId, LocalDateTime scheduledAt, int durationMinutes) {
        DayOfWeek dayOfWeek = scheduledAt.getDayOfWeek();

        professionalScheduleRepository
                .findByProfessionalIdAndDayOfWeek(professionalId, dayOfWeek)
                .orElseThrow(() -> new EntityNotFoundException(ApiError.ENTITY_NOT_FOUND_SCHEDULE));
    }

}
