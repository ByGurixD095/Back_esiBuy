package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dirección que se conserva como copia dentro del pedido. */
public record DireccionEnvioDTO(
        @NotBlank(message = "El nombre del destinatario es obligatorio")
        @Size(max = 120, message = "El nombre del destinatario es demasiado largo")
        String nombreCompleto,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200, message = "La dirección es demasiado larga")
        String direccion,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 100, message = "La ciudad es demasiado larga")
        String ciudad,

        @NotBlank(message = "La provincia es obligatoria")
        @Size(max = 100, message = "La provincia es demasiado larga")
        String provincia,

        @NotBlank(message = "El código postal es obligatorio")
        @Size(max = 20, message = "El código postal es demasiado largo")
        String codigoPostal,

        @NotBlank(message = "El país es obligatorio")
        @Size(max = 100, message = "El país es demasiado largo")
        String pais,

        @NotBlank(message = "El teléfono de contacto es obligatorio")
        @Size(max = 30, message = "El teléfono de contacto es demasiado largo")
        String telefono
) {
}
