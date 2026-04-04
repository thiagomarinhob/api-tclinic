package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.memed.*;
import com.jettech.api.solutions_clinic.model.usecase.memed.MemedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/memed")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MemedController {

    private final MemedService memedService;

    /**
     * Gera token de autenticação do Memed
     */
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> generateToken(@RequestBody GenerateTokenRequest request) {
        try {
            String token = memedService.generateToken(
                request.getProfessionalId(),
                request.getAppointmentId()
            );
            
            return ResponseEntity.ok(new TokenResponse(token));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new TokenResponse(null, e.getMessage()));
        }
    }

    /**
     * Salva documento gerado pelo Memed
     */
    @PostMapping("/documents")
    public ResponseEntity<MedicalDocumentResponse> saveDocument(
            @RequestBody SaveDocumentRequest request) {
        try {
            MedicalDocumentResponse document = memedService.saveDocument(
                request.getAppointmentId(),
                request.getDocumentUrl(),
                request.getDocumentType()
            );
            
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Busca o PDF da prescrição no Memed e salva o documento
     */
    @PostMapping("/documents/pdf")
    public ResponseEntity<MedicalDocumentResponse> saveDocumentWithPdf(
            @RequestBody SaveDocumentWithPdfRequest request) {
        try {
            MedicalDocumentResponse document = memedService.saveDocumentWithPdf(
                request.getAppointmentId(),
                request.getPrescriptionId(),
                request.getUserToken(),
                request.getDocumentType()
            );

            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lista documentos de um agendamento
     */
    @GetMapping("/appointments/{appointmentId}/documents")
    public ResponseEntity<List<MedicalDocumentResponse>> getDocuments(
            @PathVariable UUID appointmentId) {
        try {
            List<MedicalDocumentResponse> documents =
                memedService.getDocumentsByAppointment(appointmentId);

            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
