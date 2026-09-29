package esi.grupo5.esiBuy.Model;

public class Producto {
    private String id;
    private String nombre;
    private float precio;
    private int stock;
    private boolean disponibilidad;
    private Categoria categoria;

    public Producto(String id, String nombre, float precio, int stock, Categoria categoria) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.disponibilidad = stock > 0;
        this.categoria = categoria;
    }

    //------ SETTERS & GETERS --------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public float getPrecio() { return precio; }
    public void setPrecio(float precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) {
        this.stock = stock;
        this.disponibilidad = stock > 0;
    }
    public boolean isDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(boolean disponibilidad) { this.disponibilidad = disponibilidad; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}
