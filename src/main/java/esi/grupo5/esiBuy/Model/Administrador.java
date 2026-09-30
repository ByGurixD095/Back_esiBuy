package esi.grupo5.esiBuy.Model;

import esi.grupo5.esiBuy.Model.enums.Rol;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.ZoneId;

public class Administrador extends Usuario {

    private String sede;

    @NotNull
    private LocalDate fechaIncorporacion;

    public Administrador() {
        super();
        this.setRol(Rol.ADMINISTRADOR);
        this.setActivo(true);
        this.fechaIncorporacion = LocalDate.now(ZoneId.systemDefault());
    }

    public Administrador(String nombre, String apellidos, String email, 
                         String contrasena, String sede) {
        super(nombre, apellidos, email, contrasena, null, null);
        this.setRol(Rol.ADMINISTRADOR);
        this.setActivo(true);
        this.sede = sede;
        this.fechaIncorporacion = LocalDate.now(ZoneId.systemDefault());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Administrador admin = new Administrador();

        public Builder nombre(String nombre) { admin.setNombre(nombre); return this; }
        public Builder apellidos(String apellidos) { admin.setApellidos(apellidos); return this; }
        public Builder email(String email) { admin.setEmail(email); return this; }
        public Builder contrasena(String contrasena) { admin.setContrasena(contrasena); return this; }
        public Builder telefono(String telefono) { admin.setTelefono(telefono); return this; }
        public Builder imagenPerfil(String imagenPerfil) { admin.setImagenPerfil(imagenPerfil); return this; }
        public Builder sede(String sede) { admin.setSede(sede); return this; }

        public Administrador build() {
            return admin;
        }
    }

    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }

    public LocalDate getFechaIncorporacion() { return fechaIncorporacion; }
    public void setFechaIncorporacion(LocalDate fechaIncorporacion) { this.fechaIncorporacion = fechaIncorporacion; }
}