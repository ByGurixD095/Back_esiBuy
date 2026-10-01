package esi.grupo5.esiBuy.Dto;

import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record ClienteRegistroDTO(
        @NotBlank String nombre,
        @NotBlank String apellidos,
        @NotBlank @Email String email,
        @NotBlank String contrasena,
        String telefono,
        String imagenPerfil,
        @NotBlank String dni,
        @NotNull @Past LocalDate fechaNacimiento,
        TipoCliente tipoCliente
) {}