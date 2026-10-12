package esi.grupo5.esiBuy.Util;

import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CookieUtilTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletResponse response;

    @Test
    void setTokenCookies_addsSecureHttpOnlyCookiesWithConfiguredExpirations() {
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900);
        when(jwtService.getRefreshTokenExpirationSeconds()).thenReturn(3600);
        LoginResponseDTO loginResponse = new LoginResponseDTO(
                "access-token", "refresh-token", "CLIENTE", "NORMAL", "SUCCESS", null, null);

        new CookieUtil(jwtService).setTokenCookies(response, loginResponse);

        var cookies = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(response, org.mockito.Mockito.times(2)).addHeader(
                org.mockito.ArgumentMatchers.eq(HttpHeaders.SET_COOKIE), cookies.capture());
        List<String> values = cookies.getAllValues();
        assertEquals(2, values.size());
        assertTrue(values.get(0).contains("accessToken=access-token"));
        assertTrue(values.get(0).contains("Max-Age=900"));
        assertTrue(values.get(1).contains("refreshToken=refresh-token"));
        assertTrue(values.get(1).contains("Max-Age=3600"));
        for (String value : values) {
            assertTrue(value.contains("HttpOnly"));
            assertTrue(value.contains("SameSite=Strict"));
            assertTrue(value.contains("Path=/"));
        }
    }

    @Test
    void setTokenCookiesIfPresent_ignoraRespuestasSinAccessToken() {
        LoginResponseDTO loginResponse = new LoginResponseDTO(
                null, null, "VENDEDOR", null, "REQUIRES_MFA_SETUP", "seller@test.com");

        new CookieUtil(jwtService).setTokenCookiesIfPresent(response, loginResponse);

        org.mockito.Mockito.verifyNoInteractions(response, jwtService);
    }

    @Test
    void clearTokenCookies_expiresBothAuthenticationCookies() {
        new CookieUtil(jwtService).clearTokenCookies(response);

        var cookies = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(response, org.mockito.Mockito.times(2)).addHeader(
                org.mockito.ArgumentMatchers.eq(HttpHeaders.SET_COOKIE), cookies.capture());
        List<String> values = cookies.getAllValues();
        assertTrue(values.get(0).contains("accessToken="));
        assertTrue(values.get(1).contains("refreshToken="));
        for (String value : values) {
            assertTrue(value.contains("Max-Age=0"));
            assertTrue(value.contains("HttpOnly"));
            assertTrue(value.contains("SameSite=Strict"));
        }
    }
}
