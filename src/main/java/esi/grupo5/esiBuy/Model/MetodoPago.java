package esi.grupo5.esiBuy.Model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;
@Document(collection = "metodos_pago")
public class MetodoPago {
     @Id
    private String id;

    private String clienteId;

    @NotNull(message = "El tipo de método de pago es obligatorio")
    private TipoMetodoPago tipo;

    @NotBlank(message = "El alias es obligatorio")
    private String alias;

    @NotBlank(message = "La referencia es obligatoria")
    private String referencia;

    private String titular;

    private String caducidad;

    public MetodoPago() {
    }

    public MetodoPago(String clienteId, TipoMetodoPago tipo,String alias,String referencia) {
        this.clienteId = clienteId;
        this.tipo = tipo;
        this.alias = alias;
        this.referencia = referencia;
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

    public TipoMetodoPago getTipo() {
        return tipo;
    }

    public void setTipo(TipoMetodoPago tipo) {
        this.tipo = tipo;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getCaducidad() {
        return caducidad;
    }

    public void setCaducidad(String caducidad) {
        this.caducidad = caducidad;
    }
}
