package esi.grupo5.esiBuy.Dto;

/** Dirección de envío guardada para reutilizar en futuros pedidos. */
public record DireccionGuardadaResponseDTO(
        String id,
        String nombreCompleto,
        String direccion,
        String ciudad,
        String provincia,
        String codigoPostal,
        String pais,
        String telefono) {
}
