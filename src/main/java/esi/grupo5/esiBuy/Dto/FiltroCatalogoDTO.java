package esi.grupo5.esiBuy.Dto;

/*
 * Se usa 'record' porque genera automáticamente un objeto inmutable con constructores y getters, 
 * ideal para recibir datos de lectura de una petición HTTP[cite: 2].
 * 
 * Agrupa todos los parámetros de la URL para la búsqueda dinámica con MongoTemplate.
 */
public record FiltroCatalogoDTO(
    
    // Captura el texto de la barra "Buscar por nombre o referencia".
    // Si el usuario no escribe nada, llegará como null o "".
    String busqueda,
    
    // Captura el valor del desplegable de categorías (ej. "Tecnología" o "Todas").
    String categoria,
    
    // Límite inferior del slider de precios.
    // IMPORTANTE ARQUITECTURA: Usamos Integer (clase) y no int (primitivo) para que pueda ser 'null' 
    // si el usuario no usa el filtro. Si usáramos 'int', Java pondría un 0 por defecto y rompería la búsqueda.
    // Además, se maneja estrictamente en céntimos para evitar los problemas de rendimiento de BigDecimal
    Integer precioMinCent,
    
    // Límite superior del slider de precios (en céntimos). También Integer para permitir que sea opcional.
    Integer precioMaxCent,
    
    // Checkbox de "Solo ofertas". 
    // Usamos Boolean en vez de boolean para que, si el frontend no envía el parámetro, sea 'null' en vez de 'false'.
    Boolean soloOfertas,
    
    // Captura el desplegable "Ordenar por" (ej. "Relevancia", "Precio Ascendente").
    String orden

) {}