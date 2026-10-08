package esi.grupo5.esiBuy.Exception;

/**
 * Excepción para errores de autorización (no tienes permisos, cuenta bloqueada, etc.)
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(message, 403, "FORBIDDEN");
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, 403, "FORBIDDEN", cause);
    }
}
