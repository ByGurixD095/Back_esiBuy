package esi.grupo5.esiBuy.Exception;

/**
 * Excepción para tokens expirados (JWT, refresh token, etc.)
 */
public class ExpiredTokenException extends BusinessException {

    public ExpiredTokenException(String message) {
        super(message, 401, "TOKEN_EXPIRED");
    }

    public ExpiredTokenException(String message, Throwable cause) {
        super(message, 401, "TOKEN_EXPIRED", cause);
    }
}
