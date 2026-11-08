package esi.grupo5.esiBuy.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.MessagingException;

/** Transporte HTTPS hacia una Web App de Google Apps Script que envía mediante Gmail. */
@Component
@ConditionalOnProperty(prefix = "app.email", name = "provider", havingValue = "google-apps-script")
public class GoogleAppsScriptEmailSender implements EmailSender {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    private final String scriptUrl;
    private final String bridgeSecret;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public GoogleAppsScriptEmailSender(
            @Value("${google.script.url:}") String scriptUrl,
            @Value("${google.script.secret:}") String bridgeSecret,
            ObjectMapper objectMapper) {
        this(scriptUrl, bridgeSecret, objectMapper, createHttpClient());
    }

    GoogleAppsScriptEmailSender(
            String scriptUrl,
            String bridgeSecret,
            ObjectMapper objectMapper,
            HttpClient httpClient) {
        if (scriptUrl == null || scriptUrl.isBlank() || bridgeSecret == null || bridgeSecret.isBlank()) {
            throw new IllegalStateException(
                    "Google Apps Script email provider requires GOOGLE_SCRIPT_URL and BRIDGE_SECRET");
        }
        URI endpoint = URI.create(scriptUrl);
        if (!"https".equalsIgnoreCase(endpoint.getScheme())
                || !"script.google.com".equalsIgnoreCase(endpoint.getHost())
                || !endpoint.getPath().endsWith("/exec")) {
            throw new IllegalStateException("GOOGLE_SCRIPT_URL must be an HTTPS Apps Script /exec URL");
        }
        this.scriptUrl = scriptUrl;
        this.bridgeSecret = bridgeSecret;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    private static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        try {
            String requestBody = objectMapper.writeValueAsString(
                    new ScriptEmailRequest(bridgeSecret, to, subject, htmlContent));
            HttpRequest request = HttpRequest.newBuilder(URI.create(scriptUrl))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new MessagingException("Google Apps Script returned HTTP " + response.statusCode());
            }

            JsonNode responseBody = objectMapper.readTree(response.body());
            if (!responseBody.path("ok").asBoolean(false)) {
                String reason = responseBody.path("error").asText("unknown_error");
                throw new MessagingException("Google Apps Script rejected email request: " + reason);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MessagingException("Interrupted while calling Google Apps Script", e);
        } catch (IOException | IllegalArgumentException e) {
            throw new MessagingException("Unable to call Google Apps Script email bridge", e);
        }
    }

    private record ScriptEmailRequest(String secret, String to, String subject, String html) { }
}
