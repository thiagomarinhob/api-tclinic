package com.jettech.api.solutions_clinic.model.usecase.memed;

import com.jettech.api.solutions_clinic.model.entity.MedicalDocument;
import com.jettech.api.solutions_clinic.model.entity.Professional;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.MedicalDocumentRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemedService {

    private final AppointmentRepository appointmentRepository;
    private final MedicalDocumentRepository medicalDocumentRepository;
    private final ProfessionalRepository professionalRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${memed.api.key}")
    private String memedApiKey;

    @Value("${memed.api.url}")
    private String memedApiUrl;

    @Value("${memed.secret.key}")
    private String memedSecretKey;

    /**
     * Cadastra o prescritor no Memed (se ainda não existir) e retorna o token de acesso.
     * Se o prescritor já existir, busca o token via GET.
     */
    public String generateToken(String professionalId, UUID appointmentId) {
        try {
            appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado: " + appointmentId));

            Professional professional = professionalRepository
                .findById(UUID.fromString(professionalId))
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado: " + professionalId));

            if (professional.getDocumentType() == null ||
                professional.getDocumentType().name().equals("OUTRO")) {
                throw new RuntimeException(
                    "Profissional com tipo de conselho 'OUTRO' não pode ser integrado ao Memed. " +
                    "Cadastre o conselho profissional correto (CRM, CRO, COREN, etc.)."
                );
            }

            return registerOrGetToken(professional);

        } catch (Exception e) {
            throw new RuntimeException("Falha ao obter token Memed: " + e.getMessage(), e);
        }
    }

    /**
     * Tenta POST para cadastrar o prescritor.
     * Se já existir (4xx), faz GET pelo external_id para obter o token.
     */
    private String registerOrGetToken(Professional professional) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/vnd.api+json");

        String baseUrl = memedApiUrl + "/sinapse-prescricao/usuarios"
                       + "?api-key=" + memedApiKey
                       + "&secret-key=" + memedSecretKey;

        // Monta o objeto board (conselho profissional)
        Map<String, Object> board = new HashMap<>();
        board.put("board_code",   professional.getDocumentType().name());
        board.put("board_number", professional.getDocumentNumber());
        board.put("board_state",  professional.getDocumentState() != null
                                    ? professional.getDocumentState() : "");

        // Monta os atributos do prescritor
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("external_id",     professional.getId().toString());
        attributes.put("nome",            professional.getUser().getFirstName());
        attributes.put("sobrenome",       professional.getUser().getLastName() != null
                                            ? professional.getUser().getLastName() : "");
        attributes.put("cpf",             professional.getUser().getCpf() != null
                                            ? professional.getUser().getCpf() : "");
        attributes.put("board",           board);

        // birthDate já está no formato DD/MM/YYYY (String), que é o exigido pela Memed
        attributes.put("data_nascimento", professional.getUser().getBirthDate() != null
                                            ? professional.getUser().getBirthDate() : "");

        if (professional.getUser().getEmail() != null)
            attributes.put("email", professional.getUser().getEmail());

        if (professional.getUser().getPhone() != null)
            attributes.put("telefone", professional.getUser().getPhone());

        Map<String, Object> data = new HashMap<>();
        data.put("type",       "usuarios");
        data.put("attributes", attributes);

        Map<String, Object> body = new HashMap<>();
        body.put("data", data);

        try {
            // POST — cadastro do prescritor
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
            return parseToken(response.getBody());

        } catch (HttpClientErrorException ex) {
            int status = ex.getStatusCode().value();
            String responseBody = ex.getResponseBodyAsString();

            // Memed retorna 409/422, ou 400 com code "external_id" quando o prescritor
            // já está cadastrado para este parceiro — nesse caso buscamos o token via GET
            boolean alreadyExists = status == 409
                || status == 422
                || responseBody.contains("\"external_id\"");

            if (alreadyExists) {
                return getTokenByExternalId(professional.getId().toString(), headers);
            }

            // Outros 4xx são erros reais de validação — expõe o detalhe para diagnóstico
            throw new RuntimeException(
                "Erro ao cadastrar prescritor no Memed (HTTP " + status + "): " + responseBody, ex
            );
        }
    }

    /**
     * Busca o token de um prescritor já cadastrado usando o external_id como identificador.
     */
    private String getTokenByExternalId(String externalId, HttpHeaders headers) throws Exception {
        String url = memedApiUrl + "/sinapse-prescricao/usuarios/" + externalId
                   + "?api-key=" + memedApiKey
                   + "&secret-key=" + memedSecretKey;

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, request, String.class
        );
        return parseToken(response.getBody());
    }

    /** Extrai o token do JSON:API retornado pelo Memed. */
    private String parseToken(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        return root.get("data").get("attributes").get("token").asText();
    }

    // ─── Documentos ────────────────────────────────────────────────────────────

    public MedicalDocumentResponse saveDocument(UUID appointmentId,
                                                String documentUrl,
                                                String documentType) {
        appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Agendamento não encontrado: " + appointmentId));

        MedicalDocument document = new MedicalDocument();
        document.setId(UUID.randomUUID());
        document.setAppointmentId(appointmentId);
        document.setDocumentUrl(documentUrl);
        document.setDocumentType(documentType);
        document.setSource("MEMED");
        document.setCreatedAt(LocalDateTime.now());

        return mapToResponse(medicalDocumentRepository.save(document));
    }

    /**
     * Busca a URL do PDF da prescrição na API do Memed e salva como documento.
     * Endpoint: GET /v1/prescricoes/{id}/url-document/full?token={userToken}
     */
    public MedicalDocumentResponse saveDocumentWithPdf(UUID appointmentId,
                                                       String prescriptionId,
                                                       String userToken,
                                                       String documentType) {
        appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Agendamento não encontrado: " + appointmentId));

        String pdfUrl = fetchPdfUrl(prescriptionId, userToken);

        MedicalDocument document = new MedicalDocument();
        document.setId(UUID.randomUUID());
        document.setAppointmentId(appointmentId);
        document.setDocumentUrl(pdfUrl);
        document.setDocumentType(documentType);
        document.setSource("MEMED");
        document.setCreatedAt(LocalDateTime.now());

        return mapToResponse(medicalDocumentRepository.save(document));
    }

    /**
     * Consulta a URL do PDF completo de uma prescrição via Memed.
     */
    private String fetchPdfUrl(String prescriptionId, String userToken) {
        try {
            String url = memedApiUrl + "/prescricoes/" + prescriptionId
                       + "/url-document/full?token=" + userToken;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, request, String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            // Resposta: { "data": { "attributes": { "url": "https://..." } } }
            return root.get("data").get("attributes").get("url").asText();

        } catch (Exception e) {
            throw new RuntimeException("Falha ao buscar PDF da prescrição: " + e.getMessage(), e);
        }
    }

    public List<MedicalDocumentResponse> getDocumentsByAppointment(UUID appointmentId) {
        return medicalDocumentRepository
            .findByAppointmentId(appointmentId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private MedicalDocumentResponse mapToResponse(MedicalDocument doc) {
        MedicalDocumentResponse r = new MedicalDocumentResponse();
        r.setId(doc.getId());
        r.setAppointmentId(doc.getAppointmentId());
        r.setDocumentUrl(doc.getDocumentUrl());
        r.setDocumentType(doc.getDocumentType());
        r.setSource(doc.getSource());
        r.setCreatedAt(doc.getCreatedAt());
        return r;
    }
}
