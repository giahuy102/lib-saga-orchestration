package com.huyle.ms.saga.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huyle.ms.saga.entity.SagaStep;

import javax.persistence.AttributeConverter;
import java.util.List;

public class ListAttributeConverter implements AttributeConverter<List<SagaStep>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<SagaStep> steps) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(steps);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<SagaStep> convertToEntityAttribute(String stepStr) {
        try {
            return mapper.readValue(stepStr, new TypeReference<List<SagaStep>>() {});
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
