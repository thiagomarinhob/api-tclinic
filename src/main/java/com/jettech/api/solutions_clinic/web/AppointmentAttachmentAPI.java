package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.usecase.attachment.AttachmentResponse;
import com.jettech.api.solutions_clinic.model.usecase.attachment.AttachmentUploadUrlResponse;
import com.jettech.api.solutions_clinic.model.usecase.attachment.AttachmentViewUrlResponse;
import com.jettech.api.solutions_clinic.model.usecase.attachment.ConfirmAttachmentUploadRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Anexos de Atendimento", description = "Upload e gestão de arquivos anexados a um atendimento via Cloudflare R2")
public interface AppointmentAttachmentAPI {

    @PostMapping("/appointments/{appointmentId}/attachments/upload-url")
    @Operation(
            summary = "Gera URL pré-assinada para upload",
            description = "Retorna URL pré-assinada (5 min) para o frontend fazer PUT do arquivo diretamente no R2. Após o upload, chamar POST /appointments/attachments/confirm."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL gerada", content = @Content(schema = @Schema(implementation = AttachmentUploadUrlResponse.class))),
            @ApiResponse(responseCode = "404", description = "Atendimento não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "503", description = "R2 não configurado", content = @Content)
    })
    AttachmentUploadUrlResponse getUploadUrl(
            @PathVariable UUID appointmentId,
            @RequestParam(required = false) String fileName
    ) throws AuthenticationFailedException;

    @PostMapping("/appointments/attachments/confirm")
    @Operation(
            summary = "Confirma upload de anexo",
            description = "Após o frontend fazer PUT no R2, registra o anexo no banco de dados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anexo registrado", content = @Content(schema = @Schema(implementation = AttachmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Atendimento não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    AttachmentResponse confirmUpload(
            @Valid @RequestBody ConfirmAttachmentUploadRequest request
    ) throws AuthenticationFailedException;

    @GetMapping("/appointments/{appointmentId}/attachments")
    @Operation(summary = "Lista anexos do atendimento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de anexos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Atendimento não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    List<AttachmentResponse> listByAppointment(
            @PathVariable UUID appointmentId
    ) throws AuthenticationFailedException;

    @GetMapping("/appointments/attachments/{attachmentId}/view-url")
    @Operation(
            summary = "URL pré-assinada para visualizar anexo",
            description = "Retorna URL pré-assinada (15 min) para visualizar o arquivo armazenado no R2."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL gerada", content = @Content(schema = @Schema(implementation = AttachmentViewUrlResponse.class))),
            @ApiResponse(responseCode = "404", description = "Anexo não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "503", description = "R2 não configurado", content = @Content)
    })
    AttachmentViewUrlResponse getViewUrl(
            @PathVariable UUID attachmentId
    ) throws AuthenticationFailedException;

    @DeleteMapping("/appointments/attachments/{attachmentId}")
    @Operation(summary = "Remove anexo", description = "Exclui o registro do anexo. O arquivo no R2 não é removido automaticamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Anexo removido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Anexo não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    ResponseEntity<Void> deleteAttachment(
            @PathVariable UUID attachmentId
    ) throws AuthenticationFailedException;
}
