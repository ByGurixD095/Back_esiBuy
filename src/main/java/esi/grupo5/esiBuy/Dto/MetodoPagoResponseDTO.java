
package esi.grupo5.esiBuy.Dto;

import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;

public record MetodoPagoResponseDTO(
        String id,
        TipoMetodoPago tipo,
        String alias,
        String referencia
) {}
