package esi.grupo5.esiBuy.Dto;

import esi.grupo5.esiBuy.Model.enums.EstadoPedido;

/** Estado actual de un pedido y su etiqueta legible para el seguimiento del cliente. */
public record EstadoPedidoResponseDTO(String pedidoId, EstadoPedido estado, String estadoVisible) {
}
