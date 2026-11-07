package esi.grupo5.esiBuy.Dto;

/** Línea de producto incluida en la respuesta de un pedido. El campo {@code enviado} indica si el vendedor autenticado ya ha tramitado su envío. */
public record LineaPedidoResponseDTO(
        String productoId,
        String nombre,
        String referencia,
        String urlImagen,
        boolean enviado,
        int precioUnitarioCent,
        int cantidad,
        long subtotalCent
) {
}
