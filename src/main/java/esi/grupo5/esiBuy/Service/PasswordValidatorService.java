package esi.grupo5.esiBuy.Service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class PasswordValidatorService {
    
    private static final Set<String> BLACKLIST = Set.of("12345678", "password", "qwertyui", "admin123");

    // Patrones de validación
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#&()–\\[{}\\]:;',?/*~$^+=<>-]");

    public void passwordIsWeak(String rawPassword, List<String> historialHashes, BCryptPasswordEncoder encoder) {
        
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 8 caracteres.");
        }

        if (rawPassword.contains(" ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no puede contener espacios en blanco.");
        }

        if (!UPPERCASE_PATTERN.matcher(rawPassword).find()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe contener al menos una letra mayúscula.");
        }

        if (!LOWERCASE_PATTERN.matcher(rawPassword).find()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe contener al menos una letra minúscula.");
        }

        if (!DIGIT_PATTERN.matcher(rawPassword).find()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe contener al menos un número.");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(rawPassword).find()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe contener al menos un carácter especial.");
        }

        if (BLACKLIST.contains(rawPassword.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es demasiado común o débil.");
        }

        if (historialHashes != null) {
            for (String hashAntiguo : historialHashes) {
                if (encoder.matches(rawPassword, hashAntiguo)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes reutilizar tus últimas contraseñas.");
                }
            }
        }
    }
}