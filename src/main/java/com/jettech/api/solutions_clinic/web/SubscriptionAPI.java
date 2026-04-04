package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Subscriptions", description = "Endpoints para gerenciamento de assinaturas e pagamentos")
@RequestMapping("/v1/subscriptions")
public interface SubscriptionAPI {

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Webhook do Stripe",
        description = "Endpoint para receber eventos do Stripe sobre pagamentos e assinaturas"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Webhook processado com sucesso"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Erro ao processar webhook"
        )
    })
    void handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) throws AuthenticationFailedException;
}
