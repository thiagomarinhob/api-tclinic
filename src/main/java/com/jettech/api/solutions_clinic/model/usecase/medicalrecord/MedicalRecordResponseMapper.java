package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jettech.api.solutions_clinic.model.entity.MedicalRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Converte MedicalRecord (com JsonNode) para MedicalRecordResponse com Map,
 * para que a API retorne JSON limpo (sem propriedades internas do JsonNode).
 */
@Component
@RequiredArgsConstructor
public class MedicalRecordResponseMapper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public MedicalRecordResponse toResponse(MedicalRecord record) {
        var appointment = record.getAppointment();
        String patientName = appointment.getPatient() != null
                ? nullToEmpty(appointment.getPatient().getFirstName())
                : null;
        String professionalName = null;
        if (appointment.getProfessional() != null && appointment.getProfessional().getUser() != null) {
            var user = appointment.getProfessional().getUser();
            professionalName = (nullToEmpty(user.getFirstName()) + " " + nullToEmpty(user.getLastName())).trim();
            if (professionalName.isEmpty()) professionalName = null;
        }
        return new MedicalRecordResponse(
                record.getId(),
                appointment.getId(),
                record.getTemplate().getId(),
                patientName,
                professionalName,
                toMap(record.getContent()),
                toMap(record.getVitalSigns()),
                record.getSignedAt(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private Map<String, Object> toMap(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return objectMapper.convertValue(node, MAP_TYPE);
    }
}
