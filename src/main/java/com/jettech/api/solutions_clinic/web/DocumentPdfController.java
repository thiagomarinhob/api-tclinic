package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.usecase.document.GenerateAtestadoPdfRequest;
import com.jettech.api.solutions_clinic.model.usecase.document.GenerateEncaminhamentoPdfRequest;
import com.jettech.api.solutions_clinic.model.usecase.document.GenerateExamesPdfRequest;
import com.jettech.api.solutions_clinic.model.usecase.document.GenerateReceitaPdfRequest;
import com.jettech.api.solutions_clinic.security.TenantContext;
import com.jettech.api.solutions_clinic.service.DocumentPdfService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/appointments/{appointmentId}/documents")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DocumentPdfController {

    private final AppointmentRepository appointmentRepository;
    private final TenantContext tenantContext;
    private final DocumentPdfService documentPdfService;

    @PostMapping(value = "/receita/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> receita(
            @PathVariable UUID appointmentId,
            @RequestBody GenerateReceitaPdfRequest request)
            throws AuthenticationFailedException {
        Appointment appointment = loadAppointment(appointmentId);
        byte[] pdf = documentPdfService.generateReceita(appointment, request.prescricoes());
        return pdfResponse(pdf, "receita-" + appointmentId + ".pdf");
    }

    @PostMapping(value = "/exames/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exames(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody GenerateExamesPdfRequest request)
            throws AuthenticationFailedException {
        Appointment appointment = loadAppointment(appointmentId);
        byte[] pdf = documentPdfService.generateSolicitacaoExames(
                appointment, request.exames(), request.indicacaoClinica());
        return pdfResponse(pdf, "solicitacao-exames-" + appointmentId + ".pdf");
    }

    @PostMapping(value = "/atestado/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> atestado(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody GenerateAtestadoPdfRequest request)
            throws AuthenticationFailedException {
        Appointment appointment = loadAppointment(appointmentId);
        byte[] pdf = documentPdfService.generateAtestado(
                appointment, request.dias(), request.motivo());
        return pdfResponse(pdf, "atestado-" + appointmentId + ".pdf");
    }

    @PostMapping(value = "/encaminhamento/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> encaminhamento(
            @PathVariable UUID appointmentId,
            @RequestBody GenerateEncaminhamentoPdfRequest request)
            throws AuthenticationFailedException {
        Appointment appointment = loadAppointment(appointmentId);
        byte[] pdf = documentPdfService.generateEncaminhamento(appointment, request.encaminhamento());
        return pdfResponse(pdf, "encaminhamento-" + appointmentId + ".pdf");
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private Appointment loadAppointment(UUID appointmentId) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", appointmentId));
        if (!appointment.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException();
        }
        return appointment;
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.setContentLength(pdf.length);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
