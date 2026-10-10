package esi.grupo5.esiBuy.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "productos")
public class Producto {
    
    @Id
    private String id;
    private String nombre;
    private String referencia;
    private int numStock;
    private int precioCent; /* EL PRECIO IRÁ EN CÉNTIMOS, EN FRONTEND EUROS */
    private String descripcion;
    private String categoria;
    private String urlImagen;
    private boolean activo; // Control para el borrado lógico del producto
    private int descuento; // Un descuento = 5 será de un 5%
    private int descuentoPremium;
    String idVendedor; // NEW para asociar cada producto a su vendedor

    public Producto() {
        this.activo = true; // Por defecto, el producto está activo
    }

    public Producto(String nombre, String referencia, int numStock, int precioCent, 
                    String descripcion, String categoria, String urlImagen, int descuento, int descuentoPremium) {
        this();
        this.nombre = nombre;
        this.referencia = referencia;
        this.numStock = numStock;
        this.precioCent = precioCent;
        this.descripcion = descripcion;
        this.categoria = categoria; // SELECCIONABLE
        this.urlImagen = urlImagen;
        this.descuento = descuento;
        this.descuentoPremium = descuentoPremium;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public int getNumStock() { return numStock; }
    public void setNumStock(int numStock) { this.numStock = numStock; }

    public int getPrecioCent() { return precioCent; }
    public void setPrecioCent(int precioCent) { this.precioCent = precioCent; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getDescuento() { return descuento; }
    public void setDescuento(int descuento) { this.descuento = descuento; }

    public int getDescuentoPremium() { return descuentoPremium; }
    public void setDescuentoPremium(int descuentoPremium) { this.descuentoPremium = descuentoPremium; }

}

