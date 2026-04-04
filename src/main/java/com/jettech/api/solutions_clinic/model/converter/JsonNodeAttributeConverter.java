package com.jettech.api.solutions_clinic.model.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

/**
 * Converte entre JsonNode (entidade) e JSONB (coluna) para persistência em PostgreSQL.
 * Na escrita usa PGobject para o driver enviar o valor como tipo jsonb (evita erro varchar).
 * Na leitura aceita PGobject ou String conforme a versão do driver PostgreSQL.
 */
@Converter(autoApply = false)
public class JsonNodeAttributeConverter implements AttributeConverter<JsonNode, Object> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String JSONB_TYPE = "jsonb";

    @Override
    public Object convertToDatabaseColumn(JsonNode attribute) {
        if (attribute == null || attribute.isNull()) {
            return null;
        }
        try {
            PGobject pg = new PGobject();
            pg.setType(JSONB_TYPE);
            pg.setValue(MAPPER.writeValueAsString(attribute));
            return pg;
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao serializar JSON para coluna", e);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(Object dbData) {
        String json = toJsonString(dbData);
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Falha ao deserializar JSON da coluna", e);
        }
    }

    private static String toJsonString(Object dbData) {
        if (dbData == null) {
            return null;
        }
        if (dbData instanceof PGobject pg) {
            return pg.getValue();
        }
        if (dbData instanceof String s) {
            return s;
        }
        return dbData.toString();
    }
}
