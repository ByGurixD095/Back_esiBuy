package esi.grupo5.esiBuy.Model;

import java.util.ArrayList;
import java.util.List;

public class Administrador extends Usuario {
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Incidencia> incidencias = new ArrayList<>();
    private List<Categoria> categorias = new ArrayList<>();

    public Administrador(String username, String password) {
        super(username, password);
    }

    //------ SETTERS & GETERS --------
    public List<Usuario> getUsuarios() { return usuarios; }
    public List<Incidencia> getIncidencias() { return incidencias; }
    public List<Categoria> getCategorias() { return categorias; }

    //------ METHODS --------
    public void gestionarUsuarios() { /* TODO */ }
    public void gestionarVendedores() { /* TODO */ }
    public void gestionarCategorias() { /* TODO */ }
    public void gestionarIncidencias() { /* TODO */ }
}
