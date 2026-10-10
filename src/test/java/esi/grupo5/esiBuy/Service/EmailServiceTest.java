package esi.grupo5.esiBuy.Service;

import jakarta.mail.Message;
import jakarta.mail.Transport;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class EmailServiceTest {

    private final EmailService service = new EmailService("sender@example.com", "app-password");

    @Test
    void sendHtmlEmail_componeYEnviaElMensajeSinConectarConUnServidorExterno() throws Exception {
        try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
            service.sendHtmlEmail("recipient@example.com", "Asunto de prueba", "<p>Contenido</p>");

            transport.verify(() -> Transport.send(any(Message.class)));
        }
    }

    @Test
    void sendRecoveryEmail_generaYEnviaElCorreoDeRecuperacion() throws Exception {
        try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
            service.sendRecoveryEmail(
                    "recipient@example.com", "Ana", "https://example.com/reset?token=test");

            transport.verify(() -> Transport.send(any(Message.class)));
        }
    }
}
