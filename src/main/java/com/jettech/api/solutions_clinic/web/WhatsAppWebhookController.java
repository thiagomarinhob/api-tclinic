package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.usecase.whatsapp.ProcessWhatsAppWebhookRequest;
import com.jettech.api.solutions_clinic.model.usecase.whatsapp.ProcessWhatsAppWebhookUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/whatsapp/webhook")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    private final ProcessWhatsAppWebhookUseCase processWhatsAppWebhookUseCase;

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody String body) throws AuthenticationFailedException {
        log.info("[WhatsApp] Webhook recebido — tamanho={} bytes", body == null ? 0 : body.length());
        if (body == null || !body.toLowerCase().contains("messages.upsert")) {
            log.info("[WhatsApp] Webhook ignorado — evento não é messages.upsert");
            return ResponseEntity.ok().build();
        }
        log.info("[WhatsApp] Webhook messages.upsert detectado — iniciando processamento");
        log.debug("[WhatsApp] Webhook body: {}", body);
        processWhatsAppWebhookUseCase.execute(new ProcessWhatsAppWebhookRequest(body));
        log.info("[WhatsApp] Webhook processado com sucesso");
        return ResponseEntity.ok().build();
    }
}
