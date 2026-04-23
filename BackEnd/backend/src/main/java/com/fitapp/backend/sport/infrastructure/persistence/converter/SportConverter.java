// com.fitapp.backend.infrastructure.persistence.converter/SportConverter.java
package com.fitapp.backend.sport.infrastructure.persistence.converter;

import com.fitapp.backend.sport.domain.model.SportModel;
import com.fitapp.backend.sport.infrastructure.persistence.entity.SportEntity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SportConverter {
    
    public SportModel toDomain(SportEntity entity) {
        log.debug("CONVERTER_TO_DOMAIN_START | entityId={} | entityName={}", 
                 entity.getId(), entity.getName());
        
        SportModel model = new SportModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setIsPredefined(entity.getIsPredefined());
        model.setSourceType(entity.getSourceType());
        
        if (entity.getCreatedBy() != null) {
            model.setCreatedBy(entity.getCreatedBy().getId());
            log.debug("CONVERTER_TO_DOMAIN | createdBy={}", entity.getCreatedBy().getId());
        } else {
            model.setCreatedBy(null);
        }
        
        model.setParameterTemplate(entity.getParameterTemplate());
        
        model.logModelData("CONVERTED_FROM_ENTITY");
        log.debug("CONVERTER_TO_DOMAIN_END | modelId={}", model.getId());
        
        return model;
    }

    public SportEntity toEntity(SportModel model) {
        log.debug("CONVERTER_TO_ENTITY_START | modelId={} | modelName={}", 
                 model.getId(), model.getName());
        
        model.logModelData("CONVERTING_TO_ENTITY");
        
        SportEntity entity = new SportEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setIsPredefined(model.getIsPredefined());
        entity.setSourceType(model.getSourceType());
        entity.setParameterTemplate(model.getParameterTemplate());
        
        log.debug("CONVERTER_TO_ENTITY_END | entityId={} | sourceType={}", 
                 entity.getId(), entity.getSourceType());
        
        return entity;
    }
}