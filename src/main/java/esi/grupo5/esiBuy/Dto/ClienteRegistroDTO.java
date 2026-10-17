package esi.grupo5.esiBuy.Dto;

import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

/**
 * Datos validados para registrar una cuenta de cliente.
 *
 * @param nombre nombre del cliente
 * @param apellidos apellidos del cliente
 * @param email correo asociado a la cuenta
 * @param contrasena contraseña inicial
 * @param confirmarContrasena confirmación de la contraseña
 * @param telefono teléfono de contacto opcional
 * @param imagenPerfil URL o referencia de la imagen de perfil
 * @param dni documento de identidad
 * @param fechaNacimiento fecha de nacimiento
 * @param tipoCliente nivel de cliente solicitado; si no se indica, se aplica el valor predeterminado
 */
public record ClienteRegistroDTO(
        @NotBlank String nombre,
        @NotBlank String apellidos,
        @NotBlank @Email String email,
        @NotBlank String contrasena,
        @NotBlank String confirmarContrasena,
        String telefono,
        String imagenPerfil,
        @NotBlank String dni,
        @NotNull @Past LocalDate fechaNacimiento,
        TipoCliente tipoCliente
) {}