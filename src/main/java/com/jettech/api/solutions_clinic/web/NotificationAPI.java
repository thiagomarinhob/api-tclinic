package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.notification.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notificações", description = "Endpoints para notificações in-app da clínica")
public interface NotificationAPI {

    @GetMapping("/tenants/{tenantId}/notifications")
    @Operation(summary = "Lista notificações da clínica", description = "Retorna as notificações do tenant ordenadas por data (mais recentes primeiro).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado ao tenant", content = @Content)
    })
    List<NotificationResponse> getNotifications(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) throws AuthenticationFailedException;

    @GetMapping("/tenants/{tenantId}/notifications/unread-count")
    @Operation(summary = "Conta notificações não lidas", description = "Retorna a quantidade de notificações não visualizadas do tenant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado ao tenant", content = @Content)
    })
    long getUnreadCount(@PathVariable UUID tenantId) throws AuthenticationFailedException;

    @PatchMapping("/tenants/{tenantId}/notifications/{id}/read")
    @Operation(summary = "Marca notificação como lida", description = "Marca uma notificação específica como visualizada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificação marcada como lida"),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado ao tenant", content = @Content)
    })
    void markAsRead(@PathVariable UUID tenantId, @PathVariable UUID id) throws AuthenticationFailedException;

    @PatchMapping("/tenants/{tenantId}/notifications/mark-all-read")
    @Operation(summary = "Marca todas como lidas", description = "Marca todas as notificações do tenant como visualizadas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todas as notificações marcadas como lidas"),
            @ApiResponse(responseCode = "403", description = "Acesso negado ao tenant", content = @Content)
    })
    void markAllAsRead(@PathVariable UUID tenantId) throws AuthenticationFailedException;
}
