package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.ClienteRegistroDTO;
import esi.grupo5.esiBuy.Dto.VendedorRegisterRequest;
import esi.grupo5.esiBuy.Dto.AuthResponseDTO;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UsuarioRepository usuarioRepository;

    public UserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
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
                .contrasena(dto.contrasena())
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