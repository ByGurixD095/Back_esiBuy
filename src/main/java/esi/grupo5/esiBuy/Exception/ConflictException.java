package esi.grupo5.esiBuy.Exception;

/**
 * Excepción para conflictos (recurso ya existe, email duplicado, etc.)
 */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message, 409, "CONFLICT");
    }

    public ConflictException(String message, Throwable cause) {
        super(message, 409, "CONFLICT", cause);
    }
}
