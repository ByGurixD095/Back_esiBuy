package esi.grupo5.esiBuy.Dto;

/** Línea de producto incluida en la respuesta de un pedido. */
public record LineaPedidoResponseDTO(
        String productoId,
        String nombre,
        String referencia,
        String urlImagen,
        int precioUnitarioCent,
        int cantidad,
        long subtotalCent
) {
}
