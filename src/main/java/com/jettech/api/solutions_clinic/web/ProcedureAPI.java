package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.procedure.CreateProcedureRequest;
import com.jettech.api.solutions_clinic.model.usecase.procedure.ProcedureResponse;
import com.jettech.api.solutions_clinic.model.usecase.procedure.UpdateProcedureActiveBodyRequest;
import com.jettech.api.solutions_clinic.model.usecase.procedure.UpdateProcedureBodyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.util.UUID;

@Tag(name = "Procedimentos", description = "Endpoints para gerenciamento de procedimentos")
public interface ProcedureAPI {

    @PostMapping("/procedures")
    @Operation(summary = "Cria um novo procedimento", description = "Registra um novo procedimento no sistema associado a uma clínica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Procedimento criado com sucesso", 
                    content = @Content(schema = @Schema(implementation = ProcedureResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Clínica não encontrada", content = @Content)
    })
    ProcedureResponse createProcedure(@Valid @RequestBody CreateProcedureRequest request) throws AuthenticationFailedException;

    @GetMapping("/procedures")
    @Operation(
        summary = "Lista procedimentos de uma clínica com paginação, busca e filtros",
        description = "Retorna uma lista paginada de procedimentos de uma clínica (tenant). Suporta busca por nome ou descrição e filtro por status (ativo/inativo)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de procedimentos retornada com sucesso",
                content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Clínica não encontrada",
                content = @Content
            )
    })
    Page<ProcedureResponse> getProceduresByTenant(
            @RequestParam UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "name,asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) UUID professionalId
    ) throws AuthenticationFailedException;

    @GetMapping("/procedures/{id}")
    @Operation(
        summary = "Busca procedimento por ID",
        description = "Retorna os dados do procedimento incluindo todas as informações cadastradas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Procedimento encontrado com sucesso",
                content = @Content(schema = @Schema(implementation = ProcedureResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Procedimento não encontrado",
                content = @Content
            )
    })
    ProcedureResponse getProcedureById(@PathVariable UUID id) throws AuthenticationFailedException;

    @PutMapping("/procedures/{id}")
    @Operation(summary = "Atualiza um procedimento", description = "Atualiza os dados de um procedimento existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Procedimento atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProcedureResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Procedimento não encontrado", content = @Content)
    })
    ProcedureResponse updateProcedure(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProcedureBodyRequest request
    ) throws AuthenticationFailedException;

    @PatchMapping("/procedures/{id}/active")
    @Operation(summary = "Atualiza o status ativo de um procedimento", description = "Ativa ou desativa um procedimento no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status do procedimento atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProcedureResponse.class))),
            @ApiResponse(responseCode = "404", description = "Procedimento não encontrado", content = @Content)
    })
    ProcedureResponse updateProcedureActive(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProcedureActiveBodyRequest request
    ) throws AuthenticationFailedException;

    @DeleteMapping("/procedures/{id}")
    @Operation(summary = "Exclui um procedimento", description = "Remove um procedimento do sistema. Não é possível excluir procedimentos que estão associados a agendamentos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Procedimento excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Procedimento não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Não é possível excluir procedimento em uso", content = @Content)
    })
    void deleteProcedure(@PathVariable UUID id) throws AuthenticationFailedException;
}
