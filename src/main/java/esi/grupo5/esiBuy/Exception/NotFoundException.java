package esi.grupo5.esiBuy.Exception;

/**
 * Excepción para cuando un recurso no se encuentra (usuario, producto, etc.)
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(message, 404, "NOT_FOUND");
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, 404, "NOT_FOUND", cause);
    }
}
