package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.Professional;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.jettech.api.solutions_clinic.model.usecase.appointment.AppointmentFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultGetAppointmentsByProfessionalIdUseCaseTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock ProfessionalRepository professionalRepository;
    @Mock TenantContext tenantContext;
    @Mock AppointmentResponseMapper mapper;

    @InjectMocks
    DefaultGetAppointmentsByProfessionalIdUseCase useCase;

    private Professional professional;
    private Appointment appointment;

    @BeforeEach
    void setUp() throws Exception {
        Tenant tenant = tenant();
        professional = professional(tenant);
        appointment = appointment(tenant, patient(), professional);

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(professionalRepository.findById(PROFESSIONAL_ID)).thenReturn(Optional.of(professional));
        when(mapper.toResponse(any())).thenReturn(appointmentResponse());
    }

    @Test
    void shouldReturnAllAppointmentsWhenNoDateRangeProvided() throws Exception {
        when(appointmentRepository.findByProfessionalId(PROFESSIONAL_ID))
                .thenReturn(List.of(appointment));

        GetAppointmentsByProfessionalIdRequest request = new GetAppointmentsByProfessionalIdRequest(
                PROFESSIONAL_ID, null, null
        );

        List<AppointmentResponse> result = useCase.execute(request);

        assertThat(result).hasSize(1);
        verify(appointmentRepository).findByProfessionalId(PROFESSIONAL_ID);
    }

    @Test
    void shouldFilterByDateRangeWhenBothDatesProvided() throws Exception {
        LocalDate start = LocalDate.of(2026, 4, 14);
        LocalDate end = LocalDate.of(2026, 4, 20);
        when(appointmentRepository.findByProfessionalIdAndScheduledAtBetween(
                eq(PROFESSIONAL_ID), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of(appointment));

        GetAppointmentsByProfessionalIdRequest request = new GetAppointmentsByProfessionalIdRequest(
                PROFESSIONAL_ID, start, end
        );

        useCase.execute(request);

        verify(appointmentRepository).findByProfessionalIdAndScheduledAtBetween(
                eq(PROFESSIONAL_ID),
                eq(start.atStartOfDay()),
                eq(end.atTime(LocalTime.MAX))
        );
    }

    @Test
    void shouldThrowEntityNotFoundWhenProfessionalDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(professionalRepository.findById(unknownId)).thenReturn(Optional.empty());

        GetAppointmentsByProfessionalIdRequest request = new GetAppointmentsByProfessionalIdRequest(
                unknownId, null, null
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowForbiddenWhenProfessionalBelongsToDifferentTenant() throws Exception {
        when(tenantContext.getRequiredClinicId()).thenReturn(OTHER_TENANT_ID);

        GetAppointmentsByProfessionalIdRequest request = new GetAppointmentsByProfessionalIdRequest(
                PROFESSIONAL_ID, null, null
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldReturnEmptyListWhenNoAppointmentsFound() throws Exception {
        when(appointmentRepository.findByProfessionalId(PROFESSIONAL_ID)).thenReturn(List.of());

        GetAppointmentsByProfessionalIdRequest request = new GetAppointmentsByProfessionalIdRequest(
                PROFESSIONAL_ID, null, null
        );

        List<AppointmentResponse> result = useCase.execute(request);

        assertThat(result).isEmpty();
    }
}
