package esi.grupo5.esiBuy.Dto;

import java.time.LocalDate;

import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Past;

public record UserPatchDTO(

        String nombre,
        String apellidos,

        @Pattern(regexp = "\\d{9}",message = "El teléfono debe tener 9 dígitos")
        String telefono,

        String imagenPerfil,

        // Cliente
        String dni,

        @Past
        LocalDate fechaNacimiento,
        
        TipoCliente tipoCliente,

        // Vendedor
        String nombreComercial,
        String cifNif,
        String categoriaPrincipalId,

        // Administrador
        String sede
) {
}
