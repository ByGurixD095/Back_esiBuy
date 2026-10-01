package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.RefreshToken;
import esi.grupo5.esiBuy.Dto.ClienteRegistroDTO;
import esi.grupo5.esiBuy.Dto.VendedorRegisterRequest;
import esi.grupo5.esiBuy.Dto.AuthResponseDTO;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public AuthResponseDTO registrarCliente(@Valid ClienteRegistroDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Cliente cliente = Cliente.builder()
                .nombre(dto.nombre())
                .apellidos(dto.apellidos())
                .email(dto.email())
                .contrasena(dto.contrasena())
                .telefono(dto.telefono())
                .imagenPerfil(dto.imagenPerfil())
                .dni(dto.dni())
                .fechaNacimiento(dto.fechaNacimiento())
                .tipoCliente(dto.tipoCliente() != null ? dto.tipoCliente() : TipoCliente.NORMAL)
                .build();

        cliente.setActivo(true);

        Cliente clientSaved = usuarioRepository.save(cliente);

        //TODO: Guardar un toten de verdad cuando el servicio token de Alberto funcione
        String token = "dummy-token";

        return new AuthResponseDTO(
                token,
                clientSaved.getId(),
                clientSaved.getNombre(),
                clientSaved.getEmail(),
                clientSaved.getRol()
        );
    }

    @Transactional
    public AuthResponseDTO registrarVendedor(@Valid VendedorRegisterRequest dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Vendedor vendedor = Vendedor.builder()
                .nombre(dto.nombre())
                .apellidos(dto.apellidos())
                .email(dto.email())
                .contrasena(dto.contrasena())   // Encriptar con encoder.encode(dto.contrasena()) si se desea almacenar la contraseña encriptada
                .telefono(dto.telefono())
                .imagenPerfil(dto.imagenPerfil())
                .nombreComercial(dto.nombreComercial())
                .cifNif(dto.cifNif())
                .categoriaPrincipalId(dto.categoriaPrincipalId())
                .build();

        vendedor.setActivo(true);
        Vendedor guardado = usuarioRepository.save(vendedor);

        //TODO: Guardar un token de verdad cuando el servicio token de Alberto funcione
        String token = "dummy-jwt-token";

        return new AuthResponseDTO(
                token,
                guardado.getId(),
                guardado.getNombre(),
                guardado.getEmail(),
                guardado.getRol()
        );
    }
}