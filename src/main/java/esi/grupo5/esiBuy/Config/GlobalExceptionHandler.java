package esi.grupo5.esiBuy.Config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import esi.grupo5.esiBuy.Exception.BusinessException;

/** Traduce las excepciones de negocio a respuestas HTTP uniformes. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Convierte una excepción de negocio en una respuesta JSON con estado, código y mensaje.
     *
     * @param ex excepción que contiene el estado HTTP, el código y el mensaje
     * @return respuesta con estado y cuerpo derivados de la excepción
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> manejarErrorDeNegocio(BusinessException ex) {
        return buildErrorResponse(ex.getHttpStatusCode(), ex.getMessage(), ex.getErrorCode());
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
        Map<String, Object> response = new HashMap<>();
        response.put("error", errorCode);
        response.put("message", message);
        
        return ResponseEntity.status(statusCode).body(response);
    }
}
