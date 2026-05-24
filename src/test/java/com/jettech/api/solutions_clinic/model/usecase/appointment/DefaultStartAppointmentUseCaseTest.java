package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.exception.InvalidStateException;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.jettech.api.solutions_clinic.model.usecase.appointment.AppointmentFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultStartAppointmentUseCaseTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock TenantContext tenantContext;
    @Mock AppointmentResponseMapper mapper;

    @InjectMocks
    DefaultStartAppointmentUseCase useCase;

    private Appointment appointment;
    private AppointmentResponse expectedResponse;

    @BeforeEach
    void setUp() throws Exception {
        appointment = appointment();
        expectedResponse = appointmentResponse();

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(mapper.toResponse(any())).thenReturn(expectedResponse);
    }

    @Test
    void shouldStartAppointmentFromAgendadoStatus() throws Exception {
        appointment.setStatus(AppointmentStatus.AGENDADO);

        AppointmentResponse result = useCase.execute(APPOINTMENT_ID);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.EM_ATENDIMENTO);
    }

    @Test
    void shouldStartAppointmentFromConfirmadoStatus() throws Exception {
        appointment.setStatus(AppointmentStatus.CONFIRMADO);

        useCase.execute(APPOINTMENT_ID);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.EM_ATENDIMENTO);
    }

    @Test
    void shouldSetStartedAtTimestampOnStart() throws Exception {
        assertThat(appointment.getStartedAt()).isNull();

        useCase.execute(APPOINTMENT_ID);

        assertThat(appointment.getStartedAt()).isNotNull();
    }

    @Test
    void shouldThrowEntityNotFoundWhenAppointmentDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(appointmentRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(unknownId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowForbiddenWhenAppointmentBelongsToDifferentTenant() throws Exception {
        when(tenantContext.getRequiredClinicId()).thenReturn(OTHER_TENANT_ID);

        assertThatThrownBy(() -> useCase.execute(APPOINTMENT_ID))
                .isInstanceOf(ForbiddenException.class);
    }

    @ParameterizedTest
    @EnumSource(value = AppointmentStatus.class, names = {"CANCELADO", "FINALIZADO", "EM_ATENDIMENTO", "NAO_COMPARECEU"})
    void shouldThrowInvalidStateWhenStatusIsNotAllowedToStart(AppointmentStatus invalidStatus) {
        appointment.setStatus(invalidStatus);

        assertThatThrownBy(() -> useCase.execute(APPOINTMENT_ID))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void shouldPersistAppointmentAfterStarting() throws Exception {
        useCase.execute(APPOINTMENT_ID);

        verify(appointmentRepository).save(appointment);
    }
}
