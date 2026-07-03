package com.techvedika.harmonycvi.gateway.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter
public class HashMapListConverter implements AttributeConverter<List<Map<String, Object>>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Map<String, Object>> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting list to JSON", e);
        }
    }

    @Override
    public List<Map<String, Object>> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.trim().isEmpty()) {
                return new ArrayList<>(); // return empty instead of parsing null
            }
            return objectMapper.readValue(dbData, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading JSON to list", e);
        }
    }

}
