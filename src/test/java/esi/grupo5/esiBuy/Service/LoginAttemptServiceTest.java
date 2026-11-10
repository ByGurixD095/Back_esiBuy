package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Exception.RateLimitExceededException;
import esi.grupo5.esiBuy.Model.LoginAttemptState;
import esi.grupo5.esiBuy.Repository.LoginAttemptStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock private LoginAttemptStateRepository repository;
    private final Map<String, LoginAttemptState> states = new HashMap<>();
    private int adaptiveDelayCount;
    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService(repository) {
            @Override
            protected void applyAdaptiveDelay() {
                adaptiveDelayCount++;
            }
        };
        when(repository.findByIpAddress(anyString())).thenAnswer(invocation ->
                Optional.ofNullable(states.get(invocation.getArgument(0))));
        when(repository.save(any(LoginAttemptState.class))).thenAnswer(invocation -> {
            LoginAttemptState state = invocation.getArgument(0);
            states.put(state.getIpAddress(), state);
            return state;
        });
    }

    @Test
    void bloqueaLaCombinacionIpCuentaTrasCincoFallosDuranteLaVentana() {
        for (int i = 0; i < 5; i++) service.registerFailedLogin("10.0.0.1", "user@example.com");

        RateLimitExceededException error = assertThrows(RateLimitExceededException.class,
                () -> service.ensureLoginAllowed("10.0.0.1", "user@example.com"));

        assertEquals(300, error.getRetryAfterSeconds(), 2);
    }

    @Test
    void aplicaBloqueoProgresivoPorCuentaAunqueCambieLaIp() {
        for (int i = 0; i < 5; i++) service.registerFailedLogin("10.0.0." + (i + 1), "user@example.com");

        RateLimitExceededException error = assertThrows(RateLimitExceededException.class,
                () -> service.ensureLoginAllowed("10.0.1.1", "user@example.com"));

        assertEquals(30, error.getRetryAfterSeconds(), 2);
    }

    @Test
    void detectaUnaIpQuePruebaVeinteCuentasDistintas() {
        for (int i = 0; i < 20; i++) {
            String email = "user" + i + "@example.com";
            service.ensureLoginAllowed("10.0.0.1", email);
            service.registerFailedLogin("10.0.0.1", email);
        }

        service.ensureLoginAllowed("10.0.0.1", "another@example.com");
        assertThrows(RateLimitExceededException.class,
                () -> service.ensureLoginAllowed("10.0.0.1", "next@example.com"));
        assertEquals(2, adaptiveDelayCount);
    }
}
