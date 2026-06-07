package com.fitapp.backend.notification.aplication.service;

import com.fitapp.backend.notification.aplication.port.output.EmailSenderPort;
import com.fitapp.backend.notification.aplication.port.output.EmailTemplateRendererPort;
import com.fitapp.backend.notification.domain.model.EmailMessage;
import com.fitapp.backend.notification.domain.model.EmailType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private EmailSenderPort emailSenderPort;

    @Mock
    private EmailTemplateRendererPort templateRenderer;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "baseUrl", "https://api.test.com");
        ReflectionTestUtils.setField(notificationService, "deepLinkScheme", "fitapp");
    }

    @Test
    void sendVerificationEmail_buildsCorrectVerifyUrl() {
        when(templateRenderer.render(eq(EmailType.VERIFY_EMAIL), any())).thenReturn("<html></html>");

        notificationService.sendVerificationEmail("user@test.com", "token-123");

        ArgumentCaptor<EmailMessage> messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSenderPort).send(messageCaptor.capture(), eq("<html></html>"));

        EmailMessage message = messageCaptor.getValue();
        assertThat(message.getTo()).isEqualTo("user@test.com");
        assertThat(message.getType()).isEqualTo(EmailType.VERIFY_EMAIL);
        assertThat(message.getSubject()).isEqualTo(EmailType.VERIFY_EMAIL.getDefaultSubject());
    }

    @Test
    void sendWelcomeEmail_usesWelcomeTemplate() {
        when(templateRenderer.render(eq(EmailType.WELCOME), any())).thenReturn("<html>welcome</html>");

        notificationService.sendWelcomeEmail("user@test.com", "Jordy");

        verify(emailSenderPort).send(any(EmailMessage.class), eq("<html>welcome</html>"));
        verify(templateRenderer).render(eq(EmailType.WELCOME), any());
    }
}
