package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Exception.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Service
public class PasswordValidatorService {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 72;

    private static final Set<String> BLACKLIST = Set.of(
            "123456", "1234567", "12345678", "123456789", "1234567890",
            "password", "password1", "password123", "qwerty", "qwertyui", "qwerty123", "abc123",
            "111111", "000000", "123123", "admin", "admin123", "adminadmin", "administrator",
            "root", "user", "usuario", "changeme", "letmein", "welcome", "contraseña",
            "contrasena", "clave", "clave123", "hola123", "pass123", "iloveyou", "monkey",
            "dragon", "football", "baseball", "sunshine", "princess"
    );

    private final PasswordEncoder passwordEncoder;

    public PasswordValidatorService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

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

    private void validateNotNull(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("La contraseña es obligatoria");
        }
    }

    private void validateLength(String password) {
        if (password.length() < MIN_LENGTH) {
            throw new ValidationException("La contraseña debe tener al menos " + MIN_LENGTH + " caracteres");
        }
        if (password.length() > MAX_LENGTH) {
            throw new ValidationException("La contraseña no puede superar los " + MAX_LENGTH + " caracteres");
        }
    }

    private void validateNoWhitespace(String password) {
        if (password.chars().anyMatch(Character::isWhitespace)) {
            throw new ValidationException("La contraseña no puede contener espacios en blanco");
        }
    }

    private void validateUppercase(String password) {
        if (password.chars().noneMatch(Character::isUpperCase)) {
            throw new ValidationException("La contraseña debe contener al menos una letra mayúscula");
        }
    }

    private void validateLowercase(String password) {
        if (password.chars().noneMatch(Character::isLowerCase)) {
            throw new ValidationException("La contraseña debe contener al menos una letra minúscula");
        }
    }

    private void validateDigit(String password) {
        if (password.chars().noneMatch(Character::isDigit)) {
            throw new ValidationException("La contraseña debe contener al menos un número");
        }
    }

    private void validateSpecialCharacter(String password) {
        if (password.chars().noneMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))) {
            throw new ValidationException("La contraseña debe contener al menos un carácter especial");
        }
    }

    private void validateBlacklist(String password) {
        if (BLACKLIST.contains(password.toLowerCase())) {
            throw new ValidationException("La contraseña es demasiado común o débil");
        }
    }

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

    public String hashPassword(String rawPassword) {
        try {
            return passwordEncoder.encode(rawPassword);
        } catch (Exception e) {
            throw new BusinessException("Error al encriptar la contraseña", 500, "INTERNAL_ERROR", e);
        }
    }

    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        try {
            return passwordEncoder.matches(rawPassword, hashedPassword);
        } catch (Exception e) {
            throw new BusinessException("Error verificando la contraseña", 500, "INTERNAL_ERROR", e);
        }
    }

    public void validateOldPassword(String oldPassword, String hashedPassword) {
        if (!verifyPassword(oldPassword, hashedPassword)) {
            throw new AuthException("La contraseña actual es incorrecta");
        }
    }

    public void validatePasswordChange(String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new ValidationException("La contraseña actual es requerida");
        }
        validateNotNull(newPassword);
        validatePassword(newPassword, null);
    }

    public void validatePasswordChangeRequest(String currentPassword, String newPassword, String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email es requerido");
        }
        validatePasswordChange(currentPassword, newPassword);
    }

    public void validatePasswordResetRequest(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email es requerido");
        }
    }

    public void validatePasswordResetConfirmRequest(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new ValidationException("Token de recuperación es requerido");
        }
        validateNotNull(newPassword);
        validatePassword(newPassword, null);
    }

    public boolean verifyPasswordResetToken(String token, String hashedToken) {
        try {
            return passwordEncoder.matches(token, hashedToken);
        } catch (Exception e) {
            throw new BusinessException("Error verificando el token de recuperación", 500, "INTERNAL_ERROR", e);
        }
    }

}