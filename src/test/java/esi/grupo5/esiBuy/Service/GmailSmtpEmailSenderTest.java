package esi.grupo5.esiBuy.Service;

import jakarta.mail.Message;
import jakarta.mail.Transport;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class GmailSmtpEmailSenderTest {

    @Test
    void sendHtmlEmail_enviaPorGmailSmtp() throws Exception {
        GmailSmtpEmailSender sender = new GmailSmtpEmailSender("sender@example.com", "app-password");

        try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
            sender.sendHtmlEmail("recipient@example.com", "Asunto", "<p>Contenido</p>");

            transport.verify(() -> Transport.send(any(Message.class)));
        }
    }
}
