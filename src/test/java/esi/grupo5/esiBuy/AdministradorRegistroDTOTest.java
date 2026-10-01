package esi.grupo5.esiBuy;

import esi.grupo5.esiBuy.Dto.AdministradorRegistroDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdministradorRegistroDTOTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private AdministradorRegistroDTO dto(String nombre, String email, String contrasena) {
        return new AdministradorRegistroDTO(nombre, "Pérez", email, contrasena, "Madrid");
    }

    @Test
    void datosValidos_noHayErrores() {
        assertTrue(validator.validate(dto("Ana", "ana@esibuy.com", "Clave#2026x")).isEmpty());
    }

    @Test
    void nombreVacio_esInvalido() {
        assertFalse(validator.validate(dto("", "ana@esibuy.com", "Clave#2026x")).isEmpty());
    }

    @Test
    void emailMalFormado_esInvalido() {
        assertFalse(validator.validate(dto("Ana", "esto-no-es-un-email", "Clave#2026x")).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ab1#", "sinmayusculas1#", "SINMINUSCULAS1#", "SinNumeros##", "SinEspeciales12"})
    void contrasenaQueNoCumplePolitica_esInvalida(String contrasena) {
        assertFalse(validator.validate(dto("Ana", "ana@esibuy.com", contrasena)).isEmpty());
    }
}
