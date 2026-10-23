package esi.grupo5.esiBuy.Model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/** Producto marcado como favorito por un cliente; se elimina de forma lógica. */
@Document(collection = "favoritos")
@CompoundIndex(name = "cliente_producto_unico", def = "{'clienteId': 1, 'productoId': 1}", unique = true)
public class Favorito {
    /** Identificador del registro. */
    @Id
    private String id;
    /** Identificador del cliente propietario del favorito. */
    private String clienteId;
    /** Identificador del producto marcado como favorito. */
    private String productoId;
    /** Indica si el favorito está vigente; false cuando el cliente lo quita. */
    private boolean activo;
    /** Fecha y hora en que se marcó como favorito por primera vez. */
    @CreatedDate
    private LocalDateTime fechaAlta;

    /** Constructor requerido por MongoDB; el favorito nace activo. */
    public Favorito() {
        this.activo = true;
    }

    /**
     * Crea un favorito activo para un cliente y un producto.
     *
     * @param clienteId identificador del cliente
     * @param productoId identificador del producto
     */
    public Favorito(String clienteId, String productoId) {
        this();
        this.clienteId = clienteId;
        this.productoId = productoId;
    }

    /** @return identificador del registro */
    public String getId() { return id; }
    /** @param id identificador del registro */
    public void setId(String id) { this.id = id; }
    /** @return identificador del cliente */
    public String getClienteId() { return clienteId; }
    /** @param clienteId identificador del cliente */
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    /** @return identificador del producto */
    public String getProductoId() { return productoId; }
    /** @param productoId identificador del producto */
    public void setProductoId(String productoId) { this.productoId = productoId; }
    /** @return true si el favorito está vigente */
    public boolean isActivo() { return activo; }
    /** @param activo estado del favorito */
    public void setActivo(boolean activo) { this.activo = activo; }
    /** @return fecha y hora de alta */
    public LocalDateTime getFechaAlta() { return fechaAlta; }
    /** @param fechaAlta fecha y hora de alta */
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }
}