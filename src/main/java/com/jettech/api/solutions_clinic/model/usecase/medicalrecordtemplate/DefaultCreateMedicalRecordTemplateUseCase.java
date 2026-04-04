package com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jettech.api.solutions_clinic.model.entity.MedicalRecordTemplate;
import com.jettech.api.solutions_clinic.model.entity.Professional;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.MedicalRecordTemplateRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.exception.ApiError;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateMedicalRecordTemplateUseCase implements CreateMedicalRecordTemplateUseCase {

    private final MedicalRecordTemplateRepository templateRepository;
    private final TenantRepository tenantRepository;
    private final ProfessionalRepository professionalRepository;
    private final TenantContext tenantContext;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public MedicalRecordTemplateResponse execute(CreateMedicalRecordTemplateRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.tenantId());

        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.tenantId()));

        MedicalRecordTemplate template = new MedicalRecordTemplate();
        template.setTenant(tenant);
        if (request.professionalId() != null) {
            Professional professional = professionalRepository.findByIdAndTenantId(request.professionalId(), request.tenantId())
                    .orElseThrow(() -> new EntityNotFoundException("Profissional", request.professionalId()));
            if (!professional.getUser().getId().equals(tenantContext.getUserIdOrNull())) {
                throw new ForbiddenException(ApiError.ACCESS_DENIED);
            }
            template.setProfessional(professional);
        }
        template.setName(request.name());
        template.setProfessionalType(request.professionalType());
        JsonNode schemaNode = request.schema() != null ? objectMapper.valueToTree(request.schema()) : null;
        template.setSchema(schemaNode);
        template.setActive(true);

        template = templateRepository.save(template);

        return toResponse(template, null);
    }

    /** @param tenantDefaultTemplateId ID do modelo padrão do tenant (null = nenhum); usado para marcar defaultTemplate na resposta. */
    static MedicalRecordTemplateResponse toResponse(MedicalRecordTemplate template, UUID tenantDefaultTemplateId) {
        String schemaJson = template.getSchema() != null
                ? sortSchemaByOrder(template.getSchema()).toString()
                : null;
        boolean defaultTemplate = tenantDefaultTemplateId != null && template.getId().equals(tenantDefaultTemplateId);
        return new MedicalRecordTemplateResponse(
                template.getId(),
                template.getTenant() != null ? template.getTenant().getId() : null,
                template.getProfessional() != null ? template.getProfessional().getId() : null,
                template.getName(),
                template.getProfessionalType(),
                schemaJson,
                template.isReadOnly(),
                template.isActive(),
                defaultTemplate,
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    /**
     * Ordena o array de campos do schema pelo campo "order" (número); se ausente, usa o índice original.
     */
    static com.fasterxml.jackson.databind.JsonNode sortSchemaByOrder(com.fasterxml.jackson.databind.JsonNode schema) {
        if (schema == null || !schema.isArray()) return schema;
        var list = new java.util.ArrayList<com.fasterxml.jackson.databind.JsonNode>();
        schema.forEach(list::add);
        var withIndex = new java.util.ArrayList<java.util.Map.Entry<Integer, com.fasterxml.jackson.databind.JsonNode>>();
        for (int i = 0; i < list.size(); i++) {
            withIndex.add(java.util.Map.entry(i, list.get(i)));
        }
        withIndex.sort((e1, e2) -> {
            com.fasterxml.jackson.databind.JsonNode a = e1.getValue(), b = e2.getValue();
            int oa = a.has("order") && a.get("order").isNumber() ? a.get("order").asInt() : e1.getKey();
            int ob = b.has("order") && b.get("order").isNumber() ? b.get("order").asInt() : e2.getKey();
            return Integer.compare(oa, ob);
        });
        var out = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.arrayNode();
        withIndex.forEach(e -> out.add(e.getValue()));
        return out;
    }
}
