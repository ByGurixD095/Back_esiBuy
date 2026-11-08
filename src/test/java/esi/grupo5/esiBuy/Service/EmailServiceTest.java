package esi.grupo5.esiBuy.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock private EmailSender emailSender;
    private EmailService service;

    @BeforeEach
    void setUp() {
        service = new DefaultEmailService(emailSender);
    }

    @Test
    void sendHtmlEmail_delegaEnElProveedorConfigurado() throws Exception {
        service.sendHtmlEmail("recipient@example.com", "Asunto de prueba", "<p>Contenido</p>");

        verify(emailSender).sendHtmlEmail(
                "recipient@example.com", "Asunto de prueba", "<p>Contenido</p>");
    }

    @Test
    void sendRecoveryEmail_componeYDelegaElCorreoDeRecuperacion() throws Exception {
        service.sendRecoveryEmail(
                "recipient@example.com", "Ana", "https://example.com/reset?token=test");

        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendHtmlEmail(
                eq("recipient@example.com"),
                eq("Recuperar contraseña de esiBuy"),
                htmlCaptor.capture());
        assertTrue(htmlCaptor.getValue().contains("Hola, Ana"));
        assertTrue(htmlCaptor.getValue().contains("https://example.com/reset?token=test"));
    }
}
