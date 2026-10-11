package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Exception.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.web.server.ResponseStatusException;

/** Valida contraseñas, compara hashes y aplica las reglas de historial. */
@Service
public class PasswordValidatorService {

    /** Longitud mínima de contraseña. */
    private static final int MIN_LENGTH = 8;
    /** Longitud máxima aceptada por el codificador utilizado. */
    private static final int MAX_LENGTH = 72;

    /** Contraseñas comunes que se rechazan por considerarse débiles. */
    private static final Set<String> BLACKLIST = Set.of(
            "123456", "1234567", "12345678", "123456789", "1234567890",
            "password", "password1", "password123", "qwerty", "qwertyui", "qwerty123", "abc123",
            "111111", "000000", "123123", "admin", "admin123", "adminadmin", "administrator",
            "root", "user", "usuario", "changeme", "letmein", "welcome", "contraseña",
            "contrasena", "clave", "clave123", "hola123", "pass123", "iloveyou", "monkey",
            "dragon", "football", "baseball", "sunshine", "princess"
    );

    /** Codificador utilizado para guardar contraseñas y comparar hashes. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea el validador con el codificador de contraseñas.
     *
     * @param passwordEncoder codificador de contraseñas
     */
    public PasswordValidatorService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Aplica los requisitos de seguridad y comprueba el historial opcional.
     *
     * @param password contraseña en texto plano
     * @param historialHashes hashes anteriores que no pueden reutilizarse; puede ser nulo
     * @throws ValidationException si falla un requisito o se reutiliza una contraseña
     */
    public void validatePassword(String password, Collection<String> historialHashes) {
        validateNotNull(password);
        validateLength(password);
        validateNoWhitespace(password);
        validateUppercase(password);
        validateLowercase(password);
        validateDigit(password);
        validateSpecialCharacter(password);
        validateBlacklist(password);
        validatePasswordHistory(password, historialHashes);
    }

    /**
     * Valida la contraseña de registro y presenta sus fallos como errores de validación.
     *
     * @param password contraseña que se validará
     * @throws ValidationException si no cumple los requisitos
     */
    public void validateRegistrationPassword(String password) {
        try {
            validatePassword(password, List.of());
        } catch (ResponseStatusException e) {
            throw new ValidationException(
                    "La contraseña no cumple con los requisitos de seguridad: " + e.getReason());
        }
    }

