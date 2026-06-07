package com.fitapp.backend.notification.infrastructure.template;

import com.fitapp.backend.notification.aplication.port.output.EmailTemplateRendererPort;
import com.fitapp.backend.notification.domain.model.EmailType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ThymeleafEmailTemplateRenderer implements EmailTemplateRendererPort {

    private final SpringTemplateEngine templateEngine;

    @Override
    public String render(EmailType type, Map<String, Object> variables) {
        Context context = new Context(Locale.forLanguageTag("es"));
        context.setVariables(variables);
        return templateEngine.process(type.getTemplateName(), context);
    }
}
