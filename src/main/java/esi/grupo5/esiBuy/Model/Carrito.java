package esi.grupo5.esiBuy.Model;

import java.util.List;
import java.util.ArrayList;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Document(collection = "carritos")
public class Carrito {

    @Id
    private String id;

    private String clienteId;

    private List<ItemCarrito> items = new ArrayList<>();

    public Carrito() {
    }

    public Carrito(String clienteId) {
        this.clienteId = clienteId;
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

    public List<ItemCarrito> getItems() {
        return items;
    }

    public void setItems(List<ItemCarrito> items) {
        this.items = items;
    }

}

