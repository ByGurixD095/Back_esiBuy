package esi.grupo5.esiBuy.Dto;

public record ProductInfoDTO(
    String nombre,
    int numStock,
    int precioCent,
    String descripcion,
    String categoria,
    String urlImagen,
    int descuento,
    int descuentoPremium,
    String vendedor
) {}
