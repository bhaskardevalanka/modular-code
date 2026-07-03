package com.techvedika.harmonycvi.gateway.util;

import java.util.ArrayList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class JsonArrayListAnyConverter implements AttributeConverter<ArrayList<?>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ArrayList<?> attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting ArrayList<?> to JSON", e);
        }
    }

    @Override
    public ArrayList<?> convertToEntityAttribute(String dbData) {
        try {
        	if (dbData == null || dbData.trim().isEmpty()) {
                return new ArrayList<>(); // return empty instead of parsing null
            }
            return objectMapper.readValue(dbData, new TypeReference<ArrayList<?>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON to ArrayList<?>", e);
        }
    }
}