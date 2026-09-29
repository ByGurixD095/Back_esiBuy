package esi.grupo5.esiBuy.Model;

import java.util.ArrayList;
import java.util.List;


public abstract class Usuario {
    private String username;
    private String password;
    
    private List<Rol> roles = new ArrayList<>();

    protected Usuario(String username, String password) {
        this.username = username;
        this.password = password;
    }


    //------ SETTERS & GETERS --------
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<Rol> getRoles() { return roles; }
    public void setRoles(List<Rol> roles) { this.roles = roles; }


    //------ METHODS --------
    public boolean comprobarCredenciales(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    public void addRol(Rol rol) { roles.add(rol); }
}
