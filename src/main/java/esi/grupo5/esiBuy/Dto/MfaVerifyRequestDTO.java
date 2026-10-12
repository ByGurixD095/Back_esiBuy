package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Códigos presentados para completar un desafío de autenticación MFA.
 *
 * @param email correo de la cuenta autenticada
 * @param totpCode código TOTP o código de respaldo
 * @param emailOtpCode código temporal enviado por correo cuando ese factor es requerido
 */
public record MfaVerifyRequestDTO(
@NotBlank(message = "El email es obligatorio") 
        @Email(message = "Formato de email inválido") 
        String email,

        @NotBlank(message = "El código TOTP es obligatorio") 
        String totpCode,
        
        String emailOtpCode          
) {}
