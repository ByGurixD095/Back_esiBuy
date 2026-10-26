package esi.grupo5.esiBuy.Dto;

import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;

/**
 * Datos devueltos al consultar un método de pago.
 *
 * @param id identificador del método de pago
 * @param tipo tipo de método de pago
 * @param alias nombre identificativo del método de pago
 * @param referencia referencia del método de pago
 * @param titular titular de la tarjeta, si corresponde
 * @param caducidad caducidad de la tarjeta, si corresponde
 */
public record MetodoPagoResponseDTO(String id,TipoMetodoPago tipo,String alias,String referencia,
        String titular,String caducidad) {

    /**
     * Crea una respuesta de método de pago sin datos adicionales de tarjeta.
     *
     * @param id identificador del método de pago
     * @param tipo tipo de método de pago
     * @param alias nombre identificativo del método de pago
     * @param referencia referencia del método de pago
     */
    public MetodoPagoResponseDTO(String id,TipoMetodoPago tipo,String alias,String referencia) {
        this(id, tipo, alias, referencia, null, null);
    }
}