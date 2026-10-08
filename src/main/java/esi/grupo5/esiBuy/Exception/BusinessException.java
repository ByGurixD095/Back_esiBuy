package esi.grupo5.esiBuy.Exception;

/**
 * Excepción base para errores de negocio en el sistema ESI Buy.
 * Todas las excepciones personalizadas deben extender esta clase.
 */
public class BusinessException extends RuntimeException {

    private final int httpStatusCode;
    private final String errorCode;

    public BusinessException(String message, int httpStatusCode, String errorCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
    }

    public BusinessException(String message, int httpStatusCode, String errorCode, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return String.format("BusinessException[errorCode=%s, message=%s]", errorCode, getMessage());
    }
}
