package esi.grupo5.esiBuy.Dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Datos necesarios para confirmar el carrito como pedido. */
public record CrearPedidoRequestDTO(
        @NotBlank(message = "El método de pago es obligatorio")
        String metodoPagoId,

        @NotNull(message = "La dirección de envío es obligatoria")
        @Valid
        DireccionEnvioDTO direccionEnvio
) {
}
