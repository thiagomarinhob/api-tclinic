package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.AppointmentConflictException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.exception.InvalidStateException;
import com.jettech.api.solutions_clinic.model.entity.*;
import com.jettech.api.solutions_clinic.model.repository.*;
import com.jettech.api.solutions_clinic.model.service.AppointmentEmailService;
import com.jettech.api.solutions_clinic.model.service.FinancialSyncService;
import com.jettech.api.solutions_clinic.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.jettech.api.solutions_clinic.model.usecase.appointment.AppointmentFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultUpdateAppointmentUseCaseTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock AvailabilityConflictChecker availabilityConflictChecker;
    @Mock PatientRepository patientRepository;
    @Mock ProfessionalRepository professionalRepository;
    @Mock RoomRepository roomRepository;
    @Mock ProfessionalScheduleRepository professionalScheduleRepository;
    @Mock FinancialSyncService financialSyncService;
    @Mock TenantContext tenantContext;
    @Mock AppointmentEmailService appointmentEmailService;
    @Mock ProcedureLoader procedureLoader;
    @Mock AppointmentResponseMapper mapper;

    @InjectMocks
    DefaultUpdateAppointmentUseCase useCase;

    private Appointment appointment;
    private AppointmentResponse expectedResponse;

    @BeforeEach
    void setUp() throws Exception {
        Tenant tenant = tenant();
        appointment = appointment(tenant, patient(), professional(tenant));
        expectedResponse = appointmentResponse();

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(mapper.toResponse(any())).thenReturn(expectedResponse);
    }

    @Test
    void shouldUpdateObservationsSuccessfully() throws Exception {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null, null, null,
                "Nova observação", null, null, null, null, null
        );

        AppointmentResponse result = useCase.execute(request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(appointment.getObservations()).isEqualTo("Nova observação");
        verify(appointmentEmailService).sendUpdate(appointment);
    }

    @Test
    void shouldThrowEntityNotFoundWhenAppointmentDoesNotExist() {
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.empty());

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null, null, null,
                null, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowForbiddenWhenAppointmentBelongsToDifferentTenant() throws Exception {
        when(tenantContext.getRequiredClinicId()).thenReturn(OTHER_TENANT_ID);

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null, null, null,
                null, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowInvalidStateWhenAppointmentIsCancelled() {
        appointment.setStatus(AppointmentStatus.CANCELADO);

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null, null, null,
                null, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void shouldThrowInvalidStateWhenAppointmentIsFinished() {
        appointment.setStatus(AppointmentStatus.FINALIZADO);

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null, null, null,
                null, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void shouldUpdatePatientWhenPatientIdProvided() throws Exception {
        Patient newPatient = new Patient();
        newPatient.setId(UUID.randomUUID());
        when(patientRepository.findById(newPatient.getId())).thenReturn(Optional.of(newPatient));

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, newPatient.getId(), null, null, null, null,
                null, null, null, null, null, null
        );

        useCase.execute(request);

        assertThat(appointment.getPatient()).isEqualTo(newPatient);
    }

    @Test
    void shouldThrowEntityNotFoundWhenNewPatientNotFound() {
        UUID unknownPatient = UUID.randomUUID();
        when(patientRepository.findById(unknownPatient)).thenReturn(Optional.empty());

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, unknownPatient, null, null, null, null,
                null, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldValidateScheduleAndCheckConflictWhenScheduledAtChanges() throws Exception {
        ProfessionalSchedule schedule = new ProfessionalSchedule();
        when(professionalScheduleRepository.findByProfessionalIdAndDayOfWeek(any(), any()))
                .thenReturn(Optional.of(schedule));
        when(availabilityConflictChecker.findConflict(any(), anyInt(), eq(APPOINTMENT_ID), any(), any()))
                .thenReturn(null);

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null,
                SCHEDULED_AT.plusDays(1), null,
                null, null, null, null, null, null
        );

        useCase.execute(request);

        verify(professionalScheduleRepository).findByProfessionalIdAndDayOfWeek(any(), any());
        verify(availabilityConflictChecker).findConflict(any(), anyInt(), eq(APPOINTMENT_ID), any(), any());
    }

    @Test
    void shouldThrowConflictWhenNewTimeHasConflictAndForceScheduleIsFalse() {
        ProfessionalSchedule schedule = new ProfessionalSchedule();
        when(professionalScheduleRepository.findByProfessionalIdAndDayOfWeek(any(), any()))
                .thenReturn(Optional.of(schedule));
        when(availabilityConflictChecker.findConflict(any(), anyInt(), any(), any(), any()))
                .thenReturn("Conflito detectado");

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null,
                SCHEDULED_AT.plusDays(1), null,
                null, null, null, null, null, false
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(AppointmentConflictException.class);
    }

    @Test
    void shouldAllowUpdateWithForceScheduleEvenWithConflict() throws Exception {
        ProfessionalSchedule schedule = new ProfessionalSchedule();
        when(professionalScheduleRepository.findByProfessionalIdAndDayOfWeek(any(), any()))
                .thenReturn(Optional.of(schedule));
        when(availabilityConflictChecker.findConflict(any(), anyInt(), any(), any(), any()))
                .thenReturn("Conflito detectado");

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null,
                SCHEDULED_AT.plusDays(1), null,
                null, null, null, null, null, true
        );

        useCase.execute(request);

        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldSetPaidAtWhenPaymentStatusChangesToPago() throws Exception {
        appointment.setPaidAt(null);

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null, null, null,
                null, null, null, PaymentStatus.PAGO, null, null
        );

        useCase.execute(request);

        assertThat(appointment.getPaidAt()).isNotNull();
        assertThat(appointment.getPaymentStatus()).isEqualTo(PaymentStatus.PAGO);
    }

    @Test
    void shouldClearPaidAtWhenPaymentStatusChangesToCancelled() throws Exception {
        appointment.setPaidAt(LocalDateTime.now());

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null, null, null,
                null, null, null, PaymentStatus.CANCELADO, null, null
        );

        useCase.execute(request);

        assertThat(appointment.getPaidAt()).isNull();
    }

    @Test
    void shouldSyncFinancialWhenPaymentChangesFromPendenteToPago() throws Exception {
        appointment.setPaymentStatus(PaymentStatus.PENDENTE);

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null, null, null,
                null, null, null, PaymentStatus.PAGO, null, null
        );

        useCase.execute(request);

        verify(financialSyncService).syncAppointmentPayment(appointment);
    }

    @Test
    void shouldNotSyncFinancialWhenPaymentWasAlreadyPago() throws Exception {
        appointment.setPaymentStatus(PaymentStatus.PAGO);
        appointment.setPaidAt(LocalDateTime.now());

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null, null, null,
                null, null, null, PaymentStatus.PAGO, null, null
        );

        useCase.execute(request);

        verify(financialSyncService, never()).syncAppointmentPayment(any());
    }

    @Test
    void shouldUpdateProceduresAndRecalculateTotalValue() throws Exception {
        Procedure procedure = procedure();
        List<UUID> procedureIds = List.of(PROCEDURE_ID);
        ProcedureLoadResult loadResult = new ProcedureLoadResult(
                List.of(procedure), 45, new BigDecimal("250.00")
        );
        when(procedureLoader.loadAndValidate(procedureIds, TENANT_ID)).thenReturn(loadResult);

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null, null, null,
                null, procedureIds, null, null, null, null
        );

        useCase.execute(request);

        assertThat(appointment.getTotalValue()).isEqualByComparingTo("250.00");
    }

    @Test
    void shouldZeroTotalValueWhenProcedureIdsListIsEmpty() throws Exception {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null, null, null,
                null, List.of(), null, null, null, null
        );

        useCase.execute(request);

        assertThat(appointment.getTotalValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldOverrideProcedureTotalValueWhenExplicitValueProvided() throws Exception {
        Procedure procedure = procedure();
        List<UUID> procedureIds = List.of(PROCEDURE_ID);
        ProcedureLoadResult loadResult = new ProcedureLoadResult(
                List.of(procedure), 45, new BigDecimal("250.00")
        );
        when(procedureLoader.loadAndValidate(procedureIds, TENANT_ID)).thenReturn(loadResult);

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null, null, null,
                null, procedureIds, new BigDecimal("400.00"), null, null, null
        );

        useCase.execute(request);

        assertThat(appointment.getTotalValue()).isEqualByComparingTo("400.00");
    }

    @Test
    void shouldUpdatePaymentMethodWhenProvided() throws Exception {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null, null, null,
                null, null, null, null, PaymentMethod.PIX, null
        );

        useCase.execute(request);

        assertThat(appointment.getPaymentMethod()).isEqualTo(PaymentMethod.PIX);
    }

    @Test
    void shouldSendUpdateEmailAfterUpdate() throws Exception {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                APPOINTMENT_ID, null, null, null, null, null,
                null, null, null, null, null, null
        );

        useCase.execute(request);

        verify(appointmentEmailService).sendUpdate(appointment);
    }
}
