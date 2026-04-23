package com.fitapp.backend.routinecomplete.routinesetemplate.domain.model;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SetTemplateMetrics {

    private final Counter setTemplatesCreated;
    private final Counter setTemplatesDeleted;
    private final Counter setTemplatesUpdated;
    private final Counter setParametersCreated;
    private final Timer setTemplateCreationTimer;
    private final Timer setTemplateQueryTimer;

    public SetTemplateMetrics(MeterRegistry registry) {
        this.setTemplatesCreated = Counter.builder("fitapp.set_templates.created")
                .description("Total set templates created")
                .register(registry);
        this.setTemplatesDeleted = Counter.builder("fitapp.set_templates.deleted")
                .description("Total set templates deleted")
                .register(registry);
        this.setTemplatesUpdated = Counter.builder("fitapp.set_templates.updated")
                .description("Total set templates updated")
                .register(registry);
        this.setParametersCreated = Counter.builder("fitapp.set_parameters.created")
                .description("Total set parameters created")
                .register(registry);
        this.setTemplateCreationTimer = Timer.builder("fitapp.set_templates.creation.duration")
                .description("Time to create a set template including parameters")
                .register(registry);
        this.setTemplateQueryTimer = Timer.builder("fitapp.set_templates.query.duration")
                .description("Time to query set templates by routine exercise")
                .register(registry);
    }

    public void recordCreated() {
        setTemplatesCreated.increment();
    }

    public void recordDeleted() {
        setTemplatesDeleted.increment();
    }

    public void recordUpdated() {
        setTemplatesUpdated.increment();
    }

    public void recordParametersCreated(int count) {
        setParametersCreated.increment(count);
    }

    public Timer.Sample startCreationTimer() {
        return Timer.start();
    }

    public void stopCreationTimer(Timer.Sample sample) {
        if (sample != null)
            sample.stop(setTemplateCreationTimer);
    }

    public Timer.Sample startQueryTimer() {
        return Timer.start();
    }

    public void stopQueryTimer(Timer.Sample sample) {
        if (sample != null)
            sample.stop(setTemplateQueryTimer);
    }
}