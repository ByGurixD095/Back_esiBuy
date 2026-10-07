package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MfaSetupConfirmDTO(
        @NotBlank(message = "El email es obligatorio") 
        @Email(message = "Formato de email inválido") 
        String email,

        @NotBlank(message = "Debes introducir el código de tu aplicación") 
        String code
) {}
