package esi.grupo5.esiBuy.Exception;

/** Indica que los datos proporcionados no superan una validación de negocio. */
public class ValidationException extends BusinessException {

    /**
     * Crea un error de validación.
     *
     * @param message detalle de los datos no válidos
     */
    public ValidationException(String message) {
        super(message, 400, "VALIDATION_ERROR");
    }

    /**
     * Crea un error de validación conservando la causa.
     *
     * @param message detalle de los datos no válidos
     * @param cause causa original del error
     */
    public ValidationException(String message, Throwable cause) {
        super(message, 400, "VALIDATION_ERROR", cause);
    }
}
