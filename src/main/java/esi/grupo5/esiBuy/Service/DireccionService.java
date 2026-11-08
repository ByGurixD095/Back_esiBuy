package esi.grupo5.esiBuy.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Dto.DireccionEnvioDTO;
import esi.grupo5.esiBuy.Dto.DireccionGuardadaResponseDTO;
import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.DireccionGuardada;
import esi.grupo5.esiBuy.Model.DireccionEnvioPedido;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.EstadoPedido;
import esi.grupo5.esiBuy.Repository.PedidoRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

/** Persiste las direcciones como parte de la cuenta del cliente propietario. */
@Service
public class DireccionService {

    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;

    public DireccionService(UsuarioRepository usuarioRepository, PedidoRepository pedidoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public List<DireccionGuardadaResponseDTO> listar(String clienteId) {
        Cliente cliente = obtenerCliente(clienteId);
        return direccionesDe(cliente).stream().map(this::convertir).toList();
    }

    public DireccionGuardadaResponseDTO crear(String clienteId, DireccionEnvioDTO dto) {
        Cliente cliente = obtenerCliente(clienteId);
        DireccionGuardada direccion = convertir(dto);
        direccionesDe(cliente).add(direccion);
        guardar(cliente);
        return convertir(direccion);
    }

    public DireccionGuardadaResponseDTO actualizar(
            String clienteId, String direccionId, DireccionEnvioDTO dto) {
        Cliente cliente = obtenerCliente(clienteId);
        DireccionGuardada direccion = direccionesDe(cliente).stream()
                .filter(actual -> actual.getId().equals(direccionId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Dirección no encontrada"));
        
        validarDireccionNoEnUso(clienteId, direccion);
        aplicar(dto, direccion);
        guardar(cliente);
        return convertir(direccion);
    }

    public void eliminar(String clienteId, String direccionId) {
        Cliente cliente = obtenerCliente(clienteId);
        
        DireccionGuardada direccion = direccionesDe(cliente).stream()
                .filter(actual -> actual.getId().equals(direccionId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Dirección no encontrada"));
                
        validarDireccionNoEnUso(clienteId, direccion);
        
        direccionesDe(cliente).remove(direccion);
        guardar(cliente);
    }

    private Cliente obtenerCliente(String clienteId) {
        try {
            Usuario usuario = usuarioRepository.findById(clienteId)
                    .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
            if (!(usuario instanceof Cliente cliente)) {
                throw new ValidationException("Solo los clientes pueden gestionar direcciones");
            }
            return cliente;
        } catch (DataAccessException e) {
            throw new BusinessException("Error interno al consultar las direcciones", 500, "SERVER_ERROR", e);
        }
    }

    private List<DireccionGuardada> direccionesDe(Cliente cliente) {
        if (cliente.getDirecciones() == null) {
            cliente.setDirecciones(new ArrayList<>());
        }
        return cliente.getDirecciones();
    }

    private DireccionGuardada convertir(DireccionEnvioDTO dto) {
        DireccionGuardada direccion = new DireccionGuardada();
        aplicar(dto, direccion);
        return direccion;
    }

    private void aplicar(DireccionEnvioDTO dto, DireccionGuardada direccion) {
        direccion.setNombreCompleto(dto.nombreCompleto().trim());
        direccion.setDireccion(dto.direccion().trim());
        direccion.setCiudad(dto.ciudad().trim());
        direccion.setProvincia(dto.provincia().trim());
        direccion.setCodigoPostal(dto.codigoPostal().trim());
        direccion.setPais(dto.pais().trim());
        direccion.setTelefono(dto.telefono().trim());
    }

    private void validarDireccionNoEnUso(String clienteId, DireccionGuardada direccion) {
        List<EstadoPedido> estadosActivos = List.of(
                EstadoPedido.EN_PROCESO, EstadoPedido.PAGADO, EstadoPedido.TRAMITADO_ENVIADO);
                
        boolean enUso = pedidoRepository.findAllByClienteId(clienteId).stream()
                .filter(p -> estadosActivos.contains(p.getEstado()))
                .map(p -> p.getDireccionEnvio())
                .anyMatch(d -> coincideCon(d, direccion));
                        
        if (enUso) {
            throw new ConflictException("No se puede modificar ni eliminar una dirección que está en uso en un pedido en curso.");
        }
    }

    private boolean coincideCon(DireccionEnvioPedido pedido, DireccionGuardada guardada) {
        return pedido != null
                && coincideUbicacion(pedido, guardada)
                && coincideLocalidad(pedido, guardada);
    }

    private boolean coincideUbicacion(DireccionEnvioPedido pedido, DireccionGuardada guardada) {
        return pedido.getDireccion().equals(guardada.getDireccion())
                && pedido.getCiudad().equals(guardada.getCiudad());
    }

    private boolean coincideLocalidad(DireccionEnvioPedido pedido, DireccionGuardada guardada) {
        return pedido.getCodigoPostal().equals(guardada.getCodigoPostal())
                && pedido.getPais().equals(guardada.getPais());
    }

    private DireccionGuardadaResponseDTO convertir(DireccionGuardada direccion) {
        return new DireccionGuardadaResponseDTO(
                direccion.getId(),
                direccion.getNombreCompleto(),
                direccion.getDireccion(),
                direccion.getCiudad(),
                direccion.getProvincia(),
                direccion.getCodigoPostal(),
                direccion.getPais(),
                direccion.getTelefono());
    }

    private void guardar(Cliente cliente) {
        try {
            usuarioRepository.save(cliente);
        } catch (DataAccessException e) {
            throw new BusinessException("Error interno al guardar las direcciones", 500, "SERVER_ERROR", e);
        }
    }
}