    /** Rechaza contraseñas nulas o vacías. */
    private void validateNotNull(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("La contraseña es obligatoria");
        }
    }

    /** Comprueba los límites de longitud permitidos. */
    private void validateLength(String password) {
        if (password.length() < MIN_LENGTH) {
            throw new ValidationException("La contraseña debe tener al menos " + MIN_LENGTH + " caracteres");
        }
        if (password.length() > MAX_LENGTH) {
            throw new ValidationException("La contraseña no puede superar los " + MAX_LENGTH + " caracteres");
        }
    }

    /** Rechaza caracteres de espacio en blanco. */
    private void validateNoWhitespace(String password) {
        if (password.chars().anyMatch(Character::isWhitespace)) {
            throw new ValidationException("La contraseña no puede contener espacios en blanco");
        }
    }

    /** Exige al menos una letra mayúscula. */
    private void validateUppercase(String password) {
        if (password.chars().noneMatch(Character::isUpperCase)) {
            throw new ValidationException("La contraseña debe contener al menos una letra mayúscula");
        }
    }

    /** Exige al menos una letra minúscula. */
    private void validateLowercase(String password) {
        if (password.chars().noneMatch(Character::isLowerCase)) {
            throw new ValidationException("La contraseña debe contener al menos una letra minúscula");
        }
    }

    /** Exige al menos un dígito. */
    private void validateDigit(String password) {
        if (password.chars().noneMatch(Character::isDigit)) {
            throw new ValidationException("La contraseña debe contener al menos un número");
        }
    }

    /** Exige al menos un carácter que no sea letra, dígito ni espacio. */
    private void validateSpecialCharacter(String password) {
        if (password.chars().noneMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))) {
            throw new ValidationException("La contraseña debe contener al menos un carácter especial");
        }
    }

    /** Rechaza contraseñas incluidas en la lista de claves comunes. */
    private void validateBlacklist(String password) {
        if (BLACKLIST.contains(password.toLowerCase())) {
            throw new ValidationException("La contraseña es demasiado común o débil");
        }
    }

    /** Rechaza contraseñas que coincidan con alguno de los hashes anteriores. */
    private void validatePasswordHistory(String password, Collection<String> historialHashes) {
        if (historialHashes == null) {
            return;
        }
        boolean reused = historialHashes.stream()
                .anyMatch(hash -> passwordEncoder.matches(password, hash));

        if (reused) {
            throw new ValidationException("No puedes reutilizar tus últimas contraseñas");
        }
    }

    /**
     * Genera el hash de una contraseña.
     *
     * @param rawPassword contraseña en texto plano
     * @return hash generado
     * @throws BusinessException si falla el codificador
     */
    public String hashPassword(String rawPassword) {
        try {
            return passwordEncoder.encode(rawPassword);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BusinessException("Error al encriptar la contraseña", 500, "INTERNAL_ERROR", e);
        }
    }

    /**
     * Compara una contraseña en claro con un hash almacenado.
     *
     * @param rawPassword contraseña en texto plano
     * @param hashedPassword hash almacenado
     * @return {@code true} si coinciden
     * @throws BusinessException si falla el codificador
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        try {
            return passwordEncoder.matches(rawPassword, hashedPassword);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BusinessException("Error verificando la contraseña", 500, "INTERNAL_ERROR", e);
        }
    }

    /**
     * Comprueba que la contraseña actual coincida con su hash almacenado.
     *
     * @param oldPassword contraseña actual en texto plano
     * @param hashedPassword hash de referencia
     * @throws AuthException si no coinciden
     */
    public void validateOldPassword(String oldPassword, String hashedPassword) {
        if (!verifyPassword(oldPassword, hashedPassword)) {
            throw new AuthException("La contraseña actual es incorrecta");
        }
    }

    /**
     * Valida la presencia de la contraseña actual y los requisitos de la nueva.
     *
     * @param currentPassword contraseña actual
     * @param newPassword nueva contraseña
     */
    public void validatePasswordChange(String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new ValidationException("La contraseña actual es requerida");
        }
        validateNotNull(newPassword);
        validatePassword(newPassword, null);
    }

    /**
     * Comprueba el correo y valida las contraseñas para una solicitud de cambio.
     *
     * @param currentPassword contraseña actual
     * @param newPassword nueva contraseña
     * @param email correo asociado a la cuenta
     */
    public void validatePasswordChangeRequest(String currentPassword, String newPassword, String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email es requerido");
        }
        validatePasswordChange(currentPassword, newPassword);
    }

    /**
     * Comprueba que se haya proporcionado el correo de recuperación.
     *
     * @param email correo de la cuenta
     */
    public void validatePasswordResetRequest(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email es requerido");
        }
    }

    /**
     * Valida la presencia del token de recuperación y los requisitos de la nueva contraseña.
     *
     * @param token token recibido
     * @param newPassword contraseña que se establecerá
     */
    public void validatePasswordResetConfirmRequest(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new ValidationException("Token de recuperación es requerido");
        }
        validateNotNull(newPassword);
        validatePassword(newPassword, null);
    }

    /**
     * Compara un token de recuperación en claro con su hash almacenado.
     *
     * @param token token recibido
     * @param hashedToken hash de referencia
     * @return {@code true} si coinciden
     * @throws BusinessException si falla la comparación del hash
     */
    public boolean verifyPasswordResetToken(String token, String hashedToken) {
        try {
            return passwordEncoder.matches(token, hashedToken);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BusinessException("Error verificando el token de recuperación", 500, "INTERNAL_ERROR", e);
        }
    }

}