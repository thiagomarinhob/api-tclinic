package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.admin.AdminTenantDetailResponse;
import com.jettech.api.solutions_clinic.model.usecase.admin.AdminTenantListItemResponse;
import com.jettech.api.solutions_clinic.model.entity.PlanType;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.usecase.tenant.ExtendTrialBody;
import com.jettech.api.solutions_clinic.model.usecase.tenant.TenantResponse;
import com.jettech.api.solutions_clinic.model.usecase.tenant.UpdateTenantPlanBody;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin - Tenants", description = "Endpoints administrativos para gerenciamento de tenants")
public interface AdminTenantAPI {

    @GetMapping("/admin/tenants")
    @Operation(summary = "Lista todos os tenants com paginação e filtros")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos", content = @Content)
    })
    Page<AdminTenantListItemResponse> listTenants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TenantStatus status,
            @RequestParam(required = false) PlanType planType
    ) throws AuthenticationFailedException;

    @GetMapping("/admin/tenants/{id}")
    @Operation(summary = "Retorna detalhe completo de um tenant incluindo IDs Stripe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant encontrado",
                    content = @Content(schema = @Schema(implementation = AdminTenantDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tenant não encontrado", content = @Content)
    })
    AdminTenantDetailResponse getTenantDetail(@PathVariable UUID id) throws AuthenticationFailedException;

    @PostMapping("/admin/tenants/{id}/suspend")
    @Operation(summary = "Suspende um tenant (ACTIVE/TRIAL → SUSPENDED)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tenant suspenso com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tenant não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Tenant já está suspenso", content = @Content),
            @ApiResponse(responseCode = "422", description = "Transição inválida (CANCELED/PENDING_SETUP)", content = @Content)
    })
    ResponseEntity<Void> suspendTenant(@PathVariable UUID id) throws AuthenticationFailedException;

    @PostMapping("/admin/tenants/{id}/reactivate")
    @Operation(summary = "Reativa um tenant suspenso (SUSPENDED → ACTIVE)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tenant reativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tenant não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Tenant não está suspenso", content = @Content)
    })
    ResponseEntity<Void> reactivateTenant(@PathVariable UUID id) throws AuthenticationFailedException;

    @PostMapping("/admin/tenants/{id}/extend-trial")
    @Operation(summary = "Estende o período de trial de um tenant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trial estendido com sucesso",
                    content = @Content(schema = @Schema(implementation = TenantResponse.class))),
            @ApiResponse(responseCode = "400", description = "additionalDays fora do intervalo (1–365)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tenant não encontrado", content = @Content),
            @ApiResponse(responseCode = "422", description = "Status inválido para extensão de trial", content = @Content)
    })
    TenantResponse extendTrial(
            @PathVariable UUID id,
            @Valid @RequestBody ExtendTrialBody body
    ) throws AuthenticationFailedException;

    @PatchMapping("/admin/tenants/{id}/plan")
    @Operation(summary = "Altera o plano de um tenant (sem validação de TenantContext)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Plano alterado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tenant não encontrado", content = @Content),
            @ApiResponse(responseCode = "422", description = "Tenant cancelado não pode ter o plano alterado", content = @Content)
    })
    ResponseEntity<Void> changeTenantPlan(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantPlanBody body
    ) throws AuthenticationFailedException;
}
