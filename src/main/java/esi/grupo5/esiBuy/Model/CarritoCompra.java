package esi.grupo5.esiBuy.Model;

import java.util.ArrayList;
import java.util.List;

public class CarritoCompra {
    
    private List<Producto> productos = new ArrayList<>();

    public void anadirProducto(Producto producto) {
        productos.add(producto);
    }

    public List<Producto> getProductos() { return productos; }
}
