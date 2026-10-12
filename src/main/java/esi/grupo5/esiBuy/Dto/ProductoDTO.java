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
 * @param categoria categoría asociada al producto
 * @param urlImagen URL o referencia de la imagen
 * @param numStock unidades disponibles
 * @param descuento porcentaje de descuento general
 * @param descuentoPremium porcentaje de descuento para clientes prémium
 * @param activo estado de publicación; si se omite al crear, se crea activo
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

        @NotBlank(message = "La categoría no puede ser nula ni vacía")
        String categoria,

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

        Boolean activo
) {}