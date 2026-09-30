package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.RefreshToken;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserService(UsuarioRepository usuarioRepository, JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    private Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {

        Optional<Usuario> optionalUsuario = findByUsername(loginRequest.username());
        if (optionalUsuario.isEmpty() || !encoder.matches(loginRequest.password(), optionalUsuario.get().getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        Usuario usuario = optionalUsuario.get();
        String token = jwtService.generateToken(usuario);
        String refreshTokenString = jwtService.generateRefreshToken(usuario);

        // Guardamos el token de refresco en la base de datos
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenString);
        refreshToken.setUsuario(usuario);
        long expirationTime = jwtService.getRefreshTokenExpirationSeconds() * 1000L;
        refreshToken.setFechaExpiracion(LocalDateTime.now().plusSeconds(expirationTime));
        refreshTokenRepository.save(refreshToken);

        // Retornamos el DTO correspondiente según si es Cliente u otro rol
        if (usuario instanceof Cliente cliente) {
            return new LoginResponseDTO(token, refreshTokenString, usuario.getRol().toString(), cliente.getTipoCliente().toString());
        }
        return new LoginResponseDTO(token, refreshTokenString, usuario.getRol().toString(), null);
    }
}