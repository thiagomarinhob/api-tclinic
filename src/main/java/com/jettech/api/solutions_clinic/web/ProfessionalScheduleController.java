package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.professionalschedule.*;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ProfessionalScheduleController implements ProfessionalScheduleAPI {

    private final CreateProfessionalScheduleUseCase createProfessionalScheduleUseCase;
    private final GetProfessionalScheduleByIdUseCase getProfessionalScheduleByIdUseCase;
    private final GetProfessionalSchedulesByProfessionalIdUseCase getProfessionalSchedulesByProfessionalIdUseCase;
    private final DeleteProfessionalScheduleUseCase deleteProfessionalScheduleUseCase;

    @Override
    public ProfessionalScheduleResponse createProfessionalSchedule(@Valid @RequestBody CreateProfessionalScheduleRequest request) throws AuthenticationFailedException {
        return createProfessionalScheduleUseCase.execute(request);
    }

    @Override
    public ProfessionalScheduleResponse getProfessionalScheduleById(@PathVariable UUID id) throws AuthenticationFailedException {
        return getProfessionalScheduleByIdUseCase.execute(id);
    }

    @Override
    public List<ProfessionalScheduleResponse> getProfessionalSchedulesByProfessionalId(@PathVariable UUID professionalId) throws AuthenticationFailedException {
        return getProfessionalSchedulesByProfessionalIdUseCase.execute(professionalId);
    }

    @Override
    public void deleteProfessionalSchedule(@PathVariable UUID id) throws AuthenticationFailedException {
        deleteProfessionalScheduleUseCase.execute(id);
    }
}
