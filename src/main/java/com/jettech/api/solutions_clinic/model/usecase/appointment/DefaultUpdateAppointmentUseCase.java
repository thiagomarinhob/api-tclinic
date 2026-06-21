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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        log.info("Atualizando agendamento | appointmentId={}", request.id());

        Appointment appointment = appointmentRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", request.id()));
        if (!appointment.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }

        log.info("Agendamento encontrado | appointmentId={} | statusAtual={} | paymentStatusAtual={}",
                appointment.getId(), appointment.getStatus(), appointment.getPaymentStatus());

        if (appointment.getStatus() == AppointmentStatus.CANCELADO ||
            appointment.getStatus() == AppointmentStatus.FINALIZADO) {
            log.warn("Tentativa de atualização em agendamento com status inválido | appointmentId={} | status={}",
                    appointment.getId(), appointment.getStatus());
            throw new InvalidStateException(ApiError.INVALID_STATE_APPOINTMENT_STATUS);
        }

        if (request.patientId() != null) {
            Patient patient = patientRepository.findById(request.patientId())
                    .orElseThrow(() -> new EntityNotFoundException("Paciente", request.patientId()));
            appointment.setPatient(patient);
            log.debug("Paciente atualizado | appointmentId={} | novoPatientId={}", appointment.getId(), request.patientId());
        }

        if (request.professionalId() != null) {
            Professional professional = professionalRepository.findById(request.professionalId())
                    .orElseThrow(() -> new EntityNotFoundException("Profissional", request.professionalId()));
            appointment.setProfessional(professional);
            log.debug("Profissional atualizado | appointmentId={} | novoProfessionalId={}", appointment.getId(), request.professionalId());
        }

        if (request.roomId() != null) {
            Room room = roomRepository.findById(request.roomId())
                    .orElseThrow(() -> new EntityNotFoundException("Sala", request.roomId()));
            appointment.setRoom(room);
            log.debug("Sala atualizada | appointmentId={} | novaRoomId={}", appointment.getId(), request.roomId());
        }

        LocalDateTime scheduledAt = request.scheduledAt() != null ? request.scheduledAt() : appointment.getScheduledAt();
        int durationMinutes = request.durationMinutes() != null ? request.durationMinutes() : appointment.getDurationMinutes();
        UUID professionalId = request.professionalId() != null ? request.professionalId() : appointment.getProfessional().getId();
        UUID roomId = request.roomId() != null ? request.roomId() : (appointment.getRoom() != null ? appointment.getRoom().getId() : null);

        if (request.scheduledAt() != null || request.professionalId() != null || request.durationMinutes() != null) {
            log.info("Validando novo horário | appointmentId={} | novoScheduledAt={} | duracao={}min",
                    appointment.getId(), scheduledAt, durationMinutes);
            validateProfessionalSchedule(professionalId, scheduledAt, durationMinutes);

            String professionalConflict = availabilityConflictChecker.findConflict(
                    scheduledAt,
                    durationMinutes,
                    appointment.getId(),
                    (start, end) -> appointmentRepository.findByProfessionalIdAndScheduledAtBetweenAndStatusNot(
                            professionalId, start, end, AppointmentStatus.CANCELADO),
                    PROFESSIONAL_CONFLICT_MESSAGE
            );
            if (professionalConflict != null && !request.forceSchedule()) {
                log.warn("Conflito de horário bloqueou atualização | appointmentId={} | professionalId={} | scheduledAt={}",
                        appointment.getId(), professionalId, scheduledAt);
                throw new AppointmentConflictException(professionalConflict);
            }
            if (professionalConflict != null) {
                log.warn("Conflito de horário ignorado via forceSchedule | appointmentId={} | professionalId={}",
                        appointment.getId(), professionalId);
            }
        }

        appointment.setScheduledAt(scheduledAt);
        appointment.setDurationMinutes(durationMinutes);

        if (request.observations() != null) {
            appointment.setObservations(request.observations());
        }

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
                if (request.totalValue() == null) {
                    appointment.setTotalValue(loadResult.totalValueFromProcedures());
                }
                log.info("Procedimentos atualizados | appointmentId={} | quantidade={} | valorTotal={}",
                        appointment.getId(), loadResult.procedures().size(), appointment.getTotalValue());
            } else {
                if (request.totalValue() == null) {
                    appointment.setTotalValue(BigDecimal.ZERO);
                }
                log.info("Procedimentos removidos do agendamento | appointmentId={}", appointment.getId());
            }
        }

        if (request.totalValue() != null) {
            log.info("Valor total sobrescrito manualmente | appointmentId={} | novoValor={}", appointment.getId(), request.totalValue());
            appointment.setTotalValue(request.totalValue());
        }

        PaymentStatus oldPaymentStatus = appointment.getPaymentStatus();
        if (request.paymentStatus() != null) {
            appointment.setPaymentStatus(request.paymentStatus());
            log.info("Status de pagamento alterado | appointmentId={} | de={} | para={}",
                    appointment.getId(), oldPaymentStatus, request.paymentStatus());

            if (request.paymentStatus() == PaymentStatus.PAGO && appointment.getPaidAt() == null) {
                appointment.setPaidAt(LocalDateTime.now());
                log.info("Pagamento registrado | appointmentId={} | paidAt={}", appointment.getId(), appointment.getPaidAt());
            }

            if (request.paymentStatus() == PaymentStatus.CANCELADO) {
                appointment.setPaidAt(null);
            }
        }

        if (request.paymentMethod() != null) {
            appointment.setPaymentMethod(request.paymentMethod());
            log.debug("Método de pagamento atualizado | appointmentId={} | metodo={}", appointment.getId(), request.paymentMethod());
        }

        appointment = appointmentRepository.save(appointment);
        log.info("Agendamento atualizado | appointmentId={} | scheduledAt={} | status={}",
                appointment.getId(), appointment.getScheduledAt(), appointment.getStatus());

        try {
            appointmentEmailService.sendUpdate(appointment);
            log.info("E-mail de atualização enviado | appointmentId={}", appointment.getId());
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de atualização | appointmentId={} | erro={}",
                    appointment.getId(), e.getMessage(), e);
        }

        if (request.paymentStatus() != null &&
            request.paymentStatus() == PaymentStatus.PAGO &&
            oldPaymentStatus != PaymentStatus.PAGO) {
            log.info("Sincronizando transação financeira | appointmentId={}", appointment.getId());
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
