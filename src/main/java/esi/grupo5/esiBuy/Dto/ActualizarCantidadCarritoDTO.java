package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ActualizarCantidadCarritoDTO(
        @NotNull @Min(1) Integer cantidad
) {
}
