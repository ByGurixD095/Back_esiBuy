package esi.grupo5.esiBuy.Model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Categorías admitidas para clasificar productos en el catálogo. */
public enum Categoria {
    /** Tecnología y dispositivos electrónicos. */
    TECNOLOGIA("Tecnología"),
    /** Ropa, calzado y complementos. */
    MODA("Moda"),
    /** Productos para el hogar y la cocina. */
    HOGAR_Y_COCINA("Hogar y Cocina"),
    /** Productos deportivos y de ocio. */
    DEPORTE_Y_OCIO("Deporte y Ocio"),
    /** Libros y publicaciones. */
    LIBROS("Libros"),
    /** Productos que no encajan en otra categoría. */
    OTROS("Otros"),
    /** Papelería y material escolar. */
    PAPELERIA_Y_MATERIAL_ESCOLAR("Papelería y Material Escolar"), 
    /** Productos de salud y cuidado personal. */
    SALUD_Y_CUIDADO_PERSONAL("Salud y Cuidado Personal"),         
    /** Alimentos y bebidas. */
    ALIMENTACION_Y_BEBIDAS("Alimentación y Bebidas"),             
    /** Juguetes y artículos para aficiones. */
    JUGUETES_Y_HOBBIES("Juguetes y Hobbies"),                     
    /** Electrodomésticos para el hogar. */
    ELECTRODOMESTICOS("Electrodomésticos"),                       
    /** Herramientas y productos de bricolaje. */
    BRICOLAJE_Y_HERRAMIENTAS("Bricolaje y Herramientas"),         
    /** Productos para mascotas. */
    MASCOTAS("Mascotas");                          
    

    /** Nombre localizado que se expone al serializar la categoría. */
    private final String nombreVisible;

    /** @param nombreVisible etiqueta legible de la categoría */
    Categoria(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    /** @return etiqueta visible usada en la interfaz y en la serialización JSON */
    @JsonValue
    public String getNombreVisible() {
        return nombreVisible;
    }

    /**
     * Busca una categoría por su nombre técnico o por su etiqueta visible, sin distinguir mayúsculas.
     *
     * @param texto nombre técnico o etiqueta visible recibida
     * @return categoría correspondiente al texto
     * @throws IllegalArgumentException si el texto es nulo, vacío o no identifica una categoría
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