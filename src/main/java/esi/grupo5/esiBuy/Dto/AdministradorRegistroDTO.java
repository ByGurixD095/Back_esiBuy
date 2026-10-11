package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos validados para registrar una cuenta de administrador.
 *
 * @param nombre nombre del administrador
 * @param apellidos apellidos del administrador
 * @param email dirección de correo que identificará la cuenta
 * @param contrasena contraseña inicial de la cuenta
 * @param sede sede asignada al administrador
 */
public record AdministradorRegistroDTO(
        @NotBlank String nombre,
        @NotBlank String apellidos,
        @NotBlank @Email String email,
        @NotBlank
        @Size(min = 8, max = 64)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).+$")
        String contrasena,
        String sede
) {}