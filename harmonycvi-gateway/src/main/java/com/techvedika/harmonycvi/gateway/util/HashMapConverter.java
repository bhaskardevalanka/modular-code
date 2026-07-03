package com.techvedika.harmonycvi.gateway.util;

import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.io.IOException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class HashMapConverter implements AttributeConverter<HashMap<String, Object>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(HashMap<String, Object> attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error serializing HashMap", e);
        }
    }

    @Override
    public HashMap<String, Object> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : objectMapper.readValue(dbData, new TypeReference<HashMap<String, Object>>() {});
        } catch (IOException | JsonProcessingException e) {
            throw new IllegalArgumentException("Error deserializing HashMap", e);
        }
    }
}