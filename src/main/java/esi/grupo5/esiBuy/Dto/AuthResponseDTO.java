package esi.grupo5.esiBuy.Dto;

import esi.grupo5.esiBuy.Model.enums.Rol;

public record AuthResponseDTO(
        String token,
        String id,
        String nombre,
        String email,
        Rol rol
) {}