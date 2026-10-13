package esi.grupo5.esiBuy.Model;


import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.data.annotation.TypeAlias;

import esi.grupo5.esiBuy.Model.enums.Rol;

/** Cuenta de usuario con permisos de administración de la plataforma. */
@TypeAlias("administrador")
public class Administrador extends Usuario {

    /** Sede a la que está asignado el administrador. */
    private String sede;

    /** Fecha en que el administrador se incorporó. */
    @NotNull
    private LocalDate fechaIncorporacion;

    /** Crea un administrador con rol y fecha de incorporación predeterminados. */
    public Administrador() {
        super(Rol.ADMINISTRADOR, true);
        this.fechaIncorporacion = LocalDate.now(ZoneId.systemDefault());
    }

    /**
     * Crea un administrador con sus datos principales.
     *
     * @param nombre nombre de la persona titular
     * @param apellidos apellidos de la persona titular
     * @param email dirección de correo
     * @param contrasena contraseña
     * @param sede sede de trabajo
     */
    public Administrador(String nombre, String apellidos, String email, 
                         String contrasena, String sede) {
        super(nombre, apellidos, email, contrasena, null, null, Rol.ADMINISTRADOR, true);
        this.sede = sede;
        this.fechaIncorporacion = LocalDate.now(ZoneId.systemDefault());
    }

    /** @return builder para crear un administrador */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder para configurar una cuenta de administrador. */
    public static class Builder {
        private final Administrador admin = new Administrador();

        /**
         * @param nombre nombre de la persona titular
         * @return este builder
         */
        public Builder nombre(String nombre) { admin.setNombre(nombre); return this; }
        /**
         * @param apellidos apellidos de la persona titular
         * @return este builder
         */
        public Builder apellidos(String apellidos) { admin.setApellidos(apellidos); return this; }
        /**
         * @param email dirección de correo
         * @return este builder
         */
        public Builder email(String email) { admin.setEmail(email); return this; }
        /**
         * @param contrasena contraseña
         * @return este builder
         */
        public Builder contrasena(String contrasena) { admin.setContrasena(contrasena); return this; }
        /**
         * @param telefono teléfono de contacto
         * @return este builder
         */
        public Builder telefono(String telefono) { admin.setTelefono(telefono); return this; }
        /**
         * @param imagenPerfil URL o referencia de la imagen
         * @return este builder
         */
        public Builder imagenPerfil(String imagenPerfil) { admin.setImagenPerfil(imagenPerfil); return this; }
        /**
         * @param sede sede de trabajo
         * @return este builder
         */
        public Builder sede(String sede) { admin.setSede(sede); return this; }

        /** @return administrador configurado */
        public Administrador build() {
            return admin;
        }
    }

    /** @return sede asignada */
    public String getSede() { return sede; }
    /** @param sede sede asignada */
    public void setSede(String sede) { this.sede = sede; }

    /** @return fecha de incorporación */
    public LocalDate getFechaIncorporacion() { return fechaIncorporacion; }
}