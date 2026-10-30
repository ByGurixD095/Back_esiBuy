package esi.grupo5.esiBuy.Dto;

import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Datos validados para registrar un método de pago.
 *
 * @param tipo tipo de método de pago
 * @param alias nombre identificativo opcional del método de pago
 * @param numeroTarjeta número de tarjeta o referencia enmascarada
 * @param titular nombre del titular de la tarjeta
 * @param caducidad fecha de caducidad de la tarjeta
 * @param correoPayPal correo asociado a PayPal
 * @param telefonoBizum teléfono asociado a Bizum
 */
public record MetodoPagoRequestDTO(
        @NotNull(message = "El tipo de método de pago es obligatorio")
        TipoMetodoPago tipo,

        String alias,

        @Pattern(
                regexp = "^(?:[0-9]{12,19}|\\*{4} ?[0-9]{4})$",
                message = "Introduce un número de tarjeta válido o una referencia enmascarada"
        )
        String numeroTarjeta,

        String titular,

        @Pattern(
                regexp = "^(0[1-9]|1[0-2])/([0-9]{2}|[0-9]{4})$",
                message = "La caducidad debe tener formato MM/AA o MM/AAAA"
        )
        String caducidad,

        @Email(message = "El correo de PayPal no es válido")
        String correoPayPal,

        @Pattern(
                regexp = "^\\+?[0-9]{9,15}$",
                message = "El teléfono de Bizum no es válido"
        )
        String telefonoBizum
) {}
