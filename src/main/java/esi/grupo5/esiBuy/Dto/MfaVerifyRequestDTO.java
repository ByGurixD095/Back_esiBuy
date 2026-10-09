package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MfaVerifyRequestDTO(
@NotBlank(message = "El email es obligatorio") 
        @Email(message = "Formato de email inválido") 
        String email,

        @NotBlank(message = "El código TOTP es obligatorio") 
        String totpCode,
        
        String emailOtpCode          
) {}
