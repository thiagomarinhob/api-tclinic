package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.jettech.api.solutions_clinic.model.usecase.appointment.AppointmentFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultGetAppointmentByIdUseCaseTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock TenantContext tenantContext;
    @Mock AppointmentResponseMapper mapper;

    @InjectMocks
    DefaultGetAppointmentByIdUseCase useCase;

    @Test
    void shouldReturnAppointmentResponseWhenFoundAndTenantMatches() throws Exception {
        Appointment appointment = appointment();
        AppointmentResponse expected = appointmentResponse();

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(mapper.toResponse(appointment)).thenReturn(expected);

        AppointmentResponse result = useCase.execute(APPOINTMENT_ID);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldThrowEntityNotFoundWhenAppointmentDoesNotExist() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(appointmentRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(unknownId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowForbiddenWhenAppointmentBelongsToDifferentTenant() throws Exception {
        Appointment appointment = appointment();

        when(tenantContext.getRequiredClinicId()).thenReturn(OTHER_TENANT_ID);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> useCase.execute(APPOINTMENT_ID))
                .isInstanceOf(ForbiddenException.class);
    }
}
