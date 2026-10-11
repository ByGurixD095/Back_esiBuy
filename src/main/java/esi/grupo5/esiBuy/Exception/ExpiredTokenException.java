package esi.grupo5.esiBuy.Exception;

/** Indica que un token no es válido porque ha expirado. */
public class ExpiredTokenException extends BusinessException {

    /**
     * Crea un error de token expirado.
     *
     * @param message descripción del error
     */
    public ExpiredTokenException(String message) {
        super(message, 401, "TOKEN_EXPIRED");
    }

    /**
     * Crea un error de token expirado conservando la causa.
     *
     * @param message descripción del error
     * @param cause causa original del error
     */
    public ExpiredTokenException(String message, Throwable cause) {
        super(message, 401, "TOKEN_EXPIRED", cause);
    }
}
