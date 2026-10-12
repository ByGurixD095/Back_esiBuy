package esi.grupo5.esiBuy.Exception;

/** Indica un conflicto con el estado existente de un recurso. */
public class ConflictException extends BusinessException {

    /**
     * Crea un error de conflicto.
     *
     * @param message descripción del conflicto
     */
    public ConflictException(String message) {
        super(message, 409, "CONFLICT");
    }

    /**
     * Crea un error de conflicto conservando la causa.
     *
     * @param message descripción del conflicto
     * @param cause causa original del error
     */
    public ConflictException(String message, Throwable cause) {
        super(message, 409, "CONFLICT", cause);
    }
}
