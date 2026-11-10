package esi.grupo5.esiBuy.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Exception.RateLimitExceededException;
import esi.grupo5.esiBuy.Model.LoginAttemptState;
import esi.grupo5.esiBuy.Repository.LoginAttemptStateRepository;

/** Registra fallos de acceso con límites por IP y cuenta, sin almacenar identificadores en claro. */
@Service
public class LoginAttemptService {

    private static final int IP_USER_LIMIT = 5;
    private static final int ACCOUNT_FAILURE_LIMIT = 5;
    private static final int ADAPTIVE_UNIQUE_USER_THRESHOLD = 20;
    private static final int ADAPTIVE_IP_LIMIT = 2;
    private static final long WINDOW_MINUTES = 5;
    private static final long ACCOUNT_WINDOW_MINUTES = 10;
    private static final long ADAPTIVE_DELAY_MILLIS = 2_000;

    private final LoginAttemptStateRepository loginAttemptRepository;

    public LoginAttemptService(LoginAttemptStateRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    public void ensureLoginAllowed(String ipAddress, String username) {
        LocalDateTime now = LocalDateTime.now();
        String normalizedUsername = normalizeUsername(username);
        LoginAttemptState pair = find(key("pair", ipAddress, normalizedUsername));
        rejectIfBanned(pair, now);
        LoginAttemptState account = find(key("account", normalizedUsername));
        rejectIfBanned(account, now);

        LoginAttemptState ipState = find(key("ip", ipAddress));
        if (ipState == null) {
            ipState = new LoginAttemptState(key("ip", ipAddress));
            ipState.setWindowStartedAt(now);
        } else if (!isWithinWindow(ipState, now, WINDOW_MINUTES)) {
            ipState.setWindowStartedAt(now);
            ipState.setFailedAttempts(0);
            ipState.getDistinctUsers().clear();
        }
        String usernameHash = hash(normalizedUsername);
        if (!ipState.getDistinctUsers().contains(usernameHash)) {
            ipState.getDistinctUsers().add(usernameHash);
        }
        ipState.setUpdatedAt(now);
        if (ipState.getDistinctUsers().size() >= ADAPTIVE_UNIQUE_USER_THRESHOLD) {
            boolean adaptiveWindowExpired = ipState.getAdaptiveWindowStartedAt() == null
                    || !ipState.getAdaptiveWindowStartedAt().plusMinutes(WINDOW_MINUTES).isAfter(now);
            if (adaptiveWindowExpired) {
                ipState.setAdaptiveWindowStartedAt(now);
                ipState.setAdaptiveAttempts(0);
            }
            if (ipState.getAdaptiveAttempts() >= ADAPTIVE_IP_LIMIT) {
                long retryAfter = secondsUntil(ipState.getAdaptiveWindowStartedAt().plusMinutes(WINDOW_MINUTES), now);
                loginAttemptRepository.save(ipState);
                throw new RateLimitExceededException(retryAfter);
            }
            ipState.setAdaptiveAttempts(ipState.getAdaptiveAttempts() + 1);
            loginAttemptRepository.save(ipState);
            applyAdaptiveDelay();
        } else {
            loginAttemptRepository.save(ipState);
        }
    }

    /** Compatibility overload for callers that only have an IP. */
    public void ensureLoginAllowed(String ipAddress) {
        ensureLoginAllowed(ipAddress, "unknown");
    }

    public void registerSuccessfulLogin(String ipAddress, String username) {
        String normalizedUsername = normalizeUsername(username);
        loginAttemptRepository.findByIpAddress(key("pair", ipAddress, normalizedUsername))
                .ifPresent(loginAttemptRepository::delete);
        loginAttemptRepository.findByIpAddress(key("account", normalizedUsername))
                .ifPresent(loginAttemptRepository::delete);
    }

    /** Compatibility overload for existing integrations. */
    public void registerSuccessfulLogin(String ipAddress) {
        registerSuccessfulLogin(ipAddress, "unknown");
    }

    public void registerFailedLogin(String ipAddress, String username) {
        LocalDateTime now = LocalDateTime.now();
        String normalizedUsername = normalizeUsername(username);
        LoginAttemptState pair = getOrCreate(key("pair", ipAddress, normalizedUsername));
        incrementWindowed(pair, now, WINDOW_MINUTES);
        if (pair.getFailedAttempts() >= IP_USER_LIMIT) {
            pair.setBannedUntil(pair.getWindowStartedAt().plusMinutes(WINDOW_MINUTES));
        }
        loginAttemptRepository.save(pair);

        LoginAttemptState account = getOrCreate(key("account", normalizedUsername));
        if (!isWithinWindow(account, now, ACCOUNT_WINDOW_MINUTES)) {
            account.setWindowStartedAt(now);
            account.setFailedAttempts(0);
        }
        account.setFailedAttempts(account.getFailedAttempts() + 1);
        account.setLastFailureAt(now);
        account.setUpdatedAt(now);
        if (account.getFailedAttempts() >= ACCOUNT_FAILURE_LIMIT) {
            account.setBanLevel(account.getBanLevel() + 1);
            long[] backoffSeconds = {30, 120, 240, 300};
            int levelIndex = Math.min(account.getBanLevel() - 1, backoffSeconds.length - 1);
            account.setBannedUntil(now.plusSeconds(backoffSeconds[levelIndex]));
            account.setFailedAttempts(0);
            account.setWindowStartedAt(now);
        }
        loginAttemptRepository.save(account);

        LoginAttemptState ip = getOrCreate(key("ip", ipAddress));
        if (!isWithinWindow(ip, now, WINDOW_MINUTES)) {
            ip.setWindowStartedAt(now);
            ip.setFailedAttempts(0);
            ip.getDistinctUsers().clear();
            ip.setAdaptiveWindowStartedAt(null);
            ip.setAdaptiveAttempts(0);
        }
        ip.setFailedAttempts(ip.getFailedAttempts() + 1);
        ip.setLastFailureAt(now);
        ip.setUpdatedAt(now);
        loginAttemptRepository.save(ip);
    }

    /** Compatibility overload for existing integrations. */
    public void registerFailedLogin(String ipAddress) {
        registerFailedLogin(ipAddress, "unknown");
    }

    private void rejectIfBanned(LoginAttemptState state, LocalDateTime now) {
        if (state != null && state.getBannedUntil() != null && state.getBannedUntil().isAfter(now)) {
            throw new RateLimitExceededException(secondsUntil(state.getBannedUntil(), now));
        }
    }

    private long secondsUntil(LocalDateTime until, LocalDateTime now) {
        return Math.max(1, Duration.between(now, until).toSeconds());
    }

    private LoginAttemptState find(String key) {
        return loginAttemptRepository.findByIpAddress(key).orElse(null);
    }

    private LoginAttemptState getOrCreate(String key) {
        return loginAttemptRepository.findByIpAddress(key).orElseGet(() -> new LoginAttemptState(key));
    }

    private void incrementWindowed(LoginAttemptState state, LocalDateTime now, long windowMinutes) {
        if (!isWithinWindow(state, now, windowMinutes)) {
            state.setWindowStartedAt(now);
            state.setFailedAttempts(0);
        }
        state.setFailedAttempts(state.getFailedAttempts() + 1);
        state.setLastFailureAt(now);
        state.setUpdatedAt(now);
    }

    private boolean isWithinWindow(LoginAttemptState state, LocalDateTime now, long windowMinutes) {
        return state.getWindowStartedAt() != null
                && state.getWindowStartedAt().plusMinutes(windowMinutes).isAfter(now);
    }

    protected void applyAdaptiveDelay() {
        try {
            Thread.sleep(ADAPTIVE_DELAY_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Login delay interrupted", exception);
        }
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String key(String type, String... parts) {
        return type + ":" + hash(String.join("\u0000", parts));
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
