package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetAvailableSlotsUseCase implements GetAvailableSlotsUseCase {

    private final ProfessionalRepository professionalRepository;
    private final ProfessionalScheduleRepository professionalScheduleRepository;

    @Override
    public List<String> execute(GetAvailableSlotsRequest request) {
        if (!professionalRepository.existsById(request.professionalId())) {
            throw new EntityNotFoundException("Profissional", request.professionalId());
        }

        DayOfWeek dayOfWeek = request.date().getDayOfWeek();
        boolean hasSchedule = professionalScheduleRepository
                .findByProfessionalIdAndDayOfWeek(request.professionalId(), dayOfWeek)
                .isPresent();

        if (!hasSchedule) {
            return new ArrayList<>();
        }

        return new ArrayList<>();
    }
}
