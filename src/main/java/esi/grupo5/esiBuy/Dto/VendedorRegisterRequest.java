package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VendedorRegisterRequest(
        @NotBlank String nombre,
        @NotBlank String apellidos,
        @NotBlank @Email String email,
        @NotBlank String contrasena,
        String telefono,
        String imagenPerfil,
        @NotBlank String nombreComercial,
        @NotBlank String cifNif,
        @NotBlank String categoriaPrincipalId
) {}
