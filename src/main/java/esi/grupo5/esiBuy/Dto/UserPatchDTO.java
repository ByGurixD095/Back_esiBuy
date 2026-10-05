package esi.grupo5.esiBuy.Dto;

import java.time.LocalDate;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Past;


public class UserPatchDTO {

    private String nombre;
    private String apellidos;

    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos")
    private String telefono;
    private String imagenPerfil;

    //Cliente
    private String dni;
    
    @Past
    private LocalDate fechaNacimiento;
    private TipoCliente tipoCliente;

    //Vendedor
    private String nombreComercial;
    private  String cifNif;
    private  String categoriaPrincipalId;

    //Administrador
    private String sede;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getImagenPerfil() { return imagenPerfil; }
    public void setImagenPerfil(String imagenPerfil) { this.imagenPerfil = imagenPerfil; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public TipoCliente getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(TipoCliente tipoCliente) { this.tipoCliente = tipoCliente; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }
    public String getCifNif() { return cifNif; }
    public void setCifNif(String cifNif) { this.cifNif = cifNif; }
    public String getCategoriaPrincipalId() { return categoriaPrincipalId; }
    public void setCategoriaPrincipalId(String categoriaPrincipalId) { this.categoriaPrincipalId = categoriaPrincipalId; }

    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }

    

}
