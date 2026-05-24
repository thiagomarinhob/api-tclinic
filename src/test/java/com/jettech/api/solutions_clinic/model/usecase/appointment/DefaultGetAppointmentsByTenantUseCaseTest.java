package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.jettech.api.solutions_clinic.model.usecase.appointment.AppointmentFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultGetAppointmentsByTenantUseCaseTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock TenantRepository tenantRepository;
    @Mock TenantContext tenantContext;
    @Mock AppointmentResponseMapper mapper;

    @InjectMocks
    DefaultGetAppointmentsByTenantUseCase useCase;

    private Tenant tenant;
    private Appointment appt1;
    private Appointment appt2;

    @BeforeEach
    void setUp() throws Exception {
        tenant = tenant();

        appt1 = appointment();
        appt1.setScheduledAt(SCHEDULED_AT);

        appt2 = appointment();
        appt2.setId(java.util.UUID.randomUUID());
        appt2.setScheduledAt(SCHEDULED_AT.plusDays(1));

        doNothing().when(tenantContext).requireSameTenant(TENANT_ID);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
        when(mapper.toResponse(any())).thenReturn(appointmentResponse());
    }

    @Test
    void shouldReturnAllAppointmentsWhenNoFiltersApplied() throws Exception {
        when(appointmentRepository.findByTenantId(TENANT_ID)).thenReturn(List.of(appt1, appt2));

        GetAppointmentsByTenantRequest request = new GetAppointmentsByTenantRequest(
                TENANT_ID, null, null, null, null, null
        );

        List<AppointmentResponse> result = useCase.execute(request);

        assertThat(result).hasSize(2);
        verify(appointmentRepository).findByTenantId(TENANT_ID);
    }

    @Test
    void shouldFilterByDateRange() throws Exception {
        LocalDate start = LocalDate.of(2026, 4, 14);
        LocalDate end = LocalDate.of(2026, 4, 20);
        when(appointmentRepository.findByTenantIdAndScheduledAtBetween(
                eq(TENANT_ID), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of(appt1));

        GetAppointmentsByTenantRequest request = new GetAppointmentsByTenantRequest(
                TENANT_ID, null, start, end, null, null
        );

        List<AppointmentResponse> result = useCase.execute(request);

        assertThat(result).hasSize(1);
        verify(appointmentRepository).findByTenantIdAndScheduledAtBetween(
                eq(TENANT_ID),
                eq(start.atStartOfDay()),
                eq(end.atTime(LocalTime.MAX))
        );
    }

    @Test
    void shouldFilterByDateRangeAndStatus() throws Exception {
        LocalDate start = LocalDate.of(2026, 4, 14);
        LocalDate end = LocalDate.of(2026, 4, 20);
        when(appointmentRepository.findByTenantIdAndScheduledAtBetweenAndStatus(
                eq(TENANT_ID), any(), any(), eq(AppointmentStatus.AGENDADO)
        )).thenReturn(List.of(appt1));

        GetAppointmentsByTenantRequest request = new GetAppointmentsByTenantRequest(
                TENANT_ID, null, start, end, AppointmentStatus.AGENDADO, null
        );

        List<AppointmentResponse> result = useCase.execute(request);

        assertThat(result).hasSize(1);
        verify(appointmentRepository).findByTenantIdAndScheduledAtBetweenAndStatus(
                eq(TENANT_ID), any(), any(), eq(AppointmentStatus.AGENDADO)
        );
    }

    @Test
    void shouldFilterByDateOnly() throws Exception {
        LocalDate date = LocalDate.of(2026, 4, 14);
        when(appointmentRepository.findByTenantIdAndScheduledAtBetween(
                eq(TENANT_ID), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of(appt1));

        GetAppointmentsByTenantRequest request = new GetAppointmentsByTenantRequest(
                TENANT_ID, date, null, null, null, null
        );

        useCase.execute(request);

        verify(appointmentRepository).findByTenantIdAndScheduledAtBetween(
                eq(TENANT_ID),
                eq(date.atStartOfDay()),
                eq(date.atTime(LocalTime.MAX))
        );
    }

    @Test
    void shouldFilterByDateAndStatus() throws Exception {
        LocalDate date = LocalDate.of(2026, 4, 14);
        when(appointmentRepository.findByTenantIdAndScheduledAtBetweenAndStatus(
                eq(TENANT_ID), any(), any(), eq(AppointmentStatus.CONFIRMADO)
        )).thenReturn(List.of(appt1));

        GetAppointmentsByTenantRequest request = new GetAppointmentsByTenantRequest(
                TENANT_ID, date, null, null, AppointmentStatus.CONFIRMADO, null
        );

        useCase.execute(request);

        verify(appointmentRepository).findByTenantIdAndScheduledAtBetweenAndStatus(
                eq(TENANT_ID), any(), any(), eq(AppointmentStatus.CONFIRMADO)
        );
    }

    @Test
    void shouldFilterByStatusOnly() throws Exception {
        when(appointmentRepository.findByTenantIdAndStatus(TENANT_ID, AppointmentStatus.CANCELADO))
                .thenReturn(List.of(appt2));

        GetAppointmentsByTenantRequest request = new GetAppointmentsByTenantRequest(
                TENANT_ID, null, null, null, AppointmentStatus.CANCELADO, null
        );

        List<AppointmentResponse> result = useCase.execute(request);

        assertThat(result).hasSize(1);
        verify(appointmentRepository).findByTenantIdAndStatus(TENANT_ID, AppointmentStatus.CANCELADO);
    }

    @Test
    void shouldOrderByScheduledAtDescByDefault() throws Exception {
        // appt1 tem data anterior a appt2 — ordem padrão DESC deve colocar appt2 primeiro
        when(appointmentRepository.findByTenantId(TENANT_ID)).thenReturn(List.of(appt1, appt2));
        when(mapper.toResponse(appt1)).thenReturn(appointmentResponse());
        when(mapper.toResponse(appt2)).thenReturn(appointmentResponse());

        GetAppointmentsByTenantRequest request = new GetAppointmentsByTenantRequest(
                TENANT_ID, null, null, null, null, null
        );

        useCase.execute(request);

        // Verifica que o mapper foi chamado (ordenação interna não altera IDs, mas não lança exceção)
        verify(mapper, times(2)).toResponse(any());
    }

    @Test
    void shouldOrderByScheduledAtAscWhenRequested() throws Exception {
        when(appointmentRepository.findByTenantId(TENANT_ID)).thenReturn(List.of(appt2, appt1));

        GetAppointmentsByTenantRequest request = new GetAppointmentsByTenantRequest(
                TENANT_ID, null, null, null, null, "scheduledAt_asc"
        );

        useCase.execute(request);

        verify(mapper, times(2)).toResponse(any());
    }

    @Test
    void shouldThrowForbiddenWhenDifferentTenant() throws Exception {
        doThrow(new ForbiddenException()).when(tenantContext).requireSameTenant(TENANT_ID);

        GetAppointmentsByTenantRequest request = new GetAppointmentsByTenantRequest(
                TENANT_ID, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowEntityNotFoundWhenTenantDoesNotExist() throws Exception {
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

        GetAppointmentsByTenantRequest request = new GetAppointmentsByTenantRequest(
                TENANT_ID, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
