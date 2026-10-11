package esi.grupo5.esiBuy.Exception;

/** Excepción base de negocio con el estado HTTP y el código expuestos por la API. */
public class BusinessException extends RuntimeException {

    /** Estado HTTP asociado al error. */
    private final int httpStatusCode;
    /** Código estable que identifica el error en la respuesta. */
    private final String errorCode;

    /**
     * Crea una excepción de negocio sin causa subyacente.
     *
     * @param message mensaje descriptivo del error
     * @param httpStatusCode estado HTTP que debe devolverse
     * @param errorCode código identificador del error
     */
    public BusinessException(String message, int httpStatusCode, String errorCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
    }

    /**
     * Crea una excepción de negocio conservando la causa original.
     *
     * @param message mensaje descriptivo del error
     * @param httpStatusCode estado HTTP que debe devolverse
     * @param errorCode código identificador del error
     * @param cause causa original del error
     */
    public BusinessException(String message, int httpStatusCode, String errorCode, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
    }

    /**
     * Devuelve el estado HTTP asociado a esta excepción.
     *
     * @return código de estado HTTP
     */
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * Devuelve el código estable del error.
     *
     * @return código de error
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Representa la excepción mediante su código y mensaje.
     *
     * @return representación textual de la excepción
     */
    @Override
    public String toString() {
        return String.format("BusinessException[errorCode=%s, message=%s]", errorCode, getMessage());
    }
}
