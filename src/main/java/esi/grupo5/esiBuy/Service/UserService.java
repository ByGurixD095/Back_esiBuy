package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.UserDto;
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
import java.util.ArrayList;
import java.util.List;
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

    private Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {

        Optional<Usuario> optionalUsuario = findByEmail(loginRequest.username());
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

    public LoginResponseDTO refreshToken(String refreshTokenString) {
        // 1. Comprobamos que el token de refresco exista en la base de datos
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByToken(refreshTokenString);
        if (optionalRefreshToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de refresco inválido");
        }
        RefreshToken refreshTokenEntity = optionalRefreshToken.get();

        // 2. Comprobamos que la firma matemática siga siendo válida (que no haya caducado)
        if (!jwtService.isTokenValid(refreshTokenEntity.getToken())) {
            refreshTokenRepository.delete(refreshTokenEntity); // Lo borramos si ya caducó
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado");
        }

        // 3. Generamos un nuevo Access Token corto para el usuario
        Usuario user = refreshTokenEntity.getUsuario();
        String nuevoAccessToken = jwtService.generateToken(user);

        // 4. Retornamos el DTO correspondiente según si es Cliente u otro rol con el nuevo Access Token y el mismo Refresh Token
        if (user instanceof Cliente cliente) {
            return new LoginResponseDTO(nuevoAccessToken, refreshTokenEntity.getToken(), user.getRol().toString(), cliente.getTipoCliente().toString());
        }
        return new LoginResponseDTO(nuevoAccessToken, refreshTokenEntity.getToken(), user.getRol().toString(), null);

    }

    private UserDto toDto(Usuario usuario) {
        return new UserDto(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellidos(),
            usuario.getEmail(),
            usuario.getRol(),
            usuario.isActivo(),
            usuario.isBloqueado()
        );
    }

    public List<UserDto> getAllUsers() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        List<UserDto> usuariosDto = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            usuariosDto.add(toDto(usuario));
        }

        return usuariosDto;
    }

    public UserDto getUserById(String id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);

        if (usuario.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Usuario no encontrado"
            );
        }

        return toDto(usuario.get());
    }
    
}