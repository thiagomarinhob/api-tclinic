package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetAvailableSlotsUseCase implements GetAvailableSlotsUseCase {

    private final ProfessionalRepository professionalRepository;
    private final ProfessionalScheduleRepository professionalScheduleRepository;

    @Override
    public List<String> execute(GetAvailableSlotsRequest request) {
        log.info("Buscando slots disponíveis | professionalId={} | date={}", request.professionalId(), request.date());

        if (!professionalRepository.existsById(request.professionalId())) {
            throw new EntityNotFoundException("Profissional", request.professionalId());
        }

        DayOfWeek dayOfWeek = request.date().getDayOfWeek();
        boolean hasSchedule = professionalScheduleRepository
                .findByProfessionalIdAndDayOfWeek(request.professionalId(), dayOfWeek)
                .isPresent();

        if (!hasSchedule) {
            log.info("Sem agenda para o dia | professionalId={} | date={} | diaDaSemana={} | slots=0",
                    request.professionalId(), request.date(), dayOfWeek);
            return new ArrayList<>();
        }

        List<String> slots = new ArrayList<>();
        log.info("Slots retornados | professionalId={} | date={} | total={}", request.professionalId(), request.date(), slots.size());
        return slots;
    }
}
