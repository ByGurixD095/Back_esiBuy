package esi.grupo5.esiBuy.Exception;

/** Error de límite de peticiones con el tiempo mínimo antes del siguiente intento. */
public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(long retryAfterSeconds) {
        super("Demasiados intentos. Prueba de nuevo más tarde.");
        this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
