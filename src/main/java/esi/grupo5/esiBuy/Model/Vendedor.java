package esi.grupo5.esiBuy.Model;

import java.util.ArrayList;
import java.util.List;

public class Vendedor extends Usuario {
    private List<Producto> productos = new ArrayList<>();
    private List<Pedido> pedidos = new ArrayList<>();

    public Vendedor(String username, String password) {
        super(username, password);
    }

    //------ SETTERS & GETERS --------
    public List<Producto> getProductos() { return productos; }
    public List<Pedido> getPedidos() { return pedidos; }

    //------ METHODS --------
    public void gestionarProducto() { /* TODO */ }
    public void gestionarStock() { /* TODO */ }
    public void gestionarOfertas() { /* TODO */ }
    public void consultarEstadisticas() { /* TODO */ }

}
