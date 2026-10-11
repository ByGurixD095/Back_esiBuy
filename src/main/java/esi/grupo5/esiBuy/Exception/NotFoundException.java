package esi.grupo5.esiBuy.Exception;

/** Indica que no se ha encontrado el recurso solicitado. */
public class NotFoundException extends BusinessException {

    /**
     * Crea un error de recurso inexistente.
     *
     * @param message descripción del recurso no encontrado
     */
    public NotFoundException(String message) {
        super(message, 404, "NOT_FOUND");
    }

    /**
     * Crea un error de recurso inexistente conservando la causa.
     *
     * @param message descripción del recurso no encontrado
     * @param cause causa original del error
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, 404, "NOT_FOUND", cause);
    }
}
