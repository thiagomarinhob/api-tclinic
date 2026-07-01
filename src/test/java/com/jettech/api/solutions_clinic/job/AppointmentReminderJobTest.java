package com.jettech.api.solutions_clinic.job;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.service.WhatsAppNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentReminderJobTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 30, 10, 0, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);

    @Mock AppointmentRepository appointmentRepository;
    @Mock WhatsAppNotificationService whatsAppNotificationService;

    private AppointmentReminderJob job;

    @BeforeEach
    void setUp() {
        job = new AppointmentReminderJob(appointmentRepository, whatsAppNotificationService, FIXED_CLOCK);
    }

    private void stubWhatsAppSendSuccess() {
        when(whatsAppNotificationService.sendAppointmentReminderReturningMessageId(
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of("wamid.test"));
    }

    private Tenant tenantWithWindow(int confirmationWindowMinutes) {
        Tenant tenant = new Tenant();
        ReflectionTestUtils.setField(tenant, "id", UUID.randomUUID());
        tenant.setName("Clínica");
        tenant.setConfirmationWindowMinutes(confirmationWindowMinutes);
        return tenant;
    }

    private Appointment appointmentAt(Tenant tenant, LocalDateTime scheduledAt) {
        Appointment appointment = new Appointment();
        ReflectionTestUtils.setField(appointment, "id", UUID.randomUUID());
        appointment.setTenant(tenant);
        appointment.setScheduledAt(scheduledAt);
        appointment.setStatus(AppointmentStatus.AGENDADO);

        Patient patient = new Patient();
        ReflectionTestUtils.setField(patient, "id", UUID.randomUUID());
        patient.setFirstName("Maria");
        patient.setWhatsapp("11999999999");
        appointment.setPatient(patient);

        return appointment;
    }

    @Test
    void whenAppointmentIsWithinTenantOwnWindow_thenReminderIsSent() {
        stubWhatsAppSendSuccess();
        Tenant tenantA = tenantWithWindow(120);
        Appointment appointment = appointmentAt(tenantA, NOW.plusMinutes(120));

        when(appointmentRepository.findAppointmentsForReminder(any(), any(), eq(AppointmentStatus.AGENDADO)))
                .thenReturn(List.of(appointment));

        job.sendReminders();

        verify(whatsAppNotificationService).sendAppointmentReminderReturningMessageId(
                eq("11999999999"), any(), any(), any(), any(), any(), any());
        verify(appointmentRepository).save(appointment);
        assertThat(appointment.getReminderSentAt()).isNotNull();
    }

    @Test
    void whenTwoTenantsHaveDifferentWindowsInSameRun_thenEachIsEvaluatedAgainstItsOwnWindow() {
        stubWhatsAppSendSuccess();
        Tenant tenantA = tenantWithWindow(120);
        Tenant tenantB = tenantWithWindow(60);
        Appointment appointmentA = appointmentAt(tenantA, NOW.plusMinutes(120));
        Appointment appointmentB = appointmentAt(tenantB, NOW.plusMinutes(60));

        when(appointmentRepository.findAppointmentsForReminder(any(), any(), eq(AppointmentStatus.AGENDADO)))
                .thenReturn(List.of(appointmentA, appointmentB));

        job.sendReminders();

        verify(appointmentRepository).save(appointmentA);
        verify(appointmentRepository).save(appointmentB);
        assertThat(appointmentA.getReminderSentAt()).isNotNull();
        assertThat(appointmentB.getReminderSentAt()).isNotNull();
    }

    @Test
    void whenAppointmentIsOutsideTenantOwnWindow_thenReminderIsNotSentThisCycle() {
        Tenant tenantB = tenantWithWindow(60);
        // Tenant B's window target is 60min; this appointment is at 120min (tenant A's window), not tenant B's.
        Appointment appointment = appointmentAt(tenantB, NOW.plusMinutes(120));

        when(appointmentRepository.findAppointmentsForReminder(any(), any(), eq(AppointmentStatus.AGENDADO)))
                .thenReturn(List.of(appointment));

        job.sendReminders();

        verify(whatsAppNotificationService, never()).sendAppointmentReminderReturningMessageId(
                any(), any(), any(), any(), any(), any(), any());
        verify(appointmentRepository, never()).save(any());
        assertThat(appointment.getReminderSentAt()).isNull();
    }

    @Test
    void whenAppointmentIsExactlyAtLowerToleranceBoundary_thenReminderIsSent() {
        stubWhatsAppSendSuccess();
        Tenant tenant = tenantWithWindow(120);
        // target = NOW + 120min; lower tolerance boundary = target - 10min (inclusive).
        Appointment appointment = appointmentAt(tenant, NOW.plusMinutes(120).minusMinutes(10));

        when(appointmentRepository.findAppointmentsForReminder(any(), any(), eq(AppointmentStatus.AGENDADO)))
                .thenReturn(List.of(appointment));

        job.sendReminders();

        verify(appointmentRepository).save(appointment);
        assertThat(appointment.getReminderSentAt()).isNotNull();
    }

    @Test
    void whenAppointmentIsExactlyAtUpperToleranceBoundary_thenReminderIsSent() {
        stubWhatsAppSendSuccess();
        Tenant tenant = tenantWithWindow(120);
        // target = NOW + 120min; upper tolerance boundary = target + 10min (inclusive).
        Appointment appointment = appointmentAt(tenant, NOW.plusMinutes(120).plusMinutes(10));

        when(appointmentRepository.findAppointmentsForReminder(any(), any(), eq(AppointmentStatus.AGENDADO)))
                .thenReturn(List.of(appointment));

        job.sendReminders();

        verify(appointmentRepository).save(appointment);
        assertThat(appointment.getReminderSentAt()).isNotNull();
    }

    @Test
    void whenAppointmentIsJustOutsideToleranceBoundary_thenReminderIsNotSent() {
        Tenant tenant = tenantWithWindow(120);
        // target = NOW + 120min; 1min past the upper tolerance boundary (target + 10min).
        Appointment appointment = appointmentAt(tenant, NOW.plusMinutes(120).plusMinutes(11));

        when(appointmentRepository.findAppointmentsForReminder(any(), any(), eq(AppointmentStatus.AGENDADO)))
                .thenReturn(List.of(appointment));

        job.sendReminders();

        verify(appointmentRepository, never()).save(any());
        assertThat(appointment.getReminderSentAt()).isNull();
    }

    @Test
    void whenJobRuns_thenQueriesBroadRangeCoveringMinAndMaxTenantWindows() {
        when(appointmentRepository.findAppointmentsForReminder(any(), any(), eq(AppointmentStatus.AGENDADO)))
                .thenReturn(List.of());

        job.sendReminders();

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(appointmentRepository, times(1)).findAppointmentsForReminder(
                startCaptor.capture(), endCaptor.capture(), eq(AppointmentStatus.AGENDADO));

        assertThat(startCaptor.getValue())
                .isEqualTo(NOW.plusMinutes(Tenant.MIN_CONFIRMATION_WINDOW_MINUTES - 10));
        assertThat(endCaptor.getValue())
                .isEqualTo(NOW.plusMinutes(Tenant.MAX_CONFIRMATION_WINDOW_MINUTES + 10));
    }

    @Test
    void whenReminderIsSentSuccessfully_thenConfirmationCodeIsGeneratedAndPersisted() {
        stubWhatsAppSendSuccess();
        Tenant tenant = tenantWithWindow(120);
        Appointment appointment = appointmentAt(tenant, NOW.plusMinutes(120));

        when(appointmentRepository.findAppointmentsForReminder(any(), any(), eq(AppointmentStatus.AGENDADO)))
                .thenReturn(List.of(appointment));
        when(appointmentRepository.existsByConfirmationCodeAndStatusIn(anyString(), anyList()))
                .thenReturn(false);

        job.sendReminders();

        assertThat(appointment.getConfirmationCode()).isNotNull();
        assertThat(appointment.getConfirmationCode()).matches("C\\d{3}");

        verify(whatsAppNotificationService).sendAppointmentReminderReturningMessageId(
                eq("11999999999"), any(), any(), any(), any(), any(), eq(appointment.getConfirmationCode()));
    }

    @Test
    void whenGeneratedCodeCollidesWithAnActiveAppointment_thenRetriesUntilCodeIsFree() {
        stubWhatsAppSendSuccess();
        Tenant tenant = tenantWithWindow(120);
        Appointment appointment = appointmentAt(tenant, NOW.plusMinutes(120));

        when(appointmentRepository.findAppointmentsForReminder(any(), any(), eq(AppointmentStatus.AGENDADO)))
                .thenReturn(List.of(appointment));
        when(appointmentRepository.existsByConfirmationCodeAndStatusIn(anyString(), anyList()))
                .thenReturn(true, false);

        job.sendReminders();

        assertThat(appointment.getConfirmationCode()).isNotNull();
        verify(appointmentRepository, times(2)).existsByConfirmationCodeAndStatusIn(anyString(), anyList());
    }

    @Test
    void whenCodeGenerationAttemptsAreExhausted_thenReminderIsNotSent() {
        Tenant tenant = tenantWithWindow(120);
        Appointment appointment = appointmentAt(tenant, NOW.plusMinutes(120));

        when(appointmentRepository.findAppointmentsForReminder(any(), any(), eq(AppointmentStatus.AGENDADO)))
                .thenReturn(List.of(appointment));
        when(appointmentRepository.existsByConfirmationCodeAndStatusIn(anyString(), anyList()))
                .thenReturn(true);

        job.sendReminders();

        assertThat(appointment.getConfirmationCode()).isNull();
        verify(whatsAppNotificationService, never()).sendAppointmentReminderReturningMessageId(
                any(), any(), any(), any(), any(), any(), any());
    }
}
