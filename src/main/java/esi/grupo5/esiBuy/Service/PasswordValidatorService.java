package esi.grupo5.esiBuy.Service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Set;

@Service
public class PasswordValidatorService {
    
    // Simulación de Blacklist (en producción, cargar desde un archivo txt como top-10000-passwords.txt)
    private static final Set<String> BLACKLIST = Set.of("12345678", "password", "qwertyui", "admin123");

    public void validarNuevaContrasena(String rawPassword, List<String> historialHashes, BCryptPasswordEncoder encoder) {
        // Se puede agregar más validaciones según las políticas de seguridad, como verificar la complejidad de la contraseña, etc.(PREGUNTAR)
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 8 caracteres.");
        }

        if (BLACKLIST.contains(rawPassword.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es demasiado común o débil.");
        }

        if (historialHashes != null) {
            for (String hashAntiguo : historialHashes) {
                if (encoder.matches(rawPassword, hashAntiguo)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes reutilizar tus últimas 5 contraseñas.");
                }
            }
        }
    }
}