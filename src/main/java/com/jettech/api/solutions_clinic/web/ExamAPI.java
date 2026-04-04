package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.exam.ConfirmExamResultRequest;
import com.jettech.api.solutions_clinic.model.usecase.exam.ConfirmExamRequestUploadRequest;
import com.jettech.api.solutions_clinic.model.usecase.exam.CreateExamRequest;
import com.jettech.api.solutions_clinic.model.usecase.exam.ExamResponse;
import com.jettech.api.solutions_clinic.model.usecase.exam.ExamListResponse;
import com.jettech.api.solutions_clinic.model.usecase.exam.ExamResultViewUrlResponse;
import com.jettech.api.solutions_clinic.model.usecase.exam.ExamTypeResponse;
import com.jettech.api.solutions_clinic.model.usecase.exam.PresignedUploadUrlResponse;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Exames", description = "Pedidos de exame e anexação de resultados (upload via R2)")
public interface ExamAPI {

    @GetMapping("/exam-types")
    @Operation(summary = "Lista catálogo de tipos de exame", description = "Retorna todos os tipos de exame ativos, ordenados por categoria e posição.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos de exame", content = @Content)
    })
    List<ExamTypeResponse> getExamTypes();

    @GetMapping("/exams/all")
    @Operation(summary = "Lista exames da clínica", description = "Lista paginada de todos os exames da clínica, com filtros opcionais por paciente e status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de exames", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    Page<ExamListResponse> getExamsByTenant(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) String status
    ) throws AuthenticationFailedException;

    @PostMapping("/exams")
    @Operation(summary = "Cria pedido de exame", description = "Registra um novo pedido de exame para o paciente (status REQUESTED).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exame criado", content = @Content(schema = @Schema(implementation = ExamResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Paciente ou consulta não encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado ao recurso", content = @Content)
    })
    ExamResponse createExam(@Valid @RequestBody CreateExamRequest request) throws AuthenticationFailedException;

    @GetMapping("/exams/{id}")
    @Operation(summary = "Busca exame por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exame encontrado", content = @Content(schema = @Schema(implementation = ExamResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exame não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    ExamResponse getExamById(@PathVariable UUID id) throws AuthenticationFailedException;

    @GetMapping("/exams")
    @Operation(summary = "Lista exames do paciente", description = "Lista paginada de exames do paciente (tenant do contexto).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de exames", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    Page<ExamResponse> getExamsByPatient(
            @RequestParam UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) throws AuthenticationFailedException;

    @PostMapping("/exams/{examId}/upload-url")
    @Operation(
            summary = "Gera URL para upload do resultado",
            description = "Retorna uma URL pré-assinada (válida 5 min) para o frontend fazer PUT do arquivo no R2. Depois o frontend chama POST /exams/confirm-result com o objectKey retornado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL de upload gerada", content = @Content(schema = @Schema(implementation = PresignedUploadUrlResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exame não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "422", description = "Exame já possui resultado", content = @Content),
            @ApiResponse(responseCode = "503", description = "R2 não configurado", content = @Content)
    })
    PresignedUploadUrlResponse getPresignedUploadUrl(
            @PathVariable UUID examId,
            @RequestParam(required = false) String fileName
    ) throws AuthenticationFailedException;

    @PostMapping("/exams/confirm-result")
    @Operation(
            summary = "Confirma anexação do resultado",
            description = "Após o frontend fazer upload do arquivo no R2, chama este endpoint com o examId e objectKey retornado em upload-url. Atualiza o exame para COMPLETED."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exame atualizado", content = @Content(schema = @Schema(implementation = ExamResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Exame não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    ExamResponse confirmExamResultUpload(@Valid @RequestBody ConfirmExamResultRequest request) throws AuthenticationFailedException;

    @GetMapping("/exams/{id}/result-view-url")
    @Operation(summary = "URL para visualizar resultado", description = "Retorna uma URL pré-assinada (GET) para visualizar o arquivo de resultado do exame.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL gerada", content = @Content(schema = @Schema(implementation = ExamResultViewUrlResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exame não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Exame sem resultado anexado", content = @Content),
            @ApiResponse(responseCode = "503", description = "R2 não configurado", content = @Content)
    })
    ExamResultViewUrlResponse getExamResultViewUrl(@PathVariable UUID id) throws AuthenticationFailedException;

    @PostMapping("/exams/{examId}/request-upload-url")
    @Operation(
            summary = "Gera URL para upload da solicitação médica",
            description = "Retorna uma URL pré-assinada (válida 5 min) para o frontend fazer PUT da solicitação/prescrição médica no R2. Depois o frontend chama POST /exams/confirm-request."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL de upload gerada", content = @Content(schema = @Schema(implementation = PresignedUploadUrlResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exame não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "503", description = "R2 não configurado", content = @Content)
    })
    PresignedUploadUrlResponse getPresignedRequestUploadUrl(
            @PathVariable UUID examId,
            @RequestParam(required = false) String fileName
    ) throws AuthenticationFailedException;

    @PostMapping("/exams/confirm-request")
    @Operation(
            summary = "Confirma anexação da solicitação médica",
            description = "Após o upload no R2, salva a chave do arquivo de solicitação no exame sem alterar o status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exame atualizado", content = @Content(schema = @Schema(implementation = ExamResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Exame não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    ExamResponse confirmExamRequestUpload(@Valid @RequestBody ConfirmExamRequestUploadRequest request) throws AuthenticationFailedException;

    @GetMapping("/exams/{id}/request-view-url")
    @Operation(summary = "URL para visualizar solicitação médica", description = "Retorna uma URL pré-assinada (GET) para visualizar o arquivo de solicitação/prescrição médica do exame.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL gerada", content = @Content(schema = @Schema(implementation = ExamResultViewUrlResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exame não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Exame sem solicitação anexada", content = @Content),
            @ApiResponse(responseCode = "503", description = "R2 não configurado", content = @Content)
    })
    ExamResultViewUrlResponse getExamRequestViewUrl(@PathVariable UUID id) throws AuthenticationFailedException;
}
