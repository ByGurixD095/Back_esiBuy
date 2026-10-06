package esi.grupo5.esiBuy.Dto;

import esi.grupo5.esiBuy.Model.enums.Rol;

public class UserDto {
    private String id;
    private String name;
    private String apellidos;
    private String email;
    private Rol rol;
    private boolean activo;
    private boolean bloqueado;

    public UserDto() {
    }

    public UserDto(String id, String name, String apellidos, String email, Rol rol, boolean activo, boolean bloqueado) {
        this.id = id;
        this.name = name;
        this.apellidos = apellidos;
        this.email = email;
        this.rol = rol;
        this.activo = activo;
        this.bloqueado = bloqueado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

}