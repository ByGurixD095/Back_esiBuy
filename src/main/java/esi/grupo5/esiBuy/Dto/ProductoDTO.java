package esi.grupo5.esiBuy.Dto;

public record ProductoDTO(
    String nombre, 
    String referencia, 
    int numStock, 
    int precioCent, /* EL PRECIO IRÁ EN CÉNTIMOS, EN FRONTEND EUROS */
    String descripcion, 
    String categoria, 
    String urlImagen,
    int descuento,
    int descuentoPremium
) {
    

}
