package esi.grupo5.esiBuy.Model;

import jakarta.validation.constraints.NotBlank;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;

import esi.grupo5.esiBuy.Model.enums.Rol;

/** Cuenta de usuario con identidad comercial y datos de vendedor. */
@TypeAlias("vendedor")
public class Vendedor extends Usuario {

    /** Nombre comercial único del vendedor. */
    @NotBlank
    @Indexed(unique = true)
    private String nombreComercial;

    /** CIF o NIF que identifica fiscalmente al vendedor. */
    @NotBlank
    private String cifNif;

    /** Identificador de la categoría principal del vendedor. */
    @NotBlank
    private String categoriaPrincipalId;

    /** Crea un vendedor con el rol correspondiente. */
    public Vendedor() {
        super();
        this.setRol(Rol.VENDEDOR);
    }

    /**
     * Crea un vendedor con sus datos comerciales principales.
     *
     * @param nombre nombre de la persona titular
     * @param apellidos apellidos de la persona titular
     * @param email dirección de correo
     * @param contrasena contraseña
     * @param nombreComercial nombre comercial
     * @param cifNif CIF o NIF
     */
    public Vendedor(String nombre, String apellidos, String email, 
                    String contrasena, String nombreComercial, String cifNif) {
        super(nombre, apellidos, email, contrasena, null, null);
        this.setRol(Rol.VENDEDOR);
        this.nombreComercial = nombreComercial;
        this.cifNif = cifNif;
    }

    /** @return builder para crear un vendedor */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder para configurar una cuenta de vendedor. */
    public static class Builder {
        private final Vendedor vendedor = new Vendedor();

        /**
         * @param nombre nombre de la persona titular
         * @return este builder
         */
        public Builder nombre(String nombre) { vendedor.setNombre(nombre); return this; }
        /**
         * @param apellidos apellidos de la persona titular
         * @return este builder
         */
        public Builder apellidos(String apellidos) { vendedor.setApellidos(apellidos); return this; }
        /**
         * @param email dirección de correo
         * @return este builder
         */
        public Builder email(String email) { vendedor.setEmail(email); return this; }
        /**
         * @param contrasena contraseña
         * @return este builder
         */
        public Builder contrasena(String contrasena) { vendedor.setContrasena(contrasena); return this; }
        /**
         * @param telefono teléfono de contacto
         * @return este builder
         */
        public Builder telefono(String telefono) { vendedor.setTelefono(telefono); return this; }
        /**
         * @param imagenPerfil URL o referencia de la imagen
         * @return este builder
         */
        public Builder imagenPerfil(String imagenPerfil) { vendedor.setImagenPerfil(imagenPerfil); return this; }
        /**
         * @param nombreComercial nombre comercial
         * @return este builder
         */
        public Builder nombreComercial(String nombreComercial) { vendedor.setNombreComercial(nombreComercial); return this; }
        /**
         * @param cifNif CIF o NIF
         * @return este builder
         */
        public Builder cifNif(String cifNif) { vendedor.setCifNif(cifNif); return this; }
        /**
         * @param categoriaPrincipalId identificador de la categoría principal
         * @return este builder
         */
        public Builder categoriaPrincipalId(String categoriaPrincipalId) { 
            vendedor.setCategoriaPrincipalId(categoriaPrincipalId); 
            return this; 
        }

        /** @return vendedor configurado */
        public Vendedor build() {
            return vendedor;
        }
    }

    /** @return nombre comercial */
    public String getNombreComercial() { return nombreComercial; }
    /** @param nombreComercial nombre comercial */
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    /** @return CIF o NIF */
    public String getCifNif() { return cifNif; }
    /** @param cifNif CIF o NIF */
    public void setCifNif(String cifNif) { this.cifNif = cifNif; }

    /** @return identificador de la categoría principal */
    public String getCategoriaPrincipalId() { return categoriaPrincipalId; }
    /** @param categoriaPrincipalId identificador de la categoría principal */
    public void setCategoriaPrincipalId(String categoriaPrincipalId) { this.categoriaPrincipalId = categoriaPrincipalId; }
}