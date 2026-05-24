package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.entity.ProfessionalSchedule;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.jettech.api.solutions_clinic.model.usecase.appointment.AppointmentFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCheckAvailabilityUseCaseTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock ProfessionalRepository professionalRepository;
    @Mock ProfessionalScheduleRepository professionalScheduleRepository;

    @InjectMocks
    DefaultCheckAvailabilityUseCase useCase;

    @BeforeEach
    void setUp() {
        when(professionalRepository.existsById(PROFESSIONAL_ID)).thenReturn(true);
        when(professionalScheduleRepository.findByProfessionalIdAndDayOfWeek(any(), any()))
                .thenReturn(Optional.of(new ProfessionalSchedule()));
        when(appointmentRepository.findByProfessionalIdAndScheduledAtBetweenAndStatusNot(
                any(), any(), any(), eq(AppointmentStatus.CANCELADO)
        )).thenReturn(List.of());
    }

    @Test
    void shouldReturnTrueWhenNoConflictsExist() {
        CheckAvailabilityRequest request = new CheckAvailabilityRequest(
                PROFESSIONAL_ID, SCHEDULED_AT, DURATION_MINUTES, null
        );

        Boolean result = useCase.execute(request);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenProfessionalHasNoScheduleForThatDay() {
        when(professionalScheduleRepository.findByProfessionalIdAndDayOfWeek(any(), any()))
                .thenReturn(Optional.empty());

        CheckAvailabilityRequest request = new CheckAvailabilityRequest(
                PROFESSIONAL_ID, SCHEDULED_AT, DURATION_MINUTES, null
        );

        Boolean result = useCase.execute(request);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenOverlappingAppointmentExists() {
        // Agendamento existente no mesmo horário
        Appointment existing = appointment();
        existing.setScheduledAt(SCHEDULED_AT);
        existing.setDurationMinutes(DURATION_MINUTES);

        when(appointmentRepository.findByProfessionalIdAndScheduledAtBetweenAndStatusNot(
                any(), any(), any(), eq(AppointmentStatus.CANCELADO)
        )).thenReturn(List.of(existing));

        CheckAvailabilityRequest request = new CheckAvailabilityRequest(
                PROFESSIONAL_ID, SCHEDULED_AT, DURATION_MINUTES, null
        );

        Boolean result = useCase.execute(request);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNewAppointmentStartsDuringExistingOne() {
        // Existente: 10:00-10:30 / Novo: 10:15-10:45 → sobreposição
        Appointment existing = appointment();
        existing.setScheduledAt(SCHEDULED_AT);           // 10:00
        existing.setDurationMinutes(30);

        when(appointmentRepository.findByProfessionalIdAndScheduledAtBetweenAndStatusNot(
                any(), any(), any(), eq(AppointmentStatus.CANCELADO)
        )).thenReturn(List.of(existing));

        CheckAvailabilityRequest request = new CheckAvailabilityRequest(
                PROFESSIONAL_ID, SCHEDULED_AT.plusMinutes(15), 30, null   // 10:15
        );

        assertThat(useCase.execute(request)).isFalse();
    }

    @Test
    void shouldReturnTrueWhenNewAppointmentStartsExactlyWhenExistingEnds() {
        // Existente: 10:00-10:30 / Novo: 10:30-11:00 → sem sobreposição (limite exclusivo)
        Appointment existing = appointment();
        existing.setScheduledAt(SCHEDULED_AT);           // 10:00
        existing.setDurationMinutes(30);

        when(appointmentRepository.findByProfessionalIdAndScheduledAtBetweenAndStatusNot(
                any(), any(), any(), eq(AppointmentStatus.CANCELADO)
        )).thenReturn(List.of(existing));

        CheckAvailabilityRequest request = new CheckAvailabilityRequest(
                PROFESSIONAL_ID, SCHEDULED_AT.plusMinutes(30), 30, null   // 10:30
        );

        assertThat(useCase.execute(request)).isTrue();
    }

    @Test
    void shouldIgnoreExcludedAppointmentIdWhenCheckingConflicts() {
        // Mesmo agendamento excluído não deve gerar conflito (caso de edição)
        Appointment existing = appointment();
        existing.setScheduledAt(SCHEDULED_AT);
        existing.setDurationMinutes(DURATION_MINUTES);

        when(appointmentRepository.findByProfessionalIdAndScheduledAtBetweenAndStatusNot(
                any(), any(), any(), eq(AppointmentStatus.CANCELADO)
        )).thenReturn(List.of(existing));

        CheckAvailabilityRequest request = new CheckAvailabilityRequest(
                PROFESSIONAL_ID, SCHEDULED_AT, DURATION_MINUTES, APPOINTMENT_ID
        );

        Boolean result = useCase.execute(request);

        assertThat(result).isTrue();
    }

    @Test
    void shouldThrowEntityNotFoundWhenProfessionalDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(professionalRepository.existsById(unknownId)).thenReturn(false);

        CheckAvailabilityRequest request = new CheckAvailabilityRequest(
                unknownId, SCHEDULED_AT, DURATION_MINUTES, null
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldReturnTrueWhenOtherAppointmentEndsBeforeNewOneStarts() {
        // Existente: 08:00-09:00 / Novo: 10:00-10:30 → sem sobreposição
        Appointment existing = appointment();
        existing.setScheduledAt(SCHEDULED_AT.minusHours(2));  // 08:00
        existing.setDurationMinutes(60);                       // termina 09:00

        when(appointmentRepository.findByProfessionalIdAndScheduledAtBetweenAndStatusNot(
                any(), any(), any(), eq(AppointmentStatus.CANCELADO)
        )).thenReturn(List.of(existing));

        CheckAvailabilityRequest request = new CheckAvailabilityRequest(
                PROFESSIONAL_ID, SCHEDULED_AT, DURATION_MINUTES, null
        );

        assertThat(useCase.execute(request)).isTrue();
    }
}
