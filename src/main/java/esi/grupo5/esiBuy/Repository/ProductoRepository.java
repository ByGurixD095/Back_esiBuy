package esi.grupo5.esiBuy.Repository;

import esi.grupo5.esiBuy.Model.Producto;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductoRepository extends MongoRepository<Producto, String> {

    // Consulta global: INVIABLE SI SON MILES DE PRODUCTOS
    List<Producto> findByActivoTrueAndNumStockGreaterThan(int minStock);

    // Filtro estricto por categoría
    List<Producto> findByActivoTrueAndNumStockGreaterThanAndCategoria();

    
    List<Producto> findByActivoTrueAndNumStockGreatherThanAndNombreContainingIgnoreCase(int minStock, String nombre);
}