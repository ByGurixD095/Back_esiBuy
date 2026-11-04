package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos necesarios para crear una valoración. */
public record ValoracionRequestDTO(
        @NotNull @Min(1) @Max(5) Integer estrellas,
        @NotBlank @Size(max = 2000) String texto) {
}
