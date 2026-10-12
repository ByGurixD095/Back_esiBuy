package esi.grupo5.esiBuy.Dto;

import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import jakarta.validation.constraints.Pattern;

/**
 * Campos que un usuario puede modificar de su propio perfil.
 *
 * @param nombre nuevo nombre, opcional
 * @param apellidos nuevos apellidos, opcionales
 * @param telefono nuevo teléfono, opcional
 * @param imagenPerfil nueva imagen de perfil, opcional
 * @param tipoCliente tipo de cliente, aplicable a cuentas de cliente
 * @param categoriaPrincipalId nueva categoría principal, aplicable a vendedores
 * @param nombreComercial nuevo nombre comercial, aplicable a vendedores
 */
public record UserSelfUpdateDTO(
        String nombre,
        String apellidos,

        @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos")
        String telefono,

        String imagenPerfil,

        TipoCliente tipoCliente,

        String categoriaPrincipalId,
        String nombreComercial
) {}