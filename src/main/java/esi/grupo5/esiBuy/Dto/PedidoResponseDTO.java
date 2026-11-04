package esi.grupo5.esiBuy.Dto;

import java.time.LocalDateTime;
import java.util.List;

import esi.grupo5.esiBuy.Model.EstadoPedido;
import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;

/** Vista de un pedido para el cliente propietario o las líneas de un vendedor. */
public record PedidoResponseDTO(
        String id,
        List<LineaPedidoResponseDTO> lineas,
        DireccionEnvioDTO direccionEnvio,
        String metodoPagoId,
        TipoMetodoPago tipoMetodoPago,
        String aliasMetodoPago,
        String referenciaMetodoPago,
        long totalCent,
        EstadoPedido estado,
        LocalDateTime fechaPedido,
        String nombreCliente
) {
}
