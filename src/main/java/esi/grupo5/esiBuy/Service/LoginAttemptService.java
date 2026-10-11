package esi.grupo5.esiBuy.Service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import esi.grupo5.esiBuy.Model.LoginAttemptState;
import esi.grupo5.esiBuy.Repository.LoginAttemptStateRepository;

/** Registra fallos de inicio de sesión y aplica bloqueos temporales por IP. */
@Service
public class LoginAttemptService {

    /** Número de fallos acumulados antes de incrementar el bloqueo. */
    private static final int ATTEMPTS_PER_BAN = 3;
    /** Duración del primer bloqueo, en segundos. */
    private static final long INITIAL_BAN_SECONDS = 15L;
    /** Duración máxima de un bloqueo, en segundos. */
    private static final long MAX_BAN_SECONDS = 15L * 60L;

    /** Repositorio de estados de intentos de acceso. */
    private LoginAttemptStateRepository loginAttemptRepository;

    /**
     * Crea el servicio de control de intentos.
     *
     * @param loginAttemptRepository repositorio de estados por dirección IP
     */
    public LoginAttemptService(LoginAttemptStateRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    /**
     * Comprueba si una dirección IP puede iniciar sesión.
     *
     * @param ipAddress dirección IP que se validará
     * @throws ResponseStatusException con estado 429 si sigue vigente un bloqueo
     */
    public void ensureLoginAllowed(String ipAddress) {
        LoginAttemptState state = this.loginAttemptRepository.findByIpAddress(ipAddress).orElse(null);
        if (state == null || state.getBannedUntil() == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (state.getBannedUntil().isAfter(now)) {
            long remainingSeconds = Duration.between(now, state.getBannedUntil()).toSeconds();
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos desde esta IP. Prueba de nuevo en " + Math.max(1L, remainingSeconds) + " segundos.");
        }
    }

    /**
     * Borra el estado de intentos fallidos tras un inicio de sesión correcto.
     *
     * @param ipAddress dirección IP cuyo estado se reinicia
     */
    public void registerSuccessfulLogin(String ipAddress) {
        this.loginAttemptRepository.findByIpAddress(ipAddress).ifPresent(this.loginAttemptRepository::delete);
    }

    /**
     * Registra un fallo y aplica un bloqueo progresivo cada tres fallos.
     *
     * @param ipAddress dirección IP asociada al intento
     */
    public void registerFailedLogin(String ipAddress) {
        LocalDateTime now = LocalDateTime.now();
        LoginAttemptState state = this.loginAttemptRepository.findByIpAddress(ipAddress)
                .orElseGet(() -> new LoginAttemptState(ipAddress));

        if (state.getBannedUntil() != null && !state.getBannedUntil().isAfter(now)) {
            state.setBannedUntil(null);
        }

        state.setFailedAttempts(state.getFailedAttempts() + 1);
        state.setLastFailureAt(now);

        if (state.getFailedAttempts() >= ATTEMPTS_PER_BAN && state.getFailedAttempts() % ATTEMPTS_PER_BAN == 0) {
            state.setBanLevel(state.getBanLevel() + 1);
            state.setFailedAttempts(0);
            state.setBannedUntil(now.plusSeconds(calculateBanSeconds(state.getBanLevel())));
        }

        state.setUpdatedAt(now);
        this.loginAttemptRepository.save(state);
    }

    /**
     * Calcula la duración exponencial del bloqueo y la limita al máximo configurado.
     *
     * @param banLevel nivel actual del bloqueo, empezando en uno
     * @return duración del bloqueo en segundos
     */
    private long calculateBanSeconds(int banLevel) {
        long banSeconds = INITIAL_BAN_SECONDS * (1L << Math.max(0, banLevel - 1));
        return Math.min(banSeconds, MAX_BAN_SECONDS);
    }
}