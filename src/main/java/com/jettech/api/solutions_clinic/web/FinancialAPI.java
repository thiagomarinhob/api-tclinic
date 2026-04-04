package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.entity.PaymentStatus;
import com.jettech.api.solutions_clinic.model.entity.TransactionType;
import com.jettech.api.solutions_clinic.model.usecase.financial.*;
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
import java.util.List;
import java.util.UUID;

@Tag(name = "Financeiro", description = "Endpoints para gerenciamento financeiro")
public interface FinancialAPI {

    // Categorias Financeiras
    @PostMapping("/financial/categories")
    @Operation(summary = "Cria uma nova categoria financeira", description = "Registra uma nova categoria para classificar receitas ou despesas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria criada com sucesso",
                    content = @Content(schema = @Schema(implementation = FinancialCategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Clínica não encontrada", content = @Content)
    })
    FinancialCategoryResponse createFinancialCategory(@Valid @RequestBody CreateFinancialCategoryRequest request) throws AuthenticationFailedException;

    @GetMapping("/tenants/{tenantId}/financial/categories")
    @Operation(summary = "Lista categorias financeiras de uma clínica", description = "Retorna todas as categorias financeiras de uma clínica com filtros opcionais.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = FinancialCategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Clínica não encontrada", content = @Content)
    })
    List<FinancialCategoryResponse> getFinancialCategoriesByTenant(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Boolean active
    ) throws AuthenticationFailedException;

    // Transações Financeiras
    @PostMapping("/financial/transactions")
    @Operation(summary = "Cria uma nova transação financeira", description = "Registra uma nova transação financeira (receita ou despesa).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transação criada com sucesso",
                    content = @Content(schema = @Schema(implementation = FinancialTransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Clínica, categoria, agendamento ou profissional não encontrado", content = @Content)
    })
    FinancialTransactionResponse createFinancialTransaction(@Valid @RequestBody CreateFinancialTransactionRequest request) throws AuthenticationFailedException;

    @GetMapping("/tenants/{tenantId}/financial/transactions")
    @Operation(summary = "Lista transações financeiras de uma clínica", description = "Retorna todas as transações financeiras de uma clínica com filtros opcionais.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de transações retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = FinancialTransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Clínica não encontrada", content = @Content)
    })
    List<FinancialTransactionResponse> getFinancialTransactionsByTenant(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws AuthenticationFailedException;

    // Dashboard Financeiro
    @GetMapping("/tenants/{tenantId}/financial/dashboard")
    @Operation(summary = "Obtém dados do dashboard financeiro", description = "Retorna resumo financeiro com totais, saldo, agrupamentos por categoria e transações pendentes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do dashboard retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = FinancialDashboardResponse.class))),
            @ApiResponse(responseCode = "404", description = "Clínica não encontrada", content = @Content)
    })
    FinancialDashboardResponse getFinancialDashboard(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws AuthenticationFailedException;
}
