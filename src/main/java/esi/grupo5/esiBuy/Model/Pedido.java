package esi.grupo5.esiBuy.Model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private String id;
    private Instant fecha;
    private String estadoEnvio;

    private List<LineaPedido> lineas = new ArrayList<>();

    public Pedido(String id) {
        this.id = id;
        this.fecha = Instant.now();
        this.estadoEnvio = "PENDIENTE";
    }

    //------ SETTERS & GETERS --------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
    public String getEstadoEnvio() { return estadoEnvio; }
    public void setEstadoEnvio(String estadoEnvio) { this.estadoEnvio = estadoEnvio; }
    public List<LineaPedido> getLineas() { return lineas; }

    //------ METHODS --------
    public void cancelar() {
        this.estadoEnvio = "CANCELADO";
    }

    public String hacerSeguimiento() {
        return estadoEnvio;
    }

    public void anadirLinea(LineaPedido linea) { lineas.add(linea); }
}
