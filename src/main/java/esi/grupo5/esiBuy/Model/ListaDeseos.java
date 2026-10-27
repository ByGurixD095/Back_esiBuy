package esi.grupo5.esiBuy.Model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "listaDeseos")
public class ListaDeseos {
    @Id
    private String id;
    private String idCliente;
    private String nombre;
    private Set<String> productosIds = new HashSet<>();

    public ListaDeseos() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Set<String> getProductosIds() {
        return productosIds;
    }

    public void setProductosIds(Set<String> productosIds) {
        this.productosIds = productosIds;
    }
}
