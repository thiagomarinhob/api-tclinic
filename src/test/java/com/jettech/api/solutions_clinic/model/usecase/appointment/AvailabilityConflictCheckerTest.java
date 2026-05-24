package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.jettech.api.solutions_clinic.model.usecase.appointment.AppointmentFixtures.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Testa o algoritmo de detecção de sobreposição de horários.
 * Classe pura sem Spring — instanciada diretamente.
 */
class AvailabilityConflictCheckerTest {

    private AvailabilityConflictChecker checker;

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 4, 14, 10, 0);
    private static final String CONFLICT_MSG = "Conflito de horário detectado";

    @BeforeEach
    void setUp() {
        checker = new AvailabilityConflictChecker();
    }

    @Test
    void shouldReturnNullWhenNoExistingAppointments() {
        String result = checker.findConflict(
                BASE_TIME, 30, null, (s, e) -> List.of(), CONFLICT_MSG
        );

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnConflictMessageWhenAppointmentsOverlap() {
        // Novo: 10:00-10:30 / Existente: 10:15-10:45
        Appointment existing = buildAppointment(UUID.randomUUID(), BASE_TIME.plusMinutes(15), 30);

        String result = checker.findConflict(
                BASE_TIME, 30, null, (s, e) -> List.of(existing), CONFLICT_MSG
        );

        assertThat(result).isEqualTo(CONFLICT_MSG);
    }

    @Test
    void shouldReturnConflictWhenNewAppointmentEncompassesExistingOne() {
        // Novo: 10:00-11:00 / Existente: 10:15-10:45
        Appointment existing = buildAppointment(UUID.randomUUID(), BASE_TIME.plusMinutes(15), 30);

        String result = checker.findConflict(
                BASE_TIME, 60, null, (s, e) -> List.of(existing), CONFLICT_MSG
        );

        assertThat(result).isEqualTo(CONFLICT_MSG);
    }

    @Test
    void shouldReturnNullWhenNewAppointmentEndsExactlyWhenExistingStarts() {
        // Novo: 09:30-10:00 / Existente: 10:00-10:30 → sem sobreposição
        Appointment existing = buildAppointment(UUID.randomUUID(), BASE_TIME, 30);

        String result = checker.findConflict(
                BASE_TIME.minusMinutes(30), 30, null, (s, e) -> List.of(existing), CONFLICT_MSG
        );

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullWhenNewAppointmentStartsExactlyWhenExistingEnds() {
        // Existente: 09:30-10:00 / Novo: 10:00-10:30 → sem sobreposição
        Appointment existing = buildAppointment(UUID.randomUUID(), BASE_TIME.minusMinutes(30), 30);

        String result = checker.findConflict(
                BASE_TIME, 30, null, (s, e) -> List.of(existing), CONFLICT_MSG
        );

        assertThat(result).isNull();
    }

    @Test
    void shouldIgnoreExcludedAppointmentId() {
        UUID targetId = UUID.randomUUID();
        Appointment existing = buildAppointment(targetId, BASE_TIME, 30);

        String result = checker.findConflict(
                BASE_TIME, 30, targetId, (s, e) -> List.of(existing), CONFLICT_MSG
        );

        assertThat(result).isNull();
    }

    @Test
    void shouldDetectConflictAmongMultipleAppointmentsWhenOneOverlaps() {
        // Dois agendamentos: um sem conflito, um com conflito
        Appointment noConflict = buildAppointment(UUID.randomUUID(), BASE_TIME.plusHours(2), 30);
        Appointment conflict  = buildAppointment(UUID.randomUUID(), BASE_TIME.plusMinutes(15), 30);

        String result = checker.findConflict(
                BASE_TIME, 30, null, (s, e) -> List.of(noConflict, conflict), CONFLICT_MSG
        );

        assertThat(result).isEqualTo(CONFLICT_MSG);
    }

    @Test
    void shouldReturnNullWhenOnlyConflictingAppointmentIsExcluded() {
        UUID excludedId = UUID.randomUUID();
        Appointment excluded  = buildAppointment(excludedId, BASE_TIME, 30);
        Appointment otherOk  = buildAppointment(UUID.randomUUID(), BASE_TIME.plusHours(2), 30);

        String result = checker.findConflict(
                BASE_TIME, 30, excludedId, (s, e) -> List.of(excluded, otherOk), CONFLICT_MSG
        );

        assertThat(result).isNull();
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private Appointment buildAppointment(UUID id, LocalDateTime scheduledAt, int durationMinutes) {
        Appointment a = appointment();
        a.setId(id);
        a.setScheduledAt(scheduledAt);
        a.setDurationMinutes(durationMinutes);
        return a;
    }
}
