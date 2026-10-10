package esi.grupo5.esiBuy.Config;

import esi.grupo5.esiBuy.Exception.AuthException;
import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void manejarErrorDeNegocio_conservaStatusCodigoYFormatoDeRespuesta() {
        ResponseEntity<Map<String, Object>> response =
                handler.manejarErrorDeNegocio(new AuthException("Credenciales inválidas"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Map.of(
                "error", "AUTH_ERROR",
                "message", "Credenciales inválidas"), response.getBody());
    }

    @Test
    void manejarErrorDeNegocio_aplicaLaMismaRespuestaATodasLasSubclases() {
        ResponseEntity<Map<String, Object>> response =
                handler.manejarErrorDeNegocio(new ValidationException("Campo obligatorio"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of(
                "error", "VALIDATION_ERROR",
                "message", "Campo obligatorio"), response.getBody());
    }

    @Test
    void manejarErrorDeNegocio_conservaErroresDeNegocioGenericos() {
        ResponseEntity<Map<String, Object>> response =
                handler.manejarErrorDeNegocio(new BusinessException("Error interno", 500, "INTERNAL_ERROR"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "Error interno"), response.getBody());
    }
}
