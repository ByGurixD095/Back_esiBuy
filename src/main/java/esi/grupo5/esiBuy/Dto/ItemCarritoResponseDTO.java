package esi.grupo5.esiBuy.Dto;

public record ItemCarritoResponseDTO(
    String productoId,
    String nombre,
    int precioCent,
    String urlImagen,
    int cantidad,
    String idVendedor,
    String vendedor
) {
    public ItemCarritoResponseDTO(
        String productoId,
        String nombre,
        int precioCent,
        String urlImagen,
        int cantidad
    ) {
        this(productoId, nombre, precioCent, urlImagen, cantidad, null, "Vendedor");
    }
}
