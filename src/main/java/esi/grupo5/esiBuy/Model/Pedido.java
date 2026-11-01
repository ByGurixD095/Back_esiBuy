package esi.grupo5.esiBuy.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;

/** Pedido confirmado, con una copia de sus productos, precios y dirección de envío. */
@Document(collection = "pedidos")
public class Pedido {

    @Id
    private String id;
    private String clienteId;
    private List<LineaPedido> lineas = new ArrayList<>();
    private DireccionEnvioPedido direccionEnvio;
    private String metodoPagoId;
    private TipoMetodoPago tipoMetodoPago;
    private String aliasMetodoPago;
    private String referenciaMetodoPago;
    private long totalCent;
    private EstadoPedido estado;
    private LocalDateTime fechaPedido;

    public Pedido() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public List<LineaPedido> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaPedido> lineas) {
        this.lineas = lineas;
    }

    public DireccionEnvioPedido getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(DireccionEnvioPedido direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public String getMetodoPagoId() {
        return metodoPagoId;
    }

    public void setMetodoPagoId(String metodoPagoId) {
        this.metodoPagoId = metodoPagoId;
    }

    public TipoMetodoPago getTipoMetodoPago() {
        return tipoMetodoPago;
    }

    public void setTipoMetodoPago(TipoMetodoPago tipoMetodoPago) {
        this.tipoMetodoPago = tipoMetodoPago;
    }

    public String getAliasMetodoPago() {
        return aliasMetodoPago;
    }

    public void setAliasMetodoPago(String aliasMetodoPago) {
        this.aliasMetodoPago = aliasMetodoPago;
    }

    public String getReferenciaMetodoPago() {
        return referenciaMetodoPago;
    }

    public void setReferenciaMetodoPago(String referenciaMetodoPago) {
        this.referenciaMetodoPago = referenciaMetodoPago;
    }

    public long getTotalCent() {
        return totalCent;
    }

    public void setTotalCent(long totalCent) {
        this.totalCent = totalCent;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }
}
