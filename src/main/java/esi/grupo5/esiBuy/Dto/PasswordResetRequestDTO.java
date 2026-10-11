package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud para enviar un enlace de recuperación de contraseña.
 *
 * @param email correo de la cuenta para la que se solicita la recuperación
 */
public record PasswordResetRequestDTO(
        @NotBlank @Email String email
) {}