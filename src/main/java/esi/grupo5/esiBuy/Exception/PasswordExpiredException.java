package esi.grupo5.esiBuy.Exception;

/** Indica que la contraseña ha caducado y debe cambiarse. */
public class PasswordExpiredException extends BusinessException {

    /**
     * Crea un error de contraseña caducada.
     *
     * @param message descripción de la caducidad
     */
    public PasswordExpiredException(String message) {
        super(message, 403, "PASSWORD_EXPIRED");
    }

    /**
     * Crea un error de contraseña caducada conservando la causa.
     *
     * @param message descripción de la caducidad
     * @param cause causa original del error
     */
    public PasswordExpiredException(String message, Throwable cause) {
        super(message, 403, "PASSWORD_EXPIRED", cause);
    }
}
