package esi.grupo5.esiBuy.Model;

public class ItemCarrito {

    private String productoId;
    private int cantidad;

    public ItemCarrito() {
    }

    public ItemCarrito(String productoId, int cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}