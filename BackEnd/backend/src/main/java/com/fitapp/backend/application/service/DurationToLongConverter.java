package com.fitapp.backend.application.service;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Duration;

@Converter(autoApply = true)
public class DurationToLongConverter implements AttributeConverter<Duration, Long> {
    @Override
    public Long convertToDatabaseColumn(Duration duration) {
        return duration != null ? duration.toSeconds() : null;
    }

    @Override
    public Duration convertToEntityAttribute(Long dbData) {
        return dbData != null ? Duration.ofSeconds(dbData) : null;
    }
}

