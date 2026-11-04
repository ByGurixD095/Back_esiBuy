package esi.grupo5.esiBuy.Dto;

import java.util.List;

public record ProductInfoDTO(
    String nombre,
    int numStock,
    int precioCent,
    String descripcion,
    String categoria,
    String urlImagen,
    int descuento,
    int descuentoPremium,
    String vendedor,
    double mediaEstrellas,
    List<ValoracionResponseDTO> valoraciones,
    boolean puedeValorar
) {
    public ProductInfoDTO(
            String nombre,
            int numStock,
            int precioCent,
            String descripcion,
            String categoria,
            String urlImagen,
            int descuento,
            int descuentoPremium,
            String vendedor) {
        this(nombre, numStock, precioCent, descripcion, categoria, urlImagen, descuento,
                descuentoPremium, vendedor, 0, List.of(), false);
    }
}
