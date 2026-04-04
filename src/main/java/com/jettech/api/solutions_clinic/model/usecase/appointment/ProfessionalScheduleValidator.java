package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ProfessionalScheduleValidator {

    private final ProfessionalScheduleRepository professionalScheduleRepository;

    public void validate(UUID professionalId, LocalDateTime scheduledAt, int durationMinutes) {
        DayOfWeek dayOfWeek = scheduledAt.getDayOfWeek();

        professionalScheduleRepository
                .findByProfessionalIdAndDayOfWeek(professionalId, dayOfWeek)
                .orElseThrow(() -> new EntityNotFoundException(ApiError.ENTITY_NOT_FOUND_SCHEDULE));
    }
}
