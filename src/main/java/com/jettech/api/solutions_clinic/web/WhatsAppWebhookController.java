package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.usecase.whatsapp.ProcessWhatsAppWebhookRequest;
import com.jettech.api.solutions_clinic.model.usecase.whatsapp.ProcessWhatsAppWebhookUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhook para verificação e recebimento de eventos da API oficial do WhatsApp.
 * GET: verificação na configuração do webhook (hub.mode, hub.challenge, hub.verify_token).
 * POST: recebe notificações de mensagens e eventos.
 */
@Slf4j
@RestController
@RequestMapping("/v1/whatsapp/webhook")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    @Value("${whatsapp.webhook.verify-token:}")
    private String verifyToken;

    private final ProcessWhatsAppWebhookUseCase processWhatsAppWebhookUseCase;

    /**
     * Verificação do webhook (requisitado pela API do WhatsApp ao configurar a URL).
     * Retorna hub.challenge se hub.mode == "subscribe" e hub.verify_token coincidir.
     */
    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.challenge", required = false) String challenge,
            @RequestParam(value = "hub.verify_token", required = false) String token
    ) {
        if (mode != null && challenge != null && token != null
                && "subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("WEBHOOK WHATSAPP VERIFICADO");
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).build();
    }

    /**
     * Recebe as notificações do WhatsApp (mensagens, status, etc.).
     * Responde 200 rapidamente; processa respostas de botão (Confirmar/Cancelar) para atualizar status do agendamento.
     */
    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody String body) throws AuthenticationFailedException {
        log.info("Webhook WhatsApp recebido: {}", body);
        processWhatsAppWebhookUseCase.execute(new ProcessWhatsAppWebhookRequest(body));
        return ResponseEntity.ok().build();
    }
}
