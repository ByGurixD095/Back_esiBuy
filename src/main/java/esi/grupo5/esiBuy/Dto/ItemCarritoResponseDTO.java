package esi.grupo5.esiBuy.Dto;

public record ItemCarritoResponseDTO(
    String productoId,
    String nombre,
    int precioCent,
    String urlImagen,
    int cantidad
) {
    
}