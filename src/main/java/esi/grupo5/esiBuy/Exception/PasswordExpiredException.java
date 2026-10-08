package esi.grupo5.esiBuy.Exception;

/**
 * Excepción para cuando la contraseña ha caducado y debe cambiarse
 */
public class PasswordExpiredException extends BusinessException {

    public PasswordExpiredException(String message) {
        super(message, 403, "PASSWORD_EXPIRED");
    }

    public PasswordExpiredException(String message, Throwable cause) {
        super(message, 403, "PASSWORD_EXPIRED", cause);
    }
}
