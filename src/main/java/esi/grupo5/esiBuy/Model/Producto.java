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

    public Producto() {
        this.activo = true; // Por defecto, el producto está activo
    }

    public Producto(String nombre, String referencia, int numStock, int precioCent, 
                    String descripcion, String categoria, String urlImagen) {
        this();
        this.nombre = nombre;
        this.referencia = referencia;
        this.numStock = numStock;
        this.precioCent = precioCent;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.urlImagen = urlImagen;
    }

}

