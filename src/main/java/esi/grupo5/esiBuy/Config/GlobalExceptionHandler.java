package esi.grupo5.esiBuy.Config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import esi.grupo5.esiBuy.Exception.BusinessException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> manejarErrorDeNegocio(BusinessException ex) {
        return buildErrorResponse(ex.getHttpStatusCode(), ex.getMessage(), ex.getErrorCode());
    }

    // Método auxiliar para construir la respuesta de error
    private ResponseEntity<Map<String, Object>> buildErrorResponse(int statusCode, String message, String errorCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", errorCode);
        response.put("message", message);
        
        return ResponseEntity.status(statusCode).body(response);
    }
}
