
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
import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;
import esi.grupo5.esiBuy.Repository.MetodoPagoRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

@Service
public class MetodoPagoService {

    private final MetodoPagoRepository metodoPagoRepository;
    private final UsuarioRepository usuarioRepository;

    public MetodoPagoService(
            MetodoPagoRepository metodoPagoRepository,
            UsuarioRepository usuarioRepository) {
        this.metodoPagoRepository = metodoPagoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public MetodoPagoResponseDTO registrarMetodoPago(
            String clienteId, MetodoPagoRequestDTO dto) {

        validarCliente(clienteId);
        validarDatosMetodoPago(dto);

        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setClienteId(clienteId);
        metodoPago.setTipo(dto.tipo());
        metodoPago.setAlias(dto.alias());
        metodoPago.setReferencia(obtenerReferenciaSegura(dto));
        if (dto.tipo() == TipoMetodoPago.TARJETA) {
            metodoPago.setTitular(dto.titular().trim());
            metodoPago.setCaducidad(dto.caducidad().trim());
        }

        try {
            return convertirAResponseDTO(metodoPagoRepository.save(metodoPago));
        } catch (DataAccessException e) {
            throw new BusinessException(
                    "Error interno al registrar el método de pago",
                    500, "SERVER_ERROR", e);
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
                    500, "SERVER_ERROR", e);
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

    private void validarDatosMetodoPago(MetodoPagoRequestDTO dto) {
        if (dto == null || dto.tipo() == null) {
            throw new ValidationException(
                    "El tipo de método de pago es obligatorio");
        }

        if (dto.alias() == null || dto.alias().isBlank()) {
            throw new ValidationException("El alias es obligatorio");
        }

        switch (dto.tipo()) {
            case TARJETA -> {
                exigir(dto.numeroTarjeta(), "El número de tarjeta es obligatorio");
                exigir(dto.titular(), "El nombre del titular es obligatorio");
                exigir(dto.caducidad(), "La caducidad es obligatoria");
            }
            case PAYPAL ->
                exigir(dto.correoPayPal(), "El correo de PayPal es obligatorio");
            case BIZUM ->
                exigir(dto.telefonoBizum(), "El teléfono de Bizum es obligatorio");
        }
    }

    private void exigir(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ValidationException(mensaje);
        }
    }

    private String obtenerReferenciaSegura(MetodoPagoRequestDTO dto) {
        return switch (dto.tipo()) {
            case TARJETA -> enmascararTarjeta(dto.numeroTarjeta());
            case PAYPAL -> dto.correoPayPal().trim();
            case BIZUM -> dto.telefonoBizum().trim();
        };
    }

    private String enmascararTarjeta(String numeroTarjeta) {
        String numero = numeroTarjeta.replaceAll("\\s+", "");

        if (numero.matches("\\*{4} ?[0-9]{4}")) {
            return numero;
        }

        if (!numero.matches("[0-9]{12,19}")) {
            throw new ValidationException(
                    "El número de tarjeta no tiene un formato válido");
        }

        return "**** " + numero.substring(numero.length() - 4);
    }

    private MetodoPagoResponseDTO convertirAResponseDTO(
            MetodoPago metodoPago) {
        return new MetodoPagoResponseDTO(
                metodoPago.getId(),
                metodoPago.getTipo(),
                metodoPago.getAlias(),
                metodoPago.getReferencia(),
                metodoPago.getTitular(),
                metodoPago.getCaducidad());
    }
}
