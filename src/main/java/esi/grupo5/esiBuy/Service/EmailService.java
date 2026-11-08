package esi.grupo5.esiBuy.Service;

import jakarta.mail.MessagingException;

/** Operaciones de correo usadas por los servicios de la aplicación. */
public interface EmailService {

    /** Envía contenido HTML mediante el proveedor configurado. */
    void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException;

    /** Envía el enlace de recuperación de contraseña. */
    void sendRecoveryEmail(String to, String nombre, String resetLink) throws MessagingException;
}
