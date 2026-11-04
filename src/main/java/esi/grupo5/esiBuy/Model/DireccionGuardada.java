package esi.grupo5.esiBuy.Model;

import java.util.UUID;

/** Dirección reutilizable que pertenece a un cliente y se guarda en su cuenta. */
public class DireccionGuardada extends DireccionEnvioPedido {

    private String id = UUID.randomUUID().toString();

    public DireccionGuardada() {
    }

    public DireccionGuardada(
            String nombreCompleto,
            String direccion,
            String ciudad,
            String provincia,
            String codigoPostal,
            String pais,
            String telefono) {
        super(nombreCompleto, direccion, ciudad, provincia, codigoPostal, pais, telefono);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
