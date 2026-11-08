package esi.grupo5.esiBuy.Service;

import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;

/** Compone los mensajes de correo de la aplicación y delega el transporte. */
@Service
public class DefaultEmailService implements EmailService {
    private final EmailSender emailSender;

    /**
     * Crea el servicio de correo con el proveedor de transporte seleccionado.
     *
     * @param emailSender proveedor de transporte configurado
     */
    public DefaultEmailService(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    /**
     * Envía un mensaje HTML usando el proveedor configurado.
     *
     * @param to dirección del destinatario
     * @param subject asunto del mensaje
     * @param htmlContent contenido HTML
     * @throws MessagingException si falla la composición o el envío
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        emailSender.sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Compone y envía el correo con el enlace de recuperación de contraseña.
     *
     * @param to dirección del destinatario
     * @param nombre nombre que se muestra en el saludo
     * @param resetLink enlace de recuperación insertado en el mensaje
     * @throws MessagingException si falla el envío
     */
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
                
        sendHtmlEmail(to, subject, htmlContent);
    }
}
