package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.entity.ConsentType;
import com.jettech.api.solutions_clinic.model.usecase.consent.GrantConsentBodyRequest;
import com.jettech.api.solutions_clinic.model.usecase.consent.PatientConsentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Consentimentos LGPD", description = "Endpoints para gerenciamento de consentimentos de pacientes (LGPD Art. 7 e 11)")
public interface PatientConsentAPI {

    @PostMapping("/patients/{patientId}/consents")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra consentimento do paciente", description = "Registra o consentimento explícito do paciente para um tipo de uso de dados, com versão do termo e IP de origem.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consentimento registrado com sucesso",
                    content = @Content(schema = @Schema(implementation = PatientConsentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado", content = @Content)
    })
    PatientConsentResponse grantConsent(
            @PathVariable UUID patientId,
            @Valid @RequestBody GrantConsentBodyRequest request
    ) throws AuthenticationFailedException;

    @DeleteMapping("/patients/{patientId}/consents/{consentId}")
    @Operation(summary = "Revoga consentimento do paciente", description = "Registra a revogação de um consentimento ativo, preservando o histórico para auditoria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consentimento revogado com sucesso",
                    content = @Content(schema = @Schema(implementation = PatientConsentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Consentimento não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Consentimento já revogado", content = @Content)
    })
    PatientConsentResponse revokeConsent(
            @PathVariable UUID patientId,
            @PathVariable UUID consentId
    ) throws AuthenticationFailedException;

    @GetMapping("/patients/{patientId}/consents")
    @Operation(summary = "Lista consentimentos do paciente", description = "Retorna o histórico completo de consentimentos do paciente, incluindo revogados, em ordem cronológica decrescente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de consentimentos retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = PatientConsentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado", content = @Content)
    })
    List<PatientConsentResponse> getConsents(
            @PathVariable UUID patientId,
            @RequestParam(required = false) ConsentType consentType
    ) throws AuthenticationFailedException;
}
