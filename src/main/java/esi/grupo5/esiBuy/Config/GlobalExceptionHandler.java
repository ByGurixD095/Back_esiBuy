package esi.grupo5.esiBuy.Config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import esi.grupo5.esiBuy.Exception.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Maneja excepciones de autenticación (login fallido, credenciales inválidas)
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> manejarAutenticacionFallida(AuthException ex) {
        return buildErrorResponse(ex.getHttpStatusCode(), ex.getMessage(), ex.getErrorCode());
    }

    // Maneja excepciones de autorización (no tienes permisos, cuenta bloqueada)
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> manejarAutorizacionFallida(ForbiddenException ex) {
        return buildErrorResponse(ex.getHttpStatusCode(), ex.getMessage(), ex.getErrorCode());
    }

    // Maneja excepciones de token expirado
    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<Map<String, Object>> manejarTokenExpirado(ExpiredTokenException ex) {
        return buildErrorResponse(ex.getHttpStatusCode(), ex.getMessage(), ex.getErrorCode());
    }

    // Maneja excepciones de cuenta bloqueada
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Map<String, Object>> manejarCuentaBloqueada(AccountLockedException ex) {
        return buildErrorResponse(ex.getHttpStatusCode(), ex.getMessage(), ex.getErrorCode());
    }

    // Maneja excepciones de contraseña expirada
    @ExceptionHandler(PasswordExpiredException.class)
    public ResponseEntity<Map<String, Object>> manejarContrasenaExpirada(PasswordExpiredException ex) {
        return buildErrorResponse(ex.getHttpStatusCode(), ex.getMessage(), ex.getErrorCode());
    }

    // Maneja excepciones de recurso no encontrado
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> manejarRecursoNoEncontrado(NotFoundException ex) {
        return buildErrorResponse(ex.getHttpStatusCode(), ex.getMessage(), ex.getErrorCode());
    }

    // Maneja excepciones de validación (campos vacíos, formato incorrecto)
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacionFallida(ValidationException ex) {
        return buildErrorResponse(ex.getHttpStatusCode(), ex.getMessage(), ex.getErrorCode());
    }

    // Maneja excepciones de conflicto (recurso ya existe, email duplicado)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> manejarConflicto(ConflictException ex) {
        return buildErrorResponse(ex.getHttpStatusCode(), ex.getMessage(), ex.getErrorCode());
    }

    // Maneja excepciones de negocio genéricas
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
