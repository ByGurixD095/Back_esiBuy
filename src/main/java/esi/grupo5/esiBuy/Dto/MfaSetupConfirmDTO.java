package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos para confirmar la configuración de MFA con el código de la aplicación autenticadora.
 *
 * @param email correo de la cuenta que configura MFA
 * @param code código TOTP generado por la aplicación
 * @param setupToken credencial temporal obtenida durante el inicio de configuración
 */
public record MfaSetupConfirmDTO(
        @NotBlank(message = "El email es obligatorio") 
        @Email(message = "Formato de email inválido") 
        String email,

        @NotBlank(message = "Debes introducir el código de tu aplicación") 
        String code,

        @NotBlank(message = "El token de configuración MFA es obligatorio")
        String setupToken
) {}
