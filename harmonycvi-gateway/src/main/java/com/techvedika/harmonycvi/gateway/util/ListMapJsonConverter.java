package com.techvedika.harmonycvi.gateway.util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Converter
public class ListMapJsonConverter implements AttributeConverter<List<Map<String, Object>>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Map<String, Object>> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting List<Map> to JSON", e);
        }
    }

    @Override
    public List<Map<String, Object>> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(dbData, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON to List<Map>", e);
        }
    }
}
