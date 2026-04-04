package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.usecase.appointment.AppointmentResponse;
import com.jettech.api.solutions_clinic.model.usecase.appointment.CreateAppointmentRequest;
import com.jettech.api.solutions_clinic.model.usecase.appointment.UpdateAppointmentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Agendamentos", description = "Endpoints para gerenciamento de agendamentos")
public interface AppointmentAPI {

    @PostMapping("/appointments")
    @Operation(summary = "Cria um novo agendamento", description = "Registra um novo agendamento após validar horário disponível do profissional e sala.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agendamento criado com sucesso", 
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou horário indisponível", content = @Content),
            @ApiResponse(responseCode = "404", description = "Paciente, profissional, sala ou usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito de horário", content = @Content)
    })
    AppointmentResponse createAppointment(@Valid @RequestBody CreateAppointmentRequest request) throws AuthenticationFailedException;

    @GetMapping("/appointments/{id}")
    @Operation(summary = "Busca agendamento por ID", description = "Retorna os dados de um agendamento específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agendamento encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content)
    })
    AppointmentResponse getAppointmentById(@PathVariable UUID id) throws AuthenticationFailedException;

    @GetMapping("/professionals/{professionalId}/appointments")
    @Operation(summary = "Lista agendamentos de um profissional", description = "Retorna os agendamentos de um profissional, com filtro opcional por intervalo de datas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de agendamentos retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado", content = @Content)
    })
    List<AppointmentResponse> getAppointmentsByProfessionalId(
            @PathVariable UUID professionalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws AuthenticationFailedException;

    @PutMapping("/appointments")
    @Operation(summary = "Atualiza um agendamento", description = "Atualiza os dados de um agendamento existente. Valida horário disponível se houver mudanças.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agendamento atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou horário indisponível", content = @Content),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content)
    })
    AppointmentResponse updateAppointment(@Valid @RequestBody UpdateAppointmentRequest request) throws AuthenticationFailedException;

    @DeleteMapping("/appointments/{id}")
    @Operation(summary = "Cancela um agendamento", description = "Cancela um agendamento (marca como cancelado ao invés de deletar).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agendamento cancelado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content)
    })
    void deleteAppointment(@PathVariable UUID id) throws AuthenticationFailedException;

    @GetMapping("/tenants/{tenantId}/appointments")
    @Operation(summary = "Lista agendamentos de uma clínica", description = "Retorna todos os agendamentos de uma clínica com filtros opcionais de data, intervalo de datas, status e ordenação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de agendamentos retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Clínica não encontrada", content = @Content)
    })
    List<AppointmentResponse> getAppointmentsByTenant(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false, defaultValue = "scheduledAt_desc") String orderBy
    ) throws AuthenticationFailedException;

    @GetMapping("/appointments/check-availability")
    @Operation(summary = "Verifica disponibilidade de horário", description = "Verifica se um horário específico está disponível para um profissional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disponibilidade verificada com sucesso",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado", content = @Content)
    })
    Boolean checkAvailability(
            @RequestParam UUID professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam int durationMinutes,
            @RequestParam(required = false) UUID appointmentId
    ) throws AuthenticationFailedException;

    @GetMapping("/appointments/available-slots")
    @Operation(summary = "Lista horários disponíveis", description = "Retorna todos os horários disponíveis de um profissional em uma data específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de horários disponíveis retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado", content = @Content)
    })
    List<String> getAvailableSlots(
            @RequestParam UUID professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "60") int durationMinutes
    ) throws AuthenticationFailedException;

    @PatchMapping("/appointments/{id}/triage")
    @Operation(summary = "Salva dados de triagem", description = "Salva os sinais vitais (triagem) em um agendamento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Triagem salva com sucesso",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content)
    })
    AppointmentResponse saveTriage(@PathVariable UUID id, @RequestBody Map<String, Object> vitalSigns) throws AuthenticationFailedException;

    @PostMapping("/appointments/{id}/start")
    @Operation(summary = "Inicia atendimento", description = "Altera o status do agendamento para EM_ATENDIMENTO e registra o horário de início.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atendimento iniciado com sucesso",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content),
            @ApiResponse(responseCode = "422", description = "Status do agendamento não permite iniciar", content = @Content)
    })
    AppointmentResponse startAppointment(@PathVariable UUID id) throws AuthenticationFailedException;

    @PostMapping("/appointments/{id}/finish")
    @Operation(summary = "Finaliza atendimento", description = "Altera o status do agendamento para FINALIZADO e registra horário de término e duração real.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atendimento finalizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content),
            @ApiResponse(responseCode = "422", description = "Status do agendamento não permite finalizar", content = @Content)
    })
    AppointmentResponse finishAppointment(@PathVariable UUID id, @RequestBody(required = false) Map<String, Object> body) throws AuthenticationFailedException;
}
