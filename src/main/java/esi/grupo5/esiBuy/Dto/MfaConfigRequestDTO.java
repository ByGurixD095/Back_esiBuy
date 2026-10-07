package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MfaConfigRequestDTO(
    @Email
    @NotBlank
    String email,
    boolean enable2fa,
    boolean enable3fa
) {

}
