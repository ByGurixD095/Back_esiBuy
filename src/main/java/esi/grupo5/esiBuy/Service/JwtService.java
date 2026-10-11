package esi.grupo5.esiBuy.Service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Genera, valida y extrae información de los JWT de acceso y refresco. */
@Service
public class JwtService {

    /** Secreto configurado para firmar y verificar los JWT. */
    @Value("${jwt.secret}")
    private String secretKey;

    /** Duración configurada del token de acceso, en milisegundos. */
    @Value("${jwt.expiration}")
    private long expirationTime;

    /** Duración configurada del token de refresco, en milisegundos. */
    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationTime;

    /**
     * Genera un JWT de acceso con el identificador y el rol del usuario.
     *
     * @param userDetails usuario representado en el token
     * @return token de acceso firmado
     */
    public String generateToken(Usuario userDetails) {
        return generateToken(userDetails, expirationTime, true);
    }

    /**
     * Genera un JWT de refresco sin incluir el rol.
     *
     * @param userDetails usuario asociado al token
     * @return token de refresco firmado
     */
    public String generateRefreshToken(Usuario userDetails) {
        return generateToken(userDetails, refreshExpirationTime, false);
    }

    /**
     * Construye y firma el token con la duración y los claims solicitados.
     *
     * @param userDetails usuario cuyo identificador se usa como sujeto
     * @param tokenExpirationTime duración en milisegundos
     * @param includeRole indica si se incluye el claim de rol
     * @return JWT compacto firmado
     */
    private String generateToken(Usuario userDetails, long tokenExpirationTime, boolean includeRole) {
        var tokenBuilder = Jwts.builder()
                .subject(userDetails.getId());

        if (includeRole) {
            tokenBuilder.claim("rol", userDetails.getRol().toString());
        }

        return tokenBuilder
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpirationTime))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Deriva la clave HMAC usada para firmar y verificar los tokens.
     *
     * @return clave de firma
     */
    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Verifica y analiza el contenido firmado de un JWT.
     *
     * @param token token compacto que se analizará
     * @return claims contenidos en el token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extrae del sujeto del JWT el identificador de usuario.
     *
     * @param token JWT que se analizará
     * @return identificador de usuario
     */
    public String extractId(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extrae el claim de rol del JWT.
     *
     * @param token JWT que se analizará
     * @return rol del usuario
     */
    public String extractRol(String token) {
        return extractAllClaims(token).get("rol", String.class);
    }

    /**
     * Comprueba que la firma sea válida y que el token no haya expirado.
     *
     * @param token JWT que se validará
     * @return {@code true} si la firma y la expiración son válidas
     */
    public boolean isTokenValid(String token) {
        try {
            return !extractAllClaims(token).getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false; 
        }
    }

    /** @return duración del token de acceso en segundos */
    public int getAccessTokenExpirationSeconds() {
        return toSeconds(expirationTime);
    }

    /** @return duración del token de refresco en segundos */
    public int getRefreshTokenExpirationSeconds() {
        return toSeconds(refreshExpirationTime);
    }

    /**
     * Convierte milisegundos en segundos enteros.
     *
     * @param durationInMilliseconds duración en milisegundos
     * @return duración en segundos, truncada a entero
     */
    private int toSeconds(long durationInMilliseconds) {
        return (int) (durationInMilliseconds / 1000L);
    }
}
