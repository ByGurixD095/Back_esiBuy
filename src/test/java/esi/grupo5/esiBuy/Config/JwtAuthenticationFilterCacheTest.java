package esi.grupo5.esiBuy.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.JwtService;
import jakarta.servlet.http.Cookie;

class JwtAuthenticationFilterCacheTest {

    private JwtService jwtService;
    private UsuarioRepository usuarioRepository;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        usuarioRepository = mock(UsuarioRepository.class);
        filter = new JwtAuthenticationFilter(jwtService, usuarioRepository);
    }

    @Test
    void doFilter_usaCacheEnPeticionesConsecutivasSinConsultarMongoRepetidamente() throws Exception {
        String token = "valid-token";
        String userId = "usuario-123";

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractId(token)).thenReturn(userId);
        when(jwtService.extractRol(token)).thenReturn("CLIENTE");
        when(usuarioRepository.existsByIdAndActivoTrueAndBloqueadoFalse(userId)).thenReturn(true);

        MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.setCookies(new Cookie("accessToken", token));
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        MockFilterChain filterChain1 = new MockFilterChain();

        filter.doFilter(request1, response1, filterChain1);

        // Segunda petición consecutiva con el mismo usuario
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setCookies(new Cookie("accessToken", token));
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        MockFilterChain filterChain2 = new MockFilterChain();

        filter.doFilter(request2, response2, filterChain2);

        // Se verifica que solo se consultó a la base de datos 1 sola vez gracias a la caché
        verify(usuarioRepository, times(1)).existsByIdAndActivoTrueAndBloqueadoFalse(userId);
    }
}
