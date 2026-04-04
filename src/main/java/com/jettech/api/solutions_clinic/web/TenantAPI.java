package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.subscription.CreateCheckoutSessionBody;
import com.jettech.api.solutions_clinic.model.usecase.subscription.CreateCheckoutSessionResponse;
import com.jettech.api.solutions_clinic.model.usecase.tenant.ActivatePlanBody;
import com.jettech.api.solutions_clinic.model.usecase.tenant.TenantLogoUploadUrlResponse;
import com.jettech.api.solutions_clinic.model.usecase.tenant.TenantResponse;
import com.jettech.api.solutions_clinic.model.usecase.tenant.UpdateTenantLogoBody;
import com.jettech.api.solutions_clinic.model.usecase.tenant.UpdateTenantPlanBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.util.UUID;

@Tag(name = "Tenants", description = "Endpoints para gerenciamento de tenants (clínicas)")
public interface TenantAPI {

    @PatchMapping("/tenants/{tenantId}/plan")
    @Operation(
        summary = "Atualiza o plano de um tenant",
        description = "Atualiza o plano selecionado pelo tenant. Se o tenant estava em PENDING_SETUP e seleciona um plano BASIC ou PRO, o status é atualizado para ACTIVE."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Plano atualizado com sucesso",
                content = @Content(schema = @Schema(implementation = TenantResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Dados inválidos",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Tenant não encontrado",
                content = @Content
            )
    })
    TenantResponse updateTenantPlan(
            @PathVariable UUID tenantId,
            @Valid @RequestBody UpdateTenantPlanBody body
    ) throws AuthenticationFailedException;

    @PostMapping("/tenants/{tenantId}/checkout")
    @Operation(
        summary = "Cria sessão de checkout do Stripe",
        description = "Cria uma sessão de checkout do Stripe para pagamento do plano selecionado pelo tenant"
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Sessão de checkout criada com sucesso",
                content = @Content(schema = @Schema(implementation = CreateCheckoutSessionResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Dados inválidos",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Tenant não encontrado",
                content = @Content
            )
    })
    CreateCheckoutSessionResponse createCheckoutSession(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateCheckoutSessionBody body
    ) throws AuthenticationFailedException;

    @PostMapping("/tenants/{tenantId}/users/{userId}/roles/{role}")
    @Operation(
        summary = "Associa um usuário a uma clínica com um papel específico",
        description = "Cria uma associação entre um usuário e uma clínica (tenant) com um papel (role) específico. " +
                      "Se a associação já existir, retorna erro."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Usuário associado à clínica com sucesso",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Dados inválidos ou associação já existe",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Usuário ou clínica não encontrado",
                content = @Content
            )
    })
    void associateUserToTenant(
            @PathVariable UUID tenantId,
            @PathVariable UUID userId,
            @PathVariable String role
    ) throws AuthenticationFailedException;

    @PostMapping("/tenants/{tenantId}/activate")
    @Operation(
        summary = "Ativa um plano manualmente (apenas para testes)",
        description = "Ativa um plano diretamente sem passar pelo fluxo de pagamento do Stripe. " +
                      "IMPORTANTE: Este endpoint deve ser usado apenas em ambiente de desenvolvimento/teste. " +
                      "Em producao, use o fluxo normal de checkout com o Stripe."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Plano ativado com sucesso",
                content = @Content(schema = @Schema(implementation = TenantResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Dados invalidos",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Tenant nao encontrado",
                content = @Content
            )
    })
    TenantResponse activatePlan(
            @PathVariable UUID tenantId,
            @Valid @RequestBody ActivatePlanBody body
    ) throws AuthenticationFailedException;

    @PostMapping("/tenants/{tenantId}/trial")
    @Operation(
        summary = "Inicia período de teste grátis",
        description = "Ativa um período de teste grátis para o tenant. " +
                      "O trial permite acesso completo ao sistema por um período limitado (padrão: 14 dias) " +
                      "sem necessidade de pagamento. Após o término do período, o tenant será suspenso até escolher um plano pago."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Período de teste iniciado com sucesso",
                content = @Content(schema = @Schema(implementation = TenantResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Dados inválidos ou clínica já possui plano ativo/trial",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Tenant não encontrado",
                content = @Content
            )
    })
    TenantResponse startTrial(
            @PathVariable UUID tenantId
    ) throws AuthenticationFailedException;

    @GetMapping("/tenants/me")
    @Operation(summary = "Retorna dados do tenant autenticado")
    TenantResponse getCurrentTenant() throws AuthenticationFailedException;

    @PostMapping("/tenants/me/logo/upload-url")
    @Operation(summary = "Gera URL pré-assinada para upload do logo da clínica")
    TenantLogoUploadUrlResponse getLogoUploadUrl(
            @RequestParam(required = false) String fileName
    ) throws AuthenticationFailedException;

    @PatchMapping("/tenants/me/logo")
    @Operation(summary = "Salva a chave do objeto R2 do logo da clínica após upload")
    ResponseEntity<Void> updateLogo(
            @Valid @RequestBody UpdateTenantLogoBody body
    ) throws AuthenticationFailedException;
}
