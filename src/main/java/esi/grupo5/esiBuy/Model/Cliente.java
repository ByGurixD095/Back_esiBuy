package esi.grupo5.esiBuy.Model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

import org.springframework.data.annotation.TypeAlias;

import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;

@TypeAlias("cliente")
public class Cliente extends Usuario {

    @NotBlank
    private String dni;

    @NotNull
    @Past
    private LocalDate fechaNacimiento;

    @NotNull
    private TipoCliente tipoCliente;

    public Cliente() {
        super();
        this.setRol(Rol.CLIENTE);
        this.tipoCliente = TipoCliente.NORMAL;
    }

    public Cliente(String nombre, String apellidos, String email, 
                   String contrasena, String dni, LocalDate fechaNacimiento) {
        super(nombre, apellidos, email, contrasena, null, null);
        this.setRol(Rol.CLIENTE);
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.tipoCliente = TipoCliente.NORMAL;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Cliente cliente = new Cliente();

        public Builder nombre(String nombre) { cliente.setNombre(nombre); return this; }
        public Builder apellidos(String apellidos) { cliente.setApellidos(apellidos); return this; }
        public Builder email(String email) { cliente.setEmail(email); return this; }
        public Builder contrasena(String contrasena) { cliente.setContrasena(contrasena); return this; }
        public Builder telefono(String telefono) { cliente.setTelefono(telefono); return this; }
        public Builder imagenPerfil(String imagenPerfil) { cliente.setImagenPerfil(imagenPerfil); return this; }
        public Builder dni(String dni) { cliente.setDni(dni); return this; }
        public Builder fechaNacimiento(LocalDate fechaNacimiento) { cliente.setFechaNacimiento(fechaNacimiento); return this; }
        public Builder tipoCliente(TipoCliente tipoCliente) { cliente.setTipoCliente(tipoCliente); return this; }

        public Cliente build() {
            return cliente;
        }
    }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public TipoCliente getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(TipoCliente tipoCliente) { this.tipoCliente = tipoCliente; }
}