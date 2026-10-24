package esi.grupo5.esiBuy.Dto;

import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MetodoPagoRequestDTO(

    @NotNull(message = "El tipo de método de pago es obligatorio")
    TipoMetodoPago tipo,

    @NotBlank(message = "El alias es obligatorio")
    String alias,

    // Solo para tarjeta: no se debe persistir el número completo.
    @Pattern(
        regexp = "^(?:[0-9]{12,19}|\\*{4} ?[0-9]{4})$",
        message = "Introduce un número de tarjeta válido o una referencia enmascarada"
    )
    String numeroTarjeta,

    @NotBlank(message = "El nombre del titular es obligatorio para tarjeta")
    String titular,

    @Pattern(
        regexp = "^(0[1-9]|1[0-2])/([0-9]{2}|[0-9]{4})$",
        message = "La caducidad debe tener formato MM/AA o MM/AAAA"
    )
    String caducidad,

    // No recibimos ni guardamos el CVC real.
    @Email(message = "El correo de PayPal no es válido")
    String correoPayPal,

    @Pattern(
        regexp = "^\\+?[0-9]{9,15}$",
        message = "El teléfono de Bizum no es válido"
    )
    String telefonoBizum

) {}