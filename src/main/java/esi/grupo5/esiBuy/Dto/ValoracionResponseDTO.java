package esi.grupo5.esiBuy.Dto;

import java.time.LocalDateTime;

/** Información pública de una valoración. */
public record ValoracionResponseDTO(
        String nombreUsuario,
        int estrellas,
        String texto,
        LocalDateTime fecha) {
}
