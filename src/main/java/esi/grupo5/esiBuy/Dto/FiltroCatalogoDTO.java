package esi.grupo5.esiBuy.Dto;

/**
 * Filtros opcionales para buscar, acotar y ordenar el catálogo público de productos.
 *
 * @param busqueda texto buscado en el nombre o referencia
 * @param categoria categoría solicitada; el valor {@code Todas} no aplica un filtro
 * @param precioMinCent límite inferior del precio en céntimos
 * @param precioMaxCent límite superior del precio en céntimos
 * @param soloOfertas cuando es {@code true}, limita los resultados a productos con descuento
 * @param orden orden de precio reconocido por el catálogo
 */
public record FiltroCatalogoDTO(
    
    String busqueda,
    
    String categoria,
    
    Integer precioMinCent,
    
    Integer precioMaxCent,
    
    Boolean soloOfertas,
    
    String orden

) {}