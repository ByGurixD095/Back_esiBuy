package esi.grupo5.esiBuy.Dto;

import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import java.time.LocalDateTime;


public class UserPatchDTO {

    //Datos comunes
    private String nombre;
    private String apellidos;
    private String telefono;
    private String imagenPerfil;

    //Datos de cliente
    private String dni;
    private LocalDateTime fechaNacimiento;
    private TipoCliente tipoCliente; 

    //Datos de vendedor
    private String nombreComercial;
    private  String cifNif;
    private String categoriaPrincipalId;

    //Datos de administrador
    private String sede;

    //Getters y Setters
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

    public LocalDateTime getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDateTime fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

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
