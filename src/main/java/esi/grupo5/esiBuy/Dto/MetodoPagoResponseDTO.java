
package esi.grupo5.esiBuy.Dto;

import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;

public record MetodoPagoResponseDTO(
        String id,
        TipoMetodoPago tipo,
        String alias,
        String referencia,
        String titular,
        String caducidad
) {
    public MetodoPagoResponseDTO(
            String id,
            TipoMetodoPago tipo,
            String alias,
            String referencia) {
        this(id, tipo, alias, referencia, null, null);
    }
}
