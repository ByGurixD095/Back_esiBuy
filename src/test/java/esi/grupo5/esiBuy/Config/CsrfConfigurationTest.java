package esi.grupo5.esiBuy.Config;

import esi.grupo5.esiBuy.Controller.CsrfController;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CsrfController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "app.security.csrf-enabled=true",
        "app.cookie.secure=true",
        "app.cookie.same-site=None"
})
class CsrfConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void csrfEndpointIssuesTokenCookieAndTokenValue() throws Exception {
        mockMvc.perform(get("/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    void unsafeRequestWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/users/login"))
                .andExpect(status().isForbidden());
    }
}
