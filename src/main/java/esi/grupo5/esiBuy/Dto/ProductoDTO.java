package esi.grupo5.esiBuy.Dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Datos de creación o actualización de un producto.
 *
 * @param nombre nombre visible del producto
 * @param referencia referencia comercial
 * @param precioCent precio expresado en céntimos
 * @param descripcion descripción opcional
 * @param categoriaId identificador de la categoría asociada al producto
 * @param urlImagen URL o referencia de la imagen
 * @param numStock unidades disponibles
 * @param descuento porcentaje de descuento general
 * @param descuentoPremium porcentaje de descuento para clientes prémium
 * @param activo estado de publicación; si se omite al crear, se crea activo
 * @param soloPremium indica si el producto solo está disponible para clientes prémium
 */
public record ProductoDTO(

        @NotBlank(message = "El nombre del producto no puede ser nulo ni vacío")
        String nombre,

        @NotBlank(message = "La referencia del producto no puede ser nula ni vacía")
        String referencia,

        @NotNull(message = "El precio es obligatorio")
        @PositiveOrZero(message = "El precio no puede ser negativo")
        Integer precioCent,

        @Pattern(regexp = "(?s).*\\S.*", message = "La descripción no puede estar vacía")
        String descripcion,

        @NotBlank(message = "El identificador de categoría no puede ser nulo ni vacío")
        String categoriaId,

        @Pattern(regexp = "(?s).*\\S.*", message = "La URL de la imagen no puede estar vacía")
        String urlImagen,

        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer numStock,

        @Min(0)
        @Max(100)
        Integer descuento,

        @Min(0)
        @Max(100)
        Integer descuentoPremium,

        Boolean activo,

        Boolean soloPremium
) {
    /** Constructor compatible con clientes que aún no envían el indicador prémium. */
    public ProductoDTO(String nombre, String referencia, Integer precioCent, String descripcion,
            String categoriaId, String urlImagen, Integer numStock, Integer descuento,
            Integer descuentoPremium, Boolean activo) {
        this(nombre, referencia, precioCent, descripcion, categoriaId, urlImagen, numStock,
                descuento, descuentoPremium, activo, false);
    }
}
