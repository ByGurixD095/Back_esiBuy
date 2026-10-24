
package esi.grupo5.esiBuy.Dto;

import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MetodoPagoRequestDTO(
        @NotNull(message = "El tipo de método de pago es obligatorio")
        TipoMetodoPago tipo,

        @NotBlank(message = "El alias es obligatorio")
        String alias,

        @NotBlank(message = "La referencia es obligatoria")
        String referencia
) {}
