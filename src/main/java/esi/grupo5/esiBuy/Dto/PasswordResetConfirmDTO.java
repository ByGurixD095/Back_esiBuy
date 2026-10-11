package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Datos para establecer una contraseña mediante un token de recuperación.
 *
 * @param token token temporal recibido en el enlace de recuperación
 * @param pwd1 nueva contraseña
 * @param pwd2 repetición de la nueva contraseña para confirmar
 */
public record PasswordResetConfirmDTO(
        @NotBlank String token,
        @NotBlank String pwd1,
        @NotBlank String pwd2
) {}