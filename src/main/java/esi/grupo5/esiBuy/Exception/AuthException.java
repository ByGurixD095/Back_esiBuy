package esi.grupo5.esiBuy.Exception;

/** Indica un fallo de autenticación o credenciales no válidas. */
public class AuthException extends BusinessException {

    /**
     * Crea un error de autenticación.
     *
     * @param message descripción del fallo
     */
    public AuthException(String message) {
        super(message, 401, "AUTH_ERROR");
    }

    /**
     * Crea un error de autenticación conservando la causa.
     *
     * @param message descripción del fallo
     * @param cause causa original del error
     */
    public AuthException(String message, Throwable cause) {
        super(message, 401, "AUTH_ERROR", cause);
    }
}
