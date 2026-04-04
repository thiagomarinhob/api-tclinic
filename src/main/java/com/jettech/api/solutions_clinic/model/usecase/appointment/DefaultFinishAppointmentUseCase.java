package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.entity.PaymentStatus;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.service.FinancialSyncService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.exception.InvalidStateException;
import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.security.TenantContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultFinishAppointmentUseCase implements FinishAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final TenantContext tenantContext;
    private final FinancialSyncService financialSyncService;
    private final AppointmentResponseMapper mapper;

    @Override
    @Transactional
    public AppointmentResponse execute(UUID appointmentId, FinishAppointmentRequest request) throws AuthenticationFailedException {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", appointmentId));
        if (!appointment.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        if (appointment.getStatus() != AppointmentStatus.EM_ATENDIMENTO) {
            throw new InvalidStateException(ApiError.INVALID_STATE_APPOINTMENT_STATUS);
        }

        LocalDateTime finishedAt = LocalDateTime.now();
        appointment.setStatus(AppointmentStatus.FINALIZADO);
        appointment.setFinishedAt(finishedAt);

        if (appointment.getStartedAt() != null) {
            long minutes = Duration.between(appointment.getStartedAt(), finishedAt).toMinutes();
            appointment.setDurationActualMinutes((int) minutes);
        }

        if (request != null) {
            if (request.observations() != null) {
                appointment.setObservations(request.observations());
            }
            if (request.paymentStatus() != null) {
                PaymentStatus oldPaymentStatus = appointment.getPaymentStatus();
                appointment.setPaymentStatus(request.paymentStatus());
                if (request.paymentStatus() == PaymentStatus.PAGO && appointment.getPaidAt() == null) {
                    appointment.setPaidAt(finishedAt);
                }
                if (request.paymentStatus() == PaymentStatus.CANCELADO) {
                    appointment.setPaidAt(null);
                }
                if (request.paymentStatus() == PaymentStatus.PAGO && oldPaymentStatus != PaymentStatus.PAGO) {
                    financialSyncService.syncAppointmentPayment(appointment);
                }
            }
            if (request.paymentMethod() != null) {
                appointment.setPaymentMethod(request.paymentMethod());
            }
        }

        appointment = appointmentRepository.save(appointment);
        return mapper.toResponse(appointment);
    }
}
