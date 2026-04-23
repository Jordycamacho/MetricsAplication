package com.fitapp.backend.routinecomplete.routinesetemplate.aplication.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SetTemplateServiceLogger {
    
    public void logSetTemplateCreationStart(String userEmail, Long routineExerciseId) {
        log.info("SET_TEMPLATE_CREATION_INITIATED | user={} | routineExercise={}", 
                userEmail, routineExerciseId);
    }
    
    public void logSetTemplateCreationSuccess(Long setTemplateId, String userEmail) {
        log.info("SET_TEMPLATE_CREATION_SUCCESSFUL | id={} | user={}", setTemplateId, userEmail);
    }
    
    public void logSetTemplateCreationError(String userEmail, Long routineExerciseId, Exception e) {
        log.error("SET_TEMPLATE_CREATION_FAILED | user={} | routineExercise={} | error={}", 
                userEmail, routineExerciseId, e.getMessage(), e);
    }
    
    public void logSetTemplateUpdateStart(Long setTemplateId, String userEmail) {
        log.info("SET_TEMPLATE_UPDATE_INITIATED | id={} | user={}", setTemplateId, userEmail);
    }
    
    public void logSetTemplateUpdateSuccess(Long setTemplateId, String userEmail) {
        log.info("SET_TEMPLATE_UPDATE_SUCCESSFUL | id={} | user={}", setTemplateId, userEmail);
    }
    
    public void logSetTemplateUpdateError(Long setTemplateId, String userEmail, Exception e) {
        log.error("SET_TEMPLATE_UPDATE_FAILED | id={} | user={} | error={}", 
                setTemplateId, userEmail, e.getMessage(), e);
    }
    
    public void logSetTemplateRetrievalStart(Long setTemplateId, String userEmail) {
        log.debug("SET_TEMPLATE_RETRIEVAL_INITIATED | id={} | user={}", setTemplateId, userEmail);
    }
    
    public void logSetTemplateRetrievalSuccess(Long setTemplateId, String userEmail) {
        log.debug("SET_TEMPLATE_RETRIEVAL_SUCCESSFUL | id={} | user={}", setTemplateId, userEmail);
    }
    
    public void logSetTemplateRetrievalError(Long setTemplateId, String userEmail, Exception e) {
        log.error("SET_TEMPLATE_RETRIEVAL_FAILED | id={} | user={} | error={}", 
                setTemplateId, userEmail, e.getMessage(), e);
    }
    
    public void logSetTemplatesRetrievalStart(Long routineExerciseId, String userEmail) {
        log.debug("SET_TEMPLATES_RETRIEVAL_INITIATED | routineExercise={} | user={}", 
                routineExerciseId, userEmail);
    }
    
    public void logSetTemplatesRetrievalSuccess(Long routineExerciseId, int count, String userEmail) {
        log.debug("SET_TEMPLATES_RETRIEVAL_SUCCESSFUL | routineExercise={} | count={} | user={}", 
                routineExerciseId, count, userEmail);
    }
    
    public void logSetTemplatesRetrievalError(Long routineExerciseId, String userEmail, Exception e) {
        log.error("SET_TEMPLATES_RETRIEVAL_FAILED | routineExercise={} | user={} | error={}", 
                routineExerciseId, userEmail, e.getMessage(), e);
    }
    
    public void logSetTemplateDeletionStart(Long setTemplateId, String userEmail) {
        log.info("SET_TEMPLATE_DELETION_INITIATED | id={} | user={}", setTemplateId, userEmail);
    }
    
    public void logSetTemplateDeletionSuccess(Long setTemplateId, String userEmail) {
        log.info("SET_TEMPLATE_DELETION_SUCCESSFUL | id={} | user={}", setTemplateId, userEmail);
    }
    
    public void logSetTemplateDeletionError(Long setTemplateId, String userEmail, Exception e) {
        log.error("SET_TEMPLATE_DELETION_FAILED | id={} | user={} | error={}", 
                setTemplateId, userEmail, e.getMessage(), e);
    }
    
    public void logSetTemplatesBulkDeletionStart(Long routineExerciseId, String userEmail) {
        log.info("SET_TEMPLATES_BULK_DELETION_INITIATED | routineExercise={} | user={}", 
                routineExerciseId, userEmail);
    }
    
    public void logSetTemplatesBulkDeletionSuccess(Long routineExerciseId, int count, String userEmail) {
        log.info("SET_TEMPLATES_BULK_DELETION_SUCCESSFUL | routineExercise={} | count={} | user={}", 
                routineExerciseId, count, userEmail);
    }
    
    public void logSetTemplatesBulkDeletionError(Long routineExerciseId, String userEmail, Exception e) {
        log.error("SET_TEMPLATES_BULK_DELETION_FAILED | routineExercise={} | user={} | error={}", 
                routineExerciseId, userEmail, e.getMessage(), e);
    }
    
    public void logSetTemplatesReorderStart(Long routineExerciseId, int count, String userEmail) {
        log.info("SET_TEMPLATES_REORDER_INITIATED | routineExercise={} | count={} | user={}", 
                routineExerciseId, count, userEmail);
    }
    
    public void logSetTemplatesReorderSuccess(Long routineExerciseId, int count, String userEmail) {
        log.info("SET_TEMPLATES_REORDER_SUCCESSFUL | routineExercise={} | count={} | user={}", 
                routineExerciseId, count, userEmail);
    }
    
    public void logSetTemplatesReorderError(Long routineExerciseId, String userEmail, Exception e) {
        log.error("SET_TEMPLATES_REORDER_FAILED | routineExercise={} | user={} | error={}", 
                routineExerciseId, userEmail, e.getMessage(), e);
    }
    
    public void logSetTemplatesByGroupRetrievalStart(Long routineExerciseId, String groupId, String userEmail) {
        log.debug("SET_TEMPLATES_BY_GROUP_RETRIEVAL_INITIATED | routineExercise={} | group={} | user={}", 
                routineExerciseId, groupId, userEmail);
    }
    
    public void logSetTemplatesByGroupRetrievalSuccess(Long routineExerciseId, String groupId, int count, String userEmail) {
        log.debug("SET_TEMPLATES_BY_GROUP_RETRIEVAL_SUCCESSFUL | routineExercise={} | group={} | count={} | user={}", 
                routineExerciseId, groupId, count, userEmail);
    }
    
    public void logSetTemplatesByGroupRetrievalError(Long routineExerciseId, String groupId, String userEmail, Exception e) {
        log.error("SET_TEMPLATES_BY_GROUP_RETRIEVAL_FAILED | routineExercise={} | group={} | user={} | error={}", 
                routineExerciseId, groupId, userEmail, e.getMessage(), e);
    }
}