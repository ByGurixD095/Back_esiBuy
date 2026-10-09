package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Model.LoginAttemptState;
import esi.grupo5.esiBuy.Repository.LoginAttemptStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private LoginAttemptStateRepository repository;

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService(repository);
    }

    @Test
    void permiteLoginCuandoLaIpNoEstaBloqueada() {
        when(repository.findByIpAddress("10.0.0.1")).thenReturn(Optional.empty());

        service.ensureLoginAllowed("10.0.0.1");
    }

    @Test
    void bloqueaLaIpCuandoTieneUnBaneoActivo() {
        LoginAttemptState state = new LoginAttemptState("10.0.0.1");
        state.setBannedUntil(LocalDateTime.now().plusMinutes(1));
        when(repository.findByIpAddress("10.0.0.1")).thenReturn(Optional.of(state));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.ensureLoginAllowed("10.0.0.1"));

        assertEquals(429, exception.getStatusCode().value());
    }

    @Test
    void creaBaneoTrasTresIntentosFallidos() {
        LoginAttemptState state = new LoginAttemptState("10.0.0.1");
        state.setFailedAttempts(1);
        when(repository.findByIpAddress("10.0.0.1")).thenReturn(Optional.of(state));

        service.registerFailedLogin("10.0.0.1");
        service.registerFailedLogin("10.0.0.1");

        ArgumentCaptor<LoginAttemptState> captor = ArgumentCaptor.forClass(LoginAttemptState.class);
        verify(repository, org.mockito.Mockito.times(2)).save(captor.capture());
        LoginAttemptState thirdAttempt = captor.getAllValues().get(1);
        assertEquals(1, thirdAttempt.getBanLevel());
        assertEquals(0, thirdAttempt.getFailedAttempts());
        assertEquals(15, java.time.Duration.between(
                LocalDateTime.now(), thirdAttempt.getBannedUntil()).getSeconds(), 2);
    }

    @Test
    void eliminaElEstadoTrasLoginCorrecto() {
        LoginAttemptState state = new LoginAttemptState("10.0.0.1");
        when(repository.findByIpAddress("10.0.0.1")).thenReturn(Optional.of(state));

        service.registerSuccessfulLogin("10.0.0.1");

        verify(repository).delete(state);
    }
}
