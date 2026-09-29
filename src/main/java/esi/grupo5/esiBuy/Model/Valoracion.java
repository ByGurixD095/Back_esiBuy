package esi.grupo5.esiBuy.Model;

public class Valoracion {
    private int puntuacion;
    private String comentario;
    private Producto producto;

    public Valoracion(int puntuacion, String comentario, Producto producto) {
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.producto = producto;
    }

    //------ SETTERS & GETERS --------
    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
}
