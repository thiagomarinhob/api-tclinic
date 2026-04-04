package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.medicalrecord.CreateOrUpdateMedicalRecordRequest;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecord.MedicalRecordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecord.MedicalRecordListResponse;

import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "Prontuários", description = "Prontuários por consulta (conteúdo JSON conforme template)")
public interface MedicalRecordAPI {

    @GetMapping("/medical-records")
    @Operation(summary = "Lista prontuários", description = "Profissional vê apenas os seus; clínica vê todos. Filtros e paginação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de prontuários",
                    content = @Content(schema = @Schema(implementation = org.springframework.data.domain.Page.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    Page<MedicalRecordListResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) throws AuthenticationFailedException;

    @PostMapping("/medical-records")
    @Operation(summary = "Cria ou atualiza prontuário", description = "Por appointmentId. Se já existir, atualiza content e vitalSigns.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prontuário salvo",
                    content = @Content(schema = @Schema(implementation = MedicalRecordResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Agendamento ou modelo não encontrado", content = @Content)
    })
    MedicalRecordResponse createOrUpdate(@Valid @RequestBody CreateOrUpdateMedicalRecordRequest request) throws AuthenticationFailedException;

    @GetMapping("/medical-records/appointment/{appointmentId}")
    @Operation(summary = "Busca prontuário por agendamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prontuário encontrado ou vazio",
                    content = @Content(schema = @Schema(implementation = MedicalRecordResponse.class))),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content)
    })
    ResponseEntity<MedicalRecordResponse> getByAppointment(@PathVariable UUID appointmentId) throws AuthenticationFailedException;

    @GetMapping("/medical-records/{id}")
    @Operation(summary = "Busca prontuário por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prontuário encontrado",
                    content = @Content(schema = @Schema(implementation = MedicalRecordResponse.class))),
            @ApiResponse(responseCode = "404", description = "Prontuário não encontrado", content = @Content)
    })
    MedicalRecordResponse getById(@PathVariable UUID id) throws AuthenticationFailedException;

    @PostMapping("/medical-records/{id}/sign")
    @Operation(summary = "Assina prontuário", description = "Define signed_at com a data/hora atual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prontuário assinado",
                    content = @Content(schema = @Schema(implementation = MedicalRecordResponse.class))),
            @ApiResponse(responseCode = "404", description = "Prontuário não encontrado", content = @Content)
    })
    MedicalRecordResponse sign(@PathVariable UUID id) throws AuthenticationFailedException;

    @GetMapping(value = "/medical-records/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Exporta prontuário em PDF", description = "Retorna o prontuário como documento PDF para download.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF do prontuário",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "Prontuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    ResponseEntity<byte[]> exportPdf(@PathVariable UUID id) throws AuthenticationFailedException;
}
