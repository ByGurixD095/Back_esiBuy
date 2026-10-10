package esi.grupo5.esiBuy.Repository;

import esi.grupo5.esiBuy.Model.Producto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductoRepository extends MongoRepository<Producto, String> {

    // Consulta global: INVIABLE SI SON MILES DE PRODUCTOS
    List<Producto> findByActivoTrueAndNumStockGreaterThan(int minStock);
    Page<Producto> findByIdVendedor(String idVendedor, Pageable pageable);

    
}