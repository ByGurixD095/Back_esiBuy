package esi.grupo5.esiBuy.Dto;

import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import jakarta.validation.constraints.Pattern;

public record UserSelfUpdateDTO(
        String nombre,
        String apellidos,

        @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos")
        String telefono,

        String imagenPerfil,

        // Campos modificables por el Cliente
        TipoCliente tipoCliente,

        // Campos modificables por el Vendedor
        String categoriaPrincipalId,
        String nombreComercial
) {}