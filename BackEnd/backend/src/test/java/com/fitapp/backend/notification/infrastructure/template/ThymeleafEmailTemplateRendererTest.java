package com.fitapp.backend.notification.infrastructure.template;

import com.fitapp.backend.notification.domain.model.EmailType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ThymeleafEmailTemplateRendererTest {

    private ThymeleafEmailTemplateRenderer renderer;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        renderer = new ThymeleafEmailTemplateRenderer(engine);
    }

    @Test
    void renderVerifyEmail_containsVerifyLink() {
        String html = renderer.render(EmailType.VERIFY_EMAIL, Map.of(
                "verifyUrl", "https://api.test.com/api/auth/verify-email?token=abc",
                "expiryHours", 24
        ));

        assertThat(html).contains("Verifica tu correo");
        assertThat(html).contains("https://api.test.com/api/auth/verify-email?token=abc");
    }

    @Test
    void renderWelcome_containsBranding() {
        String html = renderer.render(EmailType.WELCOME, Map.of(
                "fullName", "Ana",
                "appDeepLink", "fitapp://auth/callback"
        ));

        assertThat(html).contains("JNOBFIT");
        assertThat(html).contains("Ana");
    }
}
