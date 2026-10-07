package esi.grupo5.esiBuy.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class PasswordValidatorService {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 72;

    private static final Set<String> BLACKLIST = Set.of(
        // Contraseñas extremadamente comunes
        "123456",
        "1234567",
        "12345678",
        "123456789",
        "1234567890",
        "password",
        "password1",
        "password123",
        "qwerty",
        "qwertyui",
        "qwerty123",
        "abc123",
        "111111",
        "000000",
        "123123",

        // Credenciales típicas
        "admin",
        "admin123",
        "adminadmin",
        "administrator",
        "root",
        "user",
        "usuario",
        "changeme",
        "letmein",
        "welcome",

        // Contraseñas habituales en español
        "contraseña",
        "contrasena",
        "clave",
        "clave123",
        "hola123",
        "pass123",

        // Patrones habituales
        "iloveyou",
        "monkey",
        "dragon",
        "football",
        "baseball",
        "sunshine",
        "princess"
);

    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordValidatorService(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void validatePassword(
            String rawPassword,
            Collection<String> historialHashes
    ) {

        validateNotNull(rawPassword);
        validateLength(rawPassword);
        validateNoWhitespace(rawPassword);
        validateUppercase(rawPassword);
        validateLowercase(rawPassword);
        validateDigit(rawPassword);
        validateSpecialCharacter(rawPassword);
        validateBlacklist(rawPassword);
        validatePasswordHistory(rawPassword, historialHashes);
    }

    private void validateNotNull(String password) {
        if (password == null) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "La contraseña es obligatoria."
            );
        }
    }

    private void validateLength(String password) {
        if (password.length() < MIN_LENGTH) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "La contraseña debe tener al menos " + MIN_LENGTH + " caracteres."
            );
        }

        if (password.length() > MAX_LENGTH) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "La contraseña no puede superar los " + MAX_LENGTH + " caracteres."
            );
        }
    }

    private void validateNoWhitespace(String password) {
        if (password.chars().anyMatch(Character::isWhitespace)) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "La contraseña no puede contener espacios en blanco."
            );
        }
    }

    private void validateUppercase(String password) {
        if (password.chars().noneMatch(Character::isUpperCase)) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "La contraseña debe contener al menos una letra mayúscula."
            );
        }
    }

    private void validateLowercase(String password) {
        if (password.chars().noneMatch(Character::isLowerCase)) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "La contraseña debe contener al menos una letra minúscula."
            );
        }
    }

    private void validateDigit(String password) {
        if (password.chars().noneMatch(Character::isDigit)) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "La contraseña debe contener al menos un número."
            );
        }
    }

    private void validateSpecialCharacter(String password) {
        if (password.chars().noneMatch(c ->
                !Character.isLetterOrDigit(c) && !Character.isWhitespace(c)
        )) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "La contraseña debe contener al menos un carácter especial."
            );
        }
    }

    private void validateBlacklist(String password) {
        if (BLACKLIST.contains(password.toLowerCase())) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "La contraseña es demasiado común o débil."
            );
        }
    }

    private void validatePasswordHistory(
            String password,
            Collection<String> historialHashes
    ) {
        if (historialHashes == null) {
            return;
        }

        boolean reused = historialHashes.stream()
                .anyMatch(hash -> passwordEncoder.matches(password, hash));

        if (reused) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "No puedes reutilizar tus últimas contraseñas."
            );
        }
    }
}