package esi.grupo5.esiBuy.Service;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String TIMEOUT_PROPERTY = "10000"; // 10 seconds

    private final String username;
    private final String appPassword;

    public EmailService(
        @Value("${mail.username}") String username,
        @Value("${mail.password}") String appPassword
    ) {
        this.username = username;
        this.appPassword = appPassword;
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.connectiontimeout", TIMEOUT_PROPERTY);
        props.put("mail.smtp.timeout", TIMEOUT_PROPERTY);
        props.put("mail.smtp.writetimeout", TIMEOUT_PROPERTY);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");
        message.setContent(htmlContent, "text/html; charset=UTF-8");
        logger.info("Enviando email a {} con asunto '{}'", to, subject);

        try {
            Transport.send(message);
        } catch (MessagingException e) {
            logger.error("Error al enviar email a {}: {}", to, e.getMessage());
            throw e;
        }
        logger.info("Email enviado correctamente a {}", to);
    }
        public void sendRecoveryEmail(String to, String nombre, String resetLink) throws MessagingException {
        String subject = "Recuperar contraseña de esiBuy";
        String nombreApp = "esiBuy";

        String htmlContent = """
                <html>
                    <!DOCTYPE html>
                    <html lang="es" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office">
                    <head>
                        <meta charset="utf-8">
                        <meta name="color-scheme" content="light">
                        <meta name="supported-color-schemes" content="light">
                        <style>
                            :root { color-scheme: light; }
                        </style>
                    </head>
                    <body style="margin:0; padding:0; background-color:#F4F6F8; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">

                        <!-- Cabecera Azul (Estilo corporativo/universitario) -->
                        <div style="background-color:#1976D2; padding:20px; text-align:center; border-bottom: 4px solid #115293;">
                            <h1 style="color:#ffffff; margin:0; font-size:26px; font-weight:bold; letter-spacing: 1px;">
                                %s
                            </h1>
                        </div>

                        <!-- Cuerpo del Mensaje -->
                        <div style="padding:40px 20px; text-align:center;">

                            <h2 style="color:#333333; font-size:28px; margin-bottom:15px;">
                                Hola, %s
                            </h2>

                            <p style="color:#555555; font-size:16px; line-height: 1.6; max-width: 500px; margin: 0 auto 10px auto;">
                                ¿Has olvidado tu contraseña? No te preocupes, puedes restablecerla fácilmente.
                            </p>

                            <p style="color:#555555; font-size:16px; line-height: 1.6; max-width: 500px; margin: 0 auto 30px auto;">
                                Haz clic en el siguiente botón para configurar una nueva contraseña. Por seguridad, este enlace caducará en <strong>5 minutos</strong>.
                            </p>

                            <!-- Botón Redondeado Azul -->
                            <a href="%s"
                               style="
                                    display:inline-block;
                                    padding:14px 32px;
                                    font-size:16px;
                                    font-weight:bold;
                                    color:#ffffff;
                                    text-decoration:none;
                                    border-radius:50px;
                                    background: linear-gradient(90deg, #1976D2, #115293);
                                    box-shadow: 0px 4px 10px rgba(25, 118, 210, 0.3);
                               ">
                                Cambiar contraseña
                            </a>
                            
                            <p style="color:#777777; font-size:14px; margin-top:35px; max-width: 500px; margin-left: auto; margin-right: auto;">
                                Si no has solicitado este cambio, puedes ignorar este correo. Tu cuenta sigue estando segura.
                            </p>

                        </div>

                        <!-- Pie de página -->
                        <div style="text-align:center; padding:20px; font-size:13px; color:#777777; border-top: 1px solid #E0E0E0;">
                            <p style="margin: 0 0 5px 0;">El equipo de %s</p>
                            <p style="margin: 0;">© 2026 - Todos los derechos reservados</p>
                        </div>

                    </body>
                </html>
                """.formatted(nombreApp, nombre, resetLink, nombreApp);
                
        logger.info("Preparando email de recuperación de contraseña para {}", to);

        sendHtmlEmail(to, subject, htmlContent);
    }
}