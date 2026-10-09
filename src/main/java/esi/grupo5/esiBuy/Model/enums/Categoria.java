package esi.grupo5.esiBuy.Model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Categoria {
    TECNOLOGIA("Tecnología"),
    MODA("Moda"),
    HOGAR_Y_COCINA("Hogar y Cocina"),
    DEPORTE_Y_OCIO("Deporte y Ocio"),
    LIBROS("Libros"),
    OTROS("Otros"),
    PAPELERIA_Y_MATERIAL_ESCOLAR("Papelería y Material Escolar"), 
    SALUD_Y_CUIDADO_PERSONAL("Salud y Cuidado Personal"),         
    ALIMENTACION_Y_BEBIDAS("Alimentación y Bebidas"),             
    JUGUETES_Y_HOBBIES("Juguetes y Hobbies"),                     
    ELECTRODOMESTICOS("Electrodomésticos"),                       
    BRICOLAJE_Y_HERRAMIENTAS("Bricolaje y Herramientas"),         
    MASCOTAS("Mascotas");                          
    

    private final String nombreVisible;

    Categoria(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    /**
     * Devuelve el texto amigable que verá el usuario en la interfaz y que se guardará en MongoDB.
     * La anotación @JsonValue asegura que al serializar (Java -> JSON), Jackson use el String visible (ej. "Hogar y Cocina").
     */
    @JsonValue
    public String getNombreVisible() {
        return nombreVisible;
    }

    /**
     * Mapea un String recibido desde la API/Frontend a un valor del Enum.
     * La anotación @JsonCreator asegura que Jackson convierta correctamente un JSON que contenga
     * tanto "TECNOLOGIA" como "Tecnología" al enum correspondiente.
     * Si no se encuentra, dispara la IllegalArgumentException controlada en la API.
     */
    @JsonCreator
    public static Categoria deString(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría no puede ser nula ni vacía");
        }
        for (Categoria cat : Categoria.values()) {
            if (cat.nombreVisible.equalsIgnoreCase(texto.trim()) || cat.name().equalsIgnoreCase(texto.trim())) {
                return cat;
            }
        }
        throw new IllegalArgumentException("La categoría '" + texto + "' no está permitida en la plataforma.");
    }
}