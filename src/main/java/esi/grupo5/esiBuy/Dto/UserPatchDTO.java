package esi.grupo5.esiBuy.Dto;

import java.time.LocalDate;

import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Past;

/**
 * Campos opcionales que un administrador puede modificar en una cuenta.
 *
 * @param nombre nuevo nombre
 * @param apellidos nuevos apellidos
 * @param telefono nuevo teléfono
 * @param imagenPerfil nueva imagen de perfil
 * @param dni documento de identidad de un cliente
 * @param fechaNacimiento fecha de nacimiento de un cliente
 * @param tipoCliente nivel asignado a un cliente
 * @param nombreComercial nombre comercial de un vendedor
 * @param cifNif identificador fiscal de un vendedor
 * @param categoriaPrincipalId categoría principal de un vendedor
 * @param sede sede asignada a un administrador
 */
public record UserPatchDTO(

        String nombre,
        String apellidos,

        @Pattern(regexp = "\\d{9}",message = "El teléfono debe tener 9 dígitos")
        String telefono,

        String imagenPerfil,

        String dni,

        @Past
        LocalDate fechaNacimiento,
        
        TipoCliente tipoCliente,

        String nombreComercial,
        String cifNif,
        String categoriaPrincipalId,

        String sede
) {
}
