package esi.grupo5.esiBuy.Service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationTime;

    // generar token JWT
    public String generateToken(Usuario userDetails) {
        return Jwts.builder()
                .subject(userDetails.getId())   
                .claim("rol", userDetails.getRol().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    // Generar token de refresco JWT
    public String generateRefreshToken(Usuario userDetails) {
        return Jwts.builder()
                .subject(userDetails.getId())    
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationTime))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    // Obetener clave de firma a partir de la clave secreta
    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 2. Extraer todos los claims del token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey()) // Verifica matemáticamente que la firma sea tuya
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 3. Extraer específicamente el ID (subject) del token
    public String extractId(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 3. Extraer específicamente el rol del token
    public String extractRol(String token) {
        return extractAllClaims(token).get("rol", String.class);
    }

    // 4. Validar que el token es correcto y que su fecha no ha expirado
    public boolean isTokenValid(String token) {
        try {
            // Si pasamos la validación de firma y la fecha de expiración es posterior a hoy, es válido
            return !extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            // Si el token fue manipulado, caducó, o tiene un formato incorrecto, JJWT lanzará una excepción.
            // La capturamos y devolvemos false para denegar el acceso.
            return false; 
        }
    }

    public int getAccessTokenExpirationSeconds() {
        return (int) (expirationTime / 1000L);
    }

    public int getRefreshTokenExpirationSeconds() {
        return (int) (refreshExpirationTime / 1000L);
    }
}
