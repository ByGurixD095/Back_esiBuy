package esi.grupo5.esiBuy.Dto;

public record AdministradorResponseDTO(
        String id,
        String nombre,
        String apellidos,
        String email,
        String sede,
        String rol,
        String mensaje
) {}