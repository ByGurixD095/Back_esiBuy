package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetConfirmDTO(
        @NotBlank String token,
        @NotBlank String pwd1,
        @NotBlank String pwd2
) {}