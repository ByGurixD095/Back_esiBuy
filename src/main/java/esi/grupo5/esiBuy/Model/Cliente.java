package esi.grupo5.esiBuy.Model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

import org.springframework.data.annotation.TypeAlias;

import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;

/** Cuenta de usuario con los datos propios de un cliente. */
@TypeAlias("cliente")
public class Cliente extends Usuario {

    /** Documento nacional de identidad del cliente. */
    @NotBlank
    private String dni;

    /** Fecha de nacimiento del cliente. */
    @NotNull
    @Past
    private LocalDate fechaNacimiento;

    /** Tipo de cliente, que determina las condiciones asociadas a la cuenta. */
    @NotNull
    private TipoCliente tipoCliente;

    /** Crea un cliente con rol y tipo de cliente predeterminados. */
    public Cliente() {
        super(Rol.CLIENTE, false);
        this.tipoCliente = TipoCliente.NORMAL;
    }

    /**
     * Crea un cliente con sus datos principales.
     *
     * @param nombre nombre de la persona titular
     * @param apellidos apellidos de la persona titular
     * @param email dirección de correo
     * @param contrasena contraseña
     * @param dni documento nacional de identidad
     * @param fechaNacimiento fecha de nacimiento
     */
    public Cliente(String nombre, String apellidos, String email, 
                   String contrasena, String dni, LocalDate fechaNacimiento) {
        super(nombre, apellidos, email, contrasena, null, null, Rol.CLIENTE, false);
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.tipoCliente = TipoCliente.NORMAL;
    }

    /** @return builder para crear un cliente */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder para configurar una cuenta de cliente. */
    public static class Builder {
        private final Cliente cliente = new Cliente();

        /**
         * @param nombre nombre de la persona titular
         * @return este builder
         */
        public Builder nombre(String nombre) { cliente.setNombre(nombre); return this; }
        /**
         * @param apellidos apellidos de la persona titular
         * @return este builder
         */
        public Builder apellidos(String apellidos) { cliente.setApellidos(apellidos); return this; }
        /**
         * @param email dirección de correo
         * @return este builder
         */
        public Builder email(String email) { cliente.setEmail(email); return this; }
        /**
         * @param contrasena contraseña
         * @return este builder
         */
        public Builder contrasena(String contrasena) { cliente.setContrasena(contrasena); return this; }
        /**
         * @param telefono teléfono de contacto
         * @return este builder
         */
        public Builder telefono(String telefono) { cliente.setTelefono(telefono); return this; }
        /**
         * @param imagenPerfil URL o referencia de la imagen
         * @return este builder
         */
        public Builder imagenPerfil(String imagenPerfil) { cliente.setImagenPerfil(imagenPerfil); return this; }
        /**
         * @param dni documento nacional de identidad
         * @return este builder
         */
        public Builder dni(String dni) { cliente.setDni(dni); return this; }
        /**
         * @param fechaNacimiento fecha de nacimiento
         * @return este builder
         */
        public Builder fechaNacimiento(LocalDate fechaNacimiento) { cliente.setFechaNacimiento(fechaNacimiento); return this; }
        /**
         * @param tipoCliente tipo de cliente
         * @return este builder
         */
        public Builder tipoCliente(TipoCliente tipoCliente) { cliente.setTipoCliente(tipoCliente); return this; }

        /** @return cliente configurado */
        public Cliente build() {
            return cliente;
        }
    }

    /** @return DNI del cliente */
    public String getDni() { return dni; }
    /** @param dni DNI del cliente */
    public void setDni(String dni) { this.dni = dni; }

    /** @return fecha de nacimiento */
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    /** @param fechaNacimiento fecha de nacimiento */
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    /** @return tipo de cliente */
    public TipoCliente getTipoCliente() { return tipoCliente; }
    /** @param tipoCliente tipo de cliente */
    public void setTipoCliente(TipoCliente tipoCliente) { this.tipoCliente = tipoCliente; }
}