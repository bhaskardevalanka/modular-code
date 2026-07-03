package com.techvedika.harmonycvi.gateway.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonListObjectConverter implements AttributeConverter<List<Object>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Object> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    @Override
    public List<Object> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.trim().isEmpty()) {
                return new ArrayList<Object>();
            }
            return objectMapper.readValue(dbData, new TypeReference<List<Object>>() {});
        } catch (IOException e) {
            throw new RuntimeException("JSON deserialization failed: " + dbData, e);
        }
    }

}
