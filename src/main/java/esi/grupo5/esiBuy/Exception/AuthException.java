package esi.grupo5.esiBuy.Exception;

/**
 * Excepción para errores de autenticación (login fallido, credenciales inválidas, etc.)
 */
public class AuthException extends BusinessException {

    public AuthException(String message) {
        super(message, 401, "AUTH_ERROR");
    }

    public AuthException(String message, Throwable cause) {
        super(message, 401, "AUTH_ERROR", cause);
    }
}
