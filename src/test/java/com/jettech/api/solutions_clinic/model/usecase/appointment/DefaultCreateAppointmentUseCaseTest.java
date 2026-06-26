package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.AppointmentConflictException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.*;
import com.jettech.api.solutions_clinic.model.repository.*;
import com.jettech.api.solutions_clinic.model.service.AppointmentEmailService;
import com.jettech.api.solutions_clinic.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.jettech.api.solutions_clinic.model.usecase.appointment.AppointmentFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultCreateAppointmentUseCaseTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock AvailabilityConflictChecker availabilityConflictChecker;
    @Mock PatientRepository patientRepository;
    @Mock ProfessionalRepository professionalRepository;
    @Mock RoomRepository roomRepository;
    @Mock UserRepository userRepository;
    @Mock TenantRepository tenantRepository;
    @Mock ProfessionalScheduleValidator professionalScheduleValidator;
    @Mock ProcedureLoader procedureLoader;
    @Mock AppointmentResponseMapper appointmentResponseMapper;
    @Mock TenantContext tenantContext;
    @Mock AppointmentEmailService appointmentEmailService;

    @InjectMocks
    DefaultCreateAppointmentUseCase useCase;

    private Tenant tenant;
    private Patient patient;
    private Professional professional;
    private User createdBy;
    private Appointment savedAppointment;
    private AppointmentResponse expectedResponse;

    @BeforeEach
    void setUp() throws Exception {
        tenant = tenant();
        patient = patient();
        professional = professional(tenant);
        createdBy = user();
        savedAppointment = appointment(tenant, patient, professional);
        expectedResponse = appointmentResponse();

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(professionalRepository.findById(PROFESSIONAL_ID)).thenReturn(Optional.of(professional));
        when(userRepository.findById(CREATED_BY_ID)).thenReturn(Optional.of(createdBy));
        when(availabilityConflictChecker.findConflict(any(), anyInt(), isNull(), any(), any())).thenReturn(null);
        when(appointmentRepository.save(any())).thenReturn(savedAppointment);
        when(appointmentResponseMapper.toResponse(any())).thenReturn(expectedResponse);
    }

    @Test
    void shouldCreateAppointmentSuccessfullyWithoutProcedures() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                SCHEDULED_AT, DURATION_MINUTES, null,
                TOTAL_VALUE, null, CREATED_BY_ID, false
        );

        AppointmentResponse result = useCase.execute(request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(appointmentRepository).save(any(Appointment.class));
        verify(appointmentEmailService).sendConfirmation(any(Appointment.class));
    }

    @Test
    void shouldCreateAppointmentWithRoomWhenRoomIdProvided() throws Exception {
        Room room = room();
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, ROOM_ID,
                SCHEDULED_AT, DURATION_MINUTES, null,
                TOTAL_VALUE, null, CREATED_BY_ID, false
        );

        useCase.execute(request);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        assertThat(captor.getValue().getRoom()).isEqualTo(room);
    }

    @Test
    void shouldCreateAppointmentWithoutRoomWhenRoomIdIsNull() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                SCHEDULED_AT, DURATION_MINUTES, null,
                TOTAL_VALUE, null, CREATED_BY_ID, false
        );

        useCase.execute(request);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        assertThat(captor.getValue().getRoom()).isNull();
    }

    @Test
    void shouldUseProcedureDurationAndValueWhenProceduresProvided() throws Exception {
        Procedure procedure = procedure();
        List<UUID> procedureIds = List.of(PROCEDURE_ID);
        ProcedureLoadResult loadResult = new ProcedureLoadResult(
                List.of(procedure), 45, new BigDecimal("200.00")
        );
        when(procedureLoader.loadAndValidate(procedureIds, TENANT_ID)).thenReturn(loadResult);

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                SCHEDULED_AT, DURATION_MINUTES, null,
                BigDecimal.ZERO, procedureIds, CREATED_BY_ID, false
        );

        useCase.execute(request);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository, atLeastOnce()).save(captor.capture());
        Appointment saved = captor.getAllValues().get(0);
        assertThat(saved.getDurationMinutes()).isEqualTo(45);
        assertThat(saved.getTotalValue()).isEqualByComparingTo("200.00");
    }

    @Test
    void shouldUseExplicitTotalValueWhenProvidedAlongsideProcedures() throws Exception {
        Procedure procedure = procedure();
        List<UUID> procedureIds = List.of(PROCEDURE_ID);
        ProcedureLoadResult loadResult = new ProcedureLoadResult(
                List.of(procedure), 45, new BigDecimal("200.00")
        );
        when(procedureLoader.loadAndValidate(procedureIds, TENANT_ID)).thenReturn(loadResult);

        BigDecimal explicitValue = new BigDecimal("300.00");
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                SCHEDULED_AT, DURATION_MINUTES, null,
                explicitValue, procedureIds, CREATED_BY_ID, false
        );

        useCase.execute(request);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getTotalValue()).isEqualByComparingTo("300.00");
    }

    @Test
    void shouldSetInitialStatusAsAgendadoAndPaymentAsPendente() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                SCHEDULED_AT, DURATION_MINUTES, null,
                TOTAL_VALUE, null, CREATED_BY_ID, false
        );

        useCase.execute(request);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AppointmentStatus.AGENDADO);
        assertThat(captor.getValue().getPaymentStatus()).isEqualTo(PaymentStatus.PENDENTE);
    }

    @Test
    void shouldThrowEntityNotFoundWhenTenantNotFound() {
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                SCHEDULED_AT, DURATION_MINUTES, null,
                TOTAL_VALUE, null, CREATED_BY_ID, false
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowEntityNotFoundWhenPatientNotFound() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.empty());

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                SCHEDULED_AT, DURATION_MINUTES, null,
                TOTAL_VALUE, null, CREATED_BY_ID, false
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowEntityNotFoundWhenProfessionalNotFound() {
        when(professionalRepository.findById(PROFESSIONAL_ID)).thenReturn(Optional.empty());

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                SCHEDULED_AT, DURATION_MINUTES, null,
                TOTAL_VALUE, null, CREATED_BY_ID, false
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowEntityNotFoundWhenRoomNotFound() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, ROOM_ID,
                SCHEDULED_AT, DURATION_MINUTES, null,
                TOTAL_VALUE, null, CREATED_BY_ID, false
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowEntityNotFoundWhenCreatedByUserNotFound() {
        when(userRepository.findById(CREATED_BY_ID)).thenReturn(Optional.empty());

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                SCHEDULED_AT, DURATION_MINUTES, null,
                TOTAL_VALUE, null, CREATED_BY_ID, false
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowEntityNotFoundWhenProfessionalHasNoScheduleForDay() {
        doThrow(new EntityNotFoundException("Agenda", "segunda-feira"))
                .when(professionalScheduleValidator)
                .validate(any(), any(), anyInt());

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                SCHEDULED_AT, DURATION_MINUTES, null,
                TOTAL_VALUE, null, CREATED_BY_ID, false
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowAppointmentConflictWhenProfessionalHasConflictAndForceScheduleIsFalse() {
        when(availabilityConflictChecker.findConflict(any(), anyInt(), isNull(), any(), any()))
                .thenReturn("Conflito de horário");

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                SCHEDULED_AT, DURATION_MINUTES, null,
                TOTAL_VALUE, null, CREATED_BY_ID, false
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessageContaining("Conflito de horário");
    }

    @Test
    void shouldCreateAppointmentWhenConflictExistsButForceScheduleIsTrue() throws Exception {
        when(availabilityConflictChecker.findConflict(any(), anyInt(), isNull(), any(), any()))
                .thenReturn("Conflito de horário");

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                SCHEDULED_AT, DURATION_MINUTES, null,
                TOTAL_VALUE, null, CREATED_BY_ID, true
        );

        AppointmentResponse result = useCase.execute(request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldSendConfirmationEmailAfterSuccessfulCreation() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                SCHEDULED_AT, DURATION_MINUTES, null,
                TOTAL_VALUE, null, CREATED_BY_ID, false
        );

        useCase.execute(request);

        verify(appointmentEmailService).sendConfirmation(savedAppointment);
    }

    @Test
    void shouldSaveTwiceWhenProceduresAreProvided() throws Exception {
        Procedure procedure = procedure();
        List<UUID> procedureIds = List.of(PROCEDURE_ID);
        ProcedureLoadResult loadResult = new ProcedureLoadResult(
                List.of(procedure), 30, new BigDecimal("150.00")
        );
        when(procedureLoader.loadAndValidate(procedureIds, TENANT_ID)).thenReturn(loadResult);

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                SCHEDULED_AT, DURATION_MINUTES, null,
                BigDecimal.ZERO, procedureIds, CREATED_BY_ID, false
        );

        useCase.execute(request);

        // Salva duas vezes: ao criar e ao vincular os procedimentos
        verify(appointmentRepository, times(2)).save(any(Appointment.class));
    }
}
