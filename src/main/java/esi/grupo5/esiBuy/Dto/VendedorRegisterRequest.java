package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos validados para crear una cuenta de vendedor.
 *
 * @param nombre nombre del titular
 * @param apellidos apellidos del titular
 * @param email correo de la cuenta
 * @param contrasena contraseña inicial
 * @param telefono teléfono de contacto opcional
 * @param imagenPerfil URL o referencia de la imagen de perfil
 * @param nombreComercial nombre público del comercio
 * @param cifNif identificador fiscal del vendedor
 * @param categoriaPrincipalId identificador de la categoría principal del catálogo
 */
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
