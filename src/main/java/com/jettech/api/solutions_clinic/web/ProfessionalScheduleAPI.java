package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.professionalschedule.CreateProfessionalScheduleRequest;
import com.jettech.api.solutions_clinic.model.usecase.professionalschedule.ProfessionalScheduleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.util.List;
import java.util.UUID;

@Tag(name = "Agendas de Profissionais", description = "Endpoints para gerenciamento de agendas de profissionais")
public interface ProfessionalScheduleAPI {

    @PostMapping("/professional-schedules")
    @Operation(summary = "Cria uma nova agenda para um profissional", description = "Registra um dia de atendimento para um profissional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agenda criada com sucesso",
                    content = @Content(schema = @Schema(implementation = ProfessionalScheduleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Já existe uma agenda para este profissional neste dia da semana", content = @Content)
    })
    ProfessionalScheduleResponse createProfessionalSchedule(@Valid @RequestBody CreateProfessionalScheduleRequest request) throws AuthenticationFailedException;

    @GetMapping("/professional-schedules/{id}")
    @Operation(summary = "Busca agenda por ID", description = "Retorna os dados de uma agenda específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agenda encontrada com sucesso",
                    content = @Content(schema = @Schema(implementation = ProfessionalScheduleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Agenda não encontrada", content = @Content)
    })
    ProfessionalScheduleResponse getProfessionalScheduleById(@PathVariable UUID id) throws AuthenticationFailedException;

    @GetMapping("/professionals/{professionalId}/schedules")
    @Operation(summary = "Lista agendas de um profissional", description = "Retorna todos os dias de atendimento cadastrados para um profissional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de agendas retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = ProfessionalScheduleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado", content = @Content)
    })
    List<ProfessionalScheduleResponse> getProfessionalSchedulesByProfessionalId(@PathVariable UUID professionalId) throws AuthenticationFailedException;

    @DeleteMapping("/professional-schedules/{id}")
    @Operation(summary = "Deleta uma agenda", description = "Remove um dia de atendimento do sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agenda deletada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Agenda não encontrada", content = @Content)
    })
    void deleteProfessionalSchedule(@PathVariable UUID id) throws AuthenticationFailedException;
}
