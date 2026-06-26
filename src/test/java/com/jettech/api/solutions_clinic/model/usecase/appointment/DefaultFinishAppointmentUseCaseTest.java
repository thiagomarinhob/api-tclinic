package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.exception.InvalidStateException;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.entity.PaymentMethod;
import com.jettech.api.solutions_clinic.model.entity.PaymentStatus;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.service.FinancialSyncService;
import com.jettech.api.solutions_clinic.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.jettech.api.solutions_clinic.model.usecase.appointment.AppointmentFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultFinishAppointmentUseCaseTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock TenantContext tenantContext;
    @Mock FinancialSyncService financialSyncService;
    @Mock AppointmentResponseMapper mapper;

    @InjectMocks
    DefaultFinishAppointmentUseCase useCase;

    private Appointment appointment;

    @BeforeEach
    void setUp() throws Exception {
        appointment = appointment();
        appointment.setStatus(AppointmentStatus.EM_ATENDIMENTO);

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(mapper.toResponse(any())).thenReturn(appointmentResponse());
    }

    @Test
    void shouldFinishAppointmentAndSetStatusToFinalizado() throws Exception {
        useCase.execute(APPOINTMENT_ID, null);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.FINALIZADO);
        assertThat(appointment.getFinishedAt()).isNotNull();
    }

    @Test
    void shouldCalculateActualDurationWhenStartedAtIsSet() throws Exception {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(45);
        appointment.setStartedAt(startedAt);

        useCase.execute(APPOINTMENT_ID, null);

        assertThat(appointment.getDurationActualMinutes()).isGreaterThanOrEqualTo(44);
    }

    @Test
    void shouldNotSetDurationActualMinutesWhenStartedAtIsNull() throws Exception {
        appointment.setStartedAt(null);

        useCase.execute(APPOINTMENT_ID, null);

        assertThat(appointment.getDurationActualMinutes()).isNull();
    }

    @Test
    void shouldSetPaidAtWhenPaymentStatusChangesToPago() throws Exception {
        appointment.setPaidAt(null);
        FinishAppointmentRequest request = new FinishAppointmentRequest(null, PaymentStatus.PAGO, null);

        useCase.execute(APPOINTMENT_ID, request);

        assertThat(appointment.getPaidAt()).isNotNull();
        assertThat(appointment.getPaymentStatus()).isEqualTo(PaymentStatus.PAGO);
    }

    @Test
    void shouldClearPaidAtWhenPaymentStatusChangesToCancelled() throws Exception {
        appointment.setPaidAt(LocalDateTime.now());
        FinishAppointmentRequest request = new FinishAppointmentRequest(null, PaymentStatus.CANCELADO, null);

        useCase.execute(APPOINTMENT_ID, request);

        assertThat(appointment.getPaidAt()).isNull();
    }

    @Test
    void shouldSyncFinancialWhenPaymentChangesFromPendenteToPago() throws Exception {
        appointment.setPaymentStatus(PaymentStatus.PENDENTE);
        FinishAppointmentRequest request = new FinishAppointmentRequest(null, PaymentStatus.PAGO, null);

        useCase.execute(APPOINTMENT_ID, request);

        verify(financialSyncService).syncAppointmentPayment(appointment);
    }

    @Test
    void shouldNotSyncFinancialWhenPaymentWasAlreadyPago() throws Exception {
        appointment.setPaymentStatus(PaymentStatus.PAGO);
        appointment.setPaidAt(LocalDateTime.now());
        FinishAppointmentRequest request = new FinishAppointmentRequest(null, PaymentStatus.PAGO, null);

        useCase.execute(APPOINTMENT_ID, request);

        verify(financialSyncService, never()).syncAppointmentPayment(any());
    }

    @Test
    void shouldUpdateObservationsFromRequest() throws Exception {
        FinishAppointmentRequest request = new FinishAppointmentRequest("Observação final", null, null);

        useCase.execute(APPOINTMENT_ID, request);

        assertThat(appointment.getObservations()).isEqualTo("Observação final");
    }

    @Test
    void shouldUpdatePaymentMethodFromRequest() throws Exception {
        FinishAppointmentRequest request = new FinishAppointmentRequest(null, null, PaymentMethod.PIX);

        useCase.execute(APPOINTMENT_ID, request);

        assertThat(appointment.getPaymentMethod()).isEqualTo(PaymentMethod.PIX);
    }

    @Test
    void shouldHandleNullRequestWithoutErrors() throws Exception {
        useCase.execute(APPOINTMENT_ID, null);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.FINALIZADO);
        verify(financialSyncService, never()).syncAppointmentPayment(any());
    }

    @Test
    void shouldThrowEntityNotFoundWhenAppointmentDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(appointmentRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(unknownId, null))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowForbiddenWhenAppointmentBelongsToDifferentTenant() throws Exception {
        when(tenantContext.getRequiredClinicId()).thenReturn(OTHER_TENANT_ID);

        assertThatThrownBy(() -> useCase.execute(APPOINTMENT_ID, null))
                .isInstanceOf(ForbiddenException.class);
    }

    @ParameterizedTest
    @EnumSource(value = AppointmentStatus.class, names = {"AGENDADO", "CONFIRMADO", "CANCELADO", "FINALIZADO", "NAO_COMPARECEU"})
    void shouldThrowInvalidStateWhenStatusIsNotEmAtendimento(AppointmentStatus invalidStatus) {
        appointment.setStatus(invalidStatus);

        assertThatThrownBy(() -> useCase.execute(APPOINTMENT_ID, null))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void shouldPersistAppointmentAfterFinishing() throws Exception {
        useCase.execute(APPOINTMENT_ID, null);

        verify(appointmentRepository).save(appointment);
    }
}
