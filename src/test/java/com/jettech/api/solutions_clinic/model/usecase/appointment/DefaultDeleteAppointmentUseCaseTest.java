package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.service.AppointmentEmailService;
import com.jettech.api.solutions_clinic.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class DefaultDeleteAppointmentUseCaseTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock TenantContext tenantContext;
    @Mock AppointmentEmailService appointmentEmailService;

    @InjectMocks
    DefaultDeleteAppointmentUseCase useCase;

    private Appointment appointment;

    @BeforeEach
    void setUp() throws Exception {
        appointment = appointment();
        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
    }

    @Test
    void shouldCancelAppointmentInsteadOfDeleting() throws Exception {
        useCase.execute(new CancelAppointmentRequest(APPOINTMENT_ID, null));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELADO);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldSetCancelledAtTimestampOnCancellation() throws Exception {
        useCase.execute(new CancelAppointmentRequest(APPOINTMENT_ID, null));

        assertThat(appointment.getCancelledAt()).isNotNull();
    }

    @Test
    void shouldSendCancellationEmailAfterCancellation() throws Exception {
        useCase.execute(new CancelAppointmentRequest(APPOINTMENT_ID, null));

        verify(appointmentEmailService).sendCancellation(appointment);
    }

    @Test
    void shouldThrowEntityNotFoundWhenAppointmentDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(appointmentRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new CancelAppointmentRequest(unknownId, null)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowForbiddenWhenAppointmentBelongsToDifferentTenant() throws Exception {
        when(tenantContext.getRequiredClinicId()).thenReturn(OTHER_TENANT_ID);

        assertThatThrownBy(() -> useCase.execute(new CancelAppointmentRequest(APPOINTMENT_ID, null)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldNotSendEmailWhenAppointmentNotFound() {
        when(appointmentRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new CancelAppointmentRequest(UUID.randomUUID(), null)));

        verify(appointmentEmailService, never()).sendCancellation(any());
    }
}
