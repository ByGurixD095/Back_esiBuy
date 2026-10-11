package esi.grupo5.esiBuy.Exception;

/** Indica que la operación no está permitida para el usuario o estado actual. */
public class ForbiddenException extends BusinessException {

    /**
     * Crea un error de acceso prohibido.
     *
     * @param message motivo por el que se deniega la operación
     */
    public ForbiddenException(String message) {
        super(message, 403, "FORBIDDEN");
    }

    /**
     * Crea un error de acceso prohibido conservando la causa.
     *
     * @param message motivo por el que se deniega la operación
     * @param cause causa original del error
     */
    public ForbiddenException(String message, Throwable cause) {
        super(message, 403, "FORBIDDEN", cause);
    }
}
