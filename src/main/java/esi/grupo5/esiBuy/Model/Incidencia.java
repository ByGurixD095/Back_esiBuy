package esi.grupo5.esiBuy.Model;

public class Incidencia {
    private String id;
    private String estado;

    public Incidencia(String id, String estado) {
        this.id = id;
        this.estado = estado;
    }

     //------ SETTERS & GETERS --------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
