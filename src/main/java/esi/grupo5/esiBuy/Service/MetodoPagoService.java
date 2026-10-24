
package esi.grupo5.esiBuy.Service;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Dto.MetodoPagoRequestDTO;
import esi.grupo5.esiBuy.Dto.MetodoPagoResponseDTO;
import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.MetodoPago;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Repository.MetodoPagoRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

@Service
public class MetodoPagoService {

    private final MetodoPagoRepository metodoPagoRepository;
    private final UsuarioRepository usuarioRepository;

    public MetodoPagoService(MetodoPagoRepository metodoPagoRepository,UsuarioRepository usuarioRepository) {
        this.metodoPagoRepository = metodoPagoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public MetodoPagoResponseDTO registrarMetodoPago(
            String clienteId, MetodoPagoRequestDTO dto) {

        validarCliente(clienteId);

        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setClienteId(clienteId);
        metodoPago.setTipo(dto.tipo());
        metodoPago.setAlias(dto.alias());
        metodoPago.setReferencia(dto.referencia());

        try {
            MetodoPago guardado = metodoPagoRepository.save(metodoPago);
            return convertirAResponseDTO(guardado);
        } catch (DataAccessException e) {
            throw new BusinessException(
                    "Error interno al registrar el método de pago",
                    500,
                    "SERVER_ERROR",
                    e);
        }
    }

    public List<MetodoPagoResponseDTO> listarMetodosPago(String clienteId) {
        validarCliente(clienteId);

        try {
            return metodoPagoRepository.findByClienteId(clienteId)
                    .stream()
                    .map(this::convertirAResponseDTO)
                    .toList();
        } catch (DataAccessException e) {
            throw new BusinessException(
                    "Error interno al consultar los métodos de pago",
                    500,
                    "SERVER_ERROR",
                    e);
        }
    }

    private void validarCliente(String clienteId) {
        Usuario usuario = usuarioRepository.findById(clienteId)
                .orElseThrow(() ->
                        new NotFoundException("Cliente no encontrado"));

        if (!(usuario instanceof Cliente)) {
            throw new ValidationException(
                    "Solo los clientes pueden gestionar métodos de pago");
        }
    }

    private MetodoPagoResponseDTO convertirAResponseDTO(MetodoPago metodoPago) {
        return new MetodoPagoResponseDTO(metodoPago.getId(),metodoPago.getTipo(),metodoPago.getAlias(),metodoPago.getReferencia());
    }
}
