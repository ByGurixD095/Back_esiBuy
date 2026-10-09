package esi.grupo5.esiBuy.Exception;

/**
 * Excepción para errores de validación (campos vacíos, formato incorrecto, etc.)
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(message, 400, "VALIDATION_ERROR");
    }

    public ValidationException(String message, Throwable cause) {
        super(message, 400, "VALIDATION_ERROR", cause);
    }
}
