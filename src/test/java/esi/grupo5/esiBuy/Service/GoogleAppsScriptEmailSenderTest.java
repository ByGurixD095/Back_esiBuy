package esi.grupo5.esiBuy.Service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoogleAppsScriptEmailSenderTest {
    private static final String SCRIPT_URL = "https://script.google.com/macros/s/deployment-id/exec";

    private HttpClient httpClient;
    private HttpResponse<String> httpResponse;
    private GoogleAppsScriptEmailSender sender;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        httpClient = mock(HttpClient.class);
        httpResponse = (HttpResponse<String>) mock(HttpResponse.class);
        sender = new GoogleAppsScriptEmailSender(
                SCRIPT_URL, "bridge-secret", new ObjectMapper(), httpClient);
    }

    @Test
    void sendHtmlEmail_enviaJsonAlEndpointDeAppsScript() throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"ok\":true}");
        when(httpClient.send(any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponse);

        sender.sendHtmlEmail("user@example.com", "OTP", "<p>123456</p>");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
        HttpRequest request = requestCaptor.getValue();
        assertEquals("POST", request.method());
        assertEquals(SCRIPT_URL, request.uri().toString());
        assertEquals("application/json; charset=UTF-8", request.headers()
                .firstValue("Content-Type").orElseThrow());
    }

    @Test
    void sendHtmlEmail_propagaElRechazoDelBridge() throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"ok\":false,\"error\":\"invalid_request\"}");
        when(httpClient.send(any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponse);

        MessagingException error = assertThrows(MessagingException.class,
                () -> sender.sendHtmlEmail("user@example.com", "OTP", "<p>123456</p>"));

        assertTrue(error.getMessage().contains("invalid_request"));
    }

    @Test
    void sendHtmlEmail_rechazaErroresHttp() throws Exception {
        when(httpResponse.statusCode()).thenReturn(503);
        when(httpClient.send(any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponse);

        MessagingException error = assertThrows(MessagingException.class,
                () -> sender.sendHtmlEmail("user@example.com", "OTP", "<p>123456</p>"));

        assertTrue(error.getMessage().contains("503"));
    }

    @Test
    void constructor_exigeUrlAppsScriptHttpsYSecreto() {
        assertThrows(IllegalStateException.class,
                () -> new GoogleAppsScriptEmailSender("", "bridge-secret", new ObjectMapper(), httpClient));
        assertThrows(IllegalStateException.class,
                () -> new GoogleAppsScriptEmailSender(
                        "http://script.google.com/macros/s/deployment-id/exec",
                        "bridge-secret", new ObjectMapper(), httpClient));
        assertThrows(IllegalStateException.class,
                () -> new GoogleAppsScriptEmailSender(SCRIPT_URL, "", new ObjectMapper(), httpClient));
    }
}
