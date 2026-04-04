package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.user.AuthUserRequest;
import com.jettech.api.solutions_clinic.model.usecase.user.AuthUserResponse;
import com.jettech.api.solutions_clinic.model.usecase.user.SwitchTenantRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e troca de contexto")
public interface AuthUserAPI {

    @PostMapping("/sign-in")
    @Operation(summary = "Autentica um usuário", description = "Realiza a autenticação de um usuário com email e senha, retornando um token JWT em caso de sucesso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida",
                    content = @Content(schema = @Schema(implementation = AuthUserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content)
    })
    AuthUserResponse signIn(@Valid @RequestBody AuthUserRequest authUserRequest) throws AuthenticationFailedException;

    @PostMapping("/switch-tenant")
    @Operation(summary = "Troca de clínica", description = "Troca o contexto da sessão para outra clínica em que o usuário tem vínculo. Retorna um novo JWT com o clinicId escolhido. Requer autenticação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token emitido com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthUserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado ou sem permissão na clínica", content = @Content)
    })
    AuthUserResponse switchTenant(@Valid @RequestBody SwitchTenantRequest request) throws AuthenticationFailedException;
}
