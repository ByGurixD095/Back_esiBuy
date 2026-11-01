package esi.grupo5.esiBuy.Model;

/** Datos de un producto tal como estaban cuando se confirmó el pedido. */
public class LineaPedido {

    private String productoId;
    private String nombre;
    private String referencia;
    private String urlImagen;
    private int precioUnitarioCent;
    private int cantidad;
    private long subtotalCent;

    public LineaPedido() {
    }

    public LineaPedido(
            String productoId,
            String nombre,
            String referencia,
            String urlImagen,
            int precioUnitarioCent,
            int cantidad,
            long subtotalCent) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.referencia = referencia;
        this.urlImagen = urlImagen;
        this.precioUnitarioCent = precioUnitarioCent;
        this.cantidad = cantidad;
        this.subtotalCent = subtotalCent;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public int getPrecioUnitarioCent() {
        return precioUnitarioCent;
    }

    public void setPrecioUnitarioCent(int precioUnitarioCent) {
        this.precioUnitarioCent = precioUnitarioCent;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public long getSubtotalCent() {
        return subtotalCent;
    }

    public void setSubtotalCent(long subtotalCent) {
        this.subtotalCent = subtotalCent;
    }
}
