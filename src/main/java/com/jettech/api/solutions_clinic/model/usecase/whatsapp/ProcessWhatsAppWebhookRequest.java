package com.jettech.api.solutions_clinic.model.usecase.whatsapp;

/**
 * Payload bruto do webhook do WhatsApp (corpo do POST).
 */
public record ProcessWhatsAppWebhookRequest(String body) {
}
