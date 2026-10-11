package esi.grupo5.esiBuy.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Producto persistido en MongoDB, con existencias, descuentos y vendedor propietario.
 */
@Document(collection = "productos")
public class Producto {
    /** Identificador persistente del producto. */
    @Id
    private String id;
    /** Nombre mostrado en el catálogo. */
    private String nombre;
    /** Referencia comercial utilizada para identificar el producto. */
    private String referencia;
    /** Unidades disponibles en inventario. */
    private int numStock;
    /** Precio del producto expresado en céntimos. */
    private int precioCent;
    /** Descripción visible en el catálogo. */
    private String descripcion;
    /** Categoría a la que pertenece el producto. */
    private String categoria;
    /** URL o referencia de la imagen del producto. */
    private String urlImagen;
    /** Indica si el producto está publicado; se usa para el borrado lógico. */
    private boolean activo;
    /** Porcentaje del descuento general. */
    private int descuento;
    /** Porcentaje del descuento aplicable a clientes prémium. */
    private int descuentoPremium;
    /** Identificador del vendedor propietario del producto. */
    private String idVendedor;

    /** Crea un producto nuevo activo por defecto. */
    public Producto() {
        this.activo = true;
    }

    /**
     * Crea un producto con sus datos de catálogo e inventario.
     *
     * @param nombre nombre visible
     * @param referencia referencia comercial
     * @param numStock unidades disponibles
     * @param precioCent precio en céntimos
     * @param descripcion descripción
     * @param categoria categoría
     * @param urlImagen URL o referencia de imagen
     * @param descuento porcentaje de descuento general
     * @param descuentoPremium porcentaje de descuento prémium
     */
    public Producto(String nombre, String referencia, int numStock, int precioCent, 
                    String descripcion, String categoria, String urlImagen, int descuento, int descuentoPremium) {
        this();
        this.nombre = nombre;
        this.referencia = referencia;
        this.numStock = numStock;
        this.precioCent = precioCent;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.urlImagen = urlImagen;
        this.descuento = descuento;
        this.descuentoPremium = descuentoPremium;
    }

    /** @return identificador persistente */
    public String getId() { return id; }
    /** @param id identificador persistente */
    public void setId(String id) { this.id = id; }

    /** @return nombre del producto */
    public String getNombre() { return nombre; }
    /** @param nombre nombre del producto */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** @return referencia comercial */
    public String getReferencia() { return referencia; }
    /** @param referencia referencia comercial */
    public void setReferencia(String referencia) { this.referencia = referencia; }

    /** @return unidades disponibles */
    public int getNumStock() { return numStock; }
    /** @param numStock unidades disponibles */
    public void setNumStock(int numStock) { this.numStock = numStock; }

    /** @return precio expresado en céntimos */
    public int getPrecioCent() { return precioCent; }
    /** @param precioCent precio expresado en céntimos */
    public void setPrecioCent(int precioCent) { this.precioCent = precioCent; }

    /** @return descripción visible */
    public String getDescripcion() { return descripcion; }
    /** @param descripcion descripción visible */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** @return categoría asignada */
    public String getCategoria() { return categoria; }
    /** @param categoria categoría asignada */
    public void setCategoria(String categoria) { this.categoria = categoria; }

    /** @return URL o referencia de la imagen */
    public String getUrlImagen() { return urlImagen; }
    /** @param urlImagen URL o referencia de la imagen */
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }

    /** @return {@code true} cuando el producto está publicado */
    public boolean isActivo() { return activo; }
    /** @param activo nuevo estado de publicación */
    public void setActivo(boolean activo) { this.activo = activo; }

    /** @return porcentaje de descuento general */
    public int getDescuento() { return descuento; }
    /** @param descuento porcentaje de descuento general */
    public void setDescuento(int descuento) { this.descuento = descuento; }

    /** @return porcentaje de descuento prémium */
    public int getDescuentoPremium() { return descuentoPremium; }
    /** @param descuentoPremium porcentaje de descuento prémium */
    public void setDescuentoPremium(int descuentoPremium) { this.descuentoPremium = descuentoPremium; }

    /** @return identificador del vendedor propietario */
    public String getIdVendedor() {
    return idVendedor;
    }

    /** @param idVendedor identificador del vendedor propietario */
    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }
}
