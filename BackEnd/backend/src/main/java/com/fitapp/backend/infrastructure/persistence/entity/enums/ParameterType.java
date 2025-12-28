package com.fitapp.backend.infrastructure.persistence.entity.enums;

public enum ParameterType {
    NUMBER("number", Double.class),
    INTEGER("integer", Integer.class),
    TEXT("text", String.class),
    BOOLEAN("boolean", Boolean.class),
    DURATION("duration", Long.class),
    DISTANCE("distance", Double.class),
    PERCENTAGE("percentage", Double.class);
    
    private final String typeName;
    private final Class<?> dataType;
    
    ParameterType(String typeName, Class<?> dataType) {
        this.typeName = typeName;
        this.dataType = dataType;
    }
    
    public String getTypeName() { return typeName; }
    public Class<?> getDataType() { return dataType; }
}