package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.NotBlank;

public class ListaDeseosDTO {
    
    @NotBlank(message = "El nombre de la lista no puede estar vacío")
    private String nombre;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
