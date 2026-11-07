package esi.grupo5.esiBuy.Config;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import esi.grupo5.esiBuy.Exception.BusinessException;

/** Traduce las excepciones de negocio a respuestas HTTP uniformes. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Convierte una excepción de negocio en una respuesta JSON con estado, código y mensaje.
     *
     * @param ex excepción que contiene el estado HTTP, el código y el mensaje
     * @return respuesta con estado y cuerpo derivados de la excepción
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> manejarErrorDeNegocio(BusinessException ex) {
        if (ex.getHttpStatusCode() >= 500) {
            return buildInternalErrorResponse(ex);
        }
        return buildErrorResponse(ex.getHttpStatusCode(), ex.getMessage(), ex.getErrorCode());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<Map<String, Object>> manejarErrorDeValidacion(Exception ex) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Revisa los datos enviados e inténtalo de nuevo.",
                "VALIDATION_ERROR");
    }

    @ExceptionHandler({
            ResponseStatusException.class,
            NoResourceFoundException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class
    })
    public ResponseEntity<Map<String, Object>> manejarErrorHttp(Exception ex) {
        return buildHttpErrorResponse((ErrorResponse) ex, ex);
    }

    private ResponseEntity<Map<String, Object>> buildHttpErrorResponse(ErrorResponse error, Exception ex) {
        HttpStatusCode status = error.getStatusCode();
        if (status.is4xxClientError()) {
            String message = status.value() == HttpStatus.NOT_FOUND.value()
                    ? "No se encontró el recurso solicitado."
                    : "No se pudo procesar la solicitud. Revisa los datos e inténtalo de nuevo.";
            return buildErrorResponse(status.value(), message, "REQUEST_ERROR");
        }
        return buildInternalErrorResponse(ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErrorInterno(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            return buildHttpErrorResponse(errorResponse, ex);
        }
        return buildInternalErrorResponse(ex);
    }

    /**
     * Construye el cuerpo común de una respuesta de error.
     *
     * @param statusCode estado HTTP que debe devolverse
     * @param message descripción del error
     * @param errorCode código identificador del error
     * @return respuesta con las claves {@code error} y {@code message}
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(int statusCode, String message, String errorCode) {
        return ResponseEntity.status(statusCode)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", errorCode, "message", message));
    }

    private ResponseEntity<Map<String, Object>> buildInternalErrorResponse(Exception ex) {
        String requestId = UUID.randomUUID().toString();
        LOGGER.error("Internal API error requestId={}", requestId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("X-Request-ID", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "error", "SERVER_ERROR",
                        "message", "No se pudo completar la solicitud. Inténtalo de nuevo más tarde.",
                        "requestId", requestId));
    }
}
