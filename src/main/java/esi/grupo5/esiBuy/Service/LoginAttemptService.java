package esi.grupo5.esiBuy.Service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import esi.grupo5.esiBuy.Model.LoginAttemptState;
import esi.grupo5.esiBuy.Repository.LoginAttemptStateRepository;

@Service
public class LoginAttemptService {

    private static final int ATTEMPTS_PER_BAN = 3;
    private static final long INITIAL_BAN_SECONDS = 15L;
    private static final long MAX_BAN_SECONDS = 15L * 60L;

    private LoginAttemptStateRepository loginAttemptRepository;

    public LoginAttemptService(LoginAttemptStateRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

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

    public void registerSuccessfulLogin(String ipAddress) {
        this.loginAttemptRepository.findByIpAddress(ipAddress).ifPresent(this.loginAttemptRepository::delete);
    }

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

    private long calculateBanSeconds(int banLevel) {
        long banSeconds = INITIAL_BAN_SECONDS * (1L << Math.max(0, banLevel - 1));
        return Math.min(banSeconds, MAX_BAN_SECONDS);
    }
}