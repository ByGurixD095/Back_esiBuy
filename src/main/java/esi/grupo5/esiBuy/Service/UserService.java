package esi.grupo5.esiBuy.Service;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.ClienteRegistroDTO;
import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.VendedorRegisterRequest;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.RefreshToken;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

@Service
public class UserService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordValidatorService passwordValidatorService;
    private final LoginAttemptService loginAttempService;

    public UserService(UsuarioRepository usuarioRepository, JwtService jwtService, RefreshTokenRepository refreshTokenRepository,
                         PasswordValidatorService passwordValidatorService, LoginAttemptService loginAttempService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordValidatorService = passwordValidatorService;
        this.loginAttempService = loginAttempService;
    }


    //-------- LOGIN ------------------------------
    public LoginResponseDTO login(LoginRequestDTO loginRequest, String ipAddress) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(loginRequest.username());
        if (optionalUsuario.isEmpty() || !encoder.matches(loginRequest.password(), optionalUsuario.get().getContrasena())) {
            loginAttempService.registerFailedLogin(ipAddress);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        return generarTokens(optionalUsuario.get());
    }

    //-------- TOKENS ------------------------------
    private LoginResponseDTO generarTokens(Usuario usuario) {
        String token = jwtService.generateToken(usuario);
        String refreshTokenString = jwtService.generateRefreshToken(usuario);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenString);
        refreshToken.setUsuario(usuario);
        long expirationSeconds = jwtService.getRefreshTokenExpirationSeconds();
        refreshToken.setFechaExpiracion(LocalDateTime.now().plusSeconds(expirationSeconds));
        
        try {
            refreshTokenRepository.save(refreshToken);
        } catch (Exception e) { 
            log.error("Error de base de datos al guardar el refresh token del usuario {}: {}", usuario.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno");
        }

        String tipoCliente = (usuario instanceof Cliente cliente) ? cliente.getTipoCliente().toString() : null;
        
        return new LoginResponseDTO(
                token, 
                refreshTokenString, 
                usuario.getRol().toString(), 
                tipoCliente
        );
    }

    public LoginResponseDTO refreshToken(String refreshTokenString) {
        // 1. Comprobamos que el token de refresco exista en la base de datos
        Optional<RefreshToken> optionalRefreshToken;

        try {
            optionalRefreshToken = refreshTokenRepository.findByToken(refreshTokenString);
        } catch (Exception e) {
            log.error("Error de base de datos al buscar el refresh token: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno de validación");
        }

        if (optionalRefreshToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de refresco inválido");
        }

        RefreshToken refreshTokenEntity = optionalRefreshToken.get();

        // 2. Comprobamos que la firma matemática siga siendo válida (que no haya caducado)
        if (!jwtService.isTokenValid(refreshTokenEntity.getToken())) {
            try {
                // Lo borramos si ya caducó.
                refreshTokenRepository.delete(refreshTokenEntity); 
            } catch (Exception e) {
                log.error("Error al eliminar refresh token expirado para el usuario {}: {}", 
                          refreshTokenEntity.getUsuario().getEmail(), e.getMessage());
            }

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado");
        }

        // 3. Generamos un nuevo Access Token corto para el usuario
        Usuario usuario = refreshTokenEntity.getUsuario(); 
        String nuevoAccessToken = jwtService.generateToken(usuario);

        // 4. Retornamos el DTO correspondiente según si es Cliente u otro rol
        String tipoCliente = (usuario instanceof Cliente cliente) ? cliente.getTipoCliente().toString() : null;
        
        return new LoginResponseDTO(
                nuevoAccessToken, 
                refreshTokenEntity.getToken(), 
                usuario.getRol().toString(), 
                tipoCliente
        );

    }

    //-------- REGISTER  ------------------------------

    @Transactional
    public LoginResponseDTO registrarCliente(@Valid ClienteRegistroDTO dto) {
        try{
            passwordValidatorService.passwordIsWeak(dto.contrasena(), new ArrayList<>(), encoder);
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no cumple con los requisitos de seguridad: " + e.getReason());
        }
        
        Cliente cliente = Cliente.builder()
                .nombre(dto.nombre())
                .apellidos(dto.apellidos())
                .email(dto.email())
                .contrasena(encoder.encode(dto.contrasena()))
                .telefono(dto.telefono())
                .imagenPerfil(dto.imagenPerfil())
                .dni(dto.dni())
                .fechaNacimiento(dto.fechaNacimiento())
                .tipoCliente(dto.tipoCliente() != null ? dto.tipoCliente() : TipoCliente.NORMAL)
                .build();

        cliente.setHistorialContrasenas(new ArrayList<>(List.of(cliente.getContrasena())));
        return procesarRegistroUsuario(cliente);
    }

    @Transactional
    public LoginResponseDTO registrarVendedor(@Valid VendedorRegisterRequest dto) {
        try{
           passwordValidatorService.passwordIsWeak(dto.contrasena(), new ArrayList<>(), encoder);
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no cumple con los requisitos de seguridad: " + e.getReason());
        }

        Vendedor vendedor = Vendedor.builder()
                .nombre(dto.nombre())
                .apellidos(dto.apellidos())
                .email(dto.email())
                .contrasena(encoder.encode(dto.contrasena()))
                .telefono(dto.telefono())
                .imagenPerfil(dto.imagenPerfil())
                .nombreComercial(dto.nombreComercial())
                .cifNif(dto.cifNif())
                .categoriaPrincipalId(dto.categoriaPrincipalId())
                .build();

        vendedor.setHistorialContrasenas(new ArrayList<>(List.of(vendedor.getContrasena())));
        return procesarRegistroUsuario(vendedor);
    }

    private LoginResponseDTO procesarRegistroUsuario(Usuario usuario) {
        boolean existeEmail;
        try {
            existeEmail = usuarioRepository.existsByEmail(usuario.getEmail());
        } catch (Exception e) {
            log.error("Error al comprobar la existencia del email {}: {}", usuario.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al verificar el usuario");
        }

        if (existeEmail) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }

        usuario.setActivo(true);

        Usuario usuarioGuardado;
        try {
            usuarioGuardado = usuarioRepository.save(usuario);
        } catch (Exception e) {
            log.error("Error al guardar el nuevo usuario {}: {}", usuario.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al registrar el usuario");
        }

        return generarTokens(usuarioGuardado);
    }
}