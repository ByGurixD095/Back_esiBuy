package esi.grupo5.esiBuy.Repository;

import esi.grupo5.esiBuy.Model.Producto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Acceso persistente a productos y consultas derivadas del catálogo. */
public interface ProductoRepository extends MongoRepository<Producto, String> {

    /**
     * Busca productos activos con existencias superiores al mínimo indicado.
     *
     * @param minStock umbral de existencias que el producto debe superar
     * @return productos coincidentes
     */
    List<Producto> findByActivoTrueAndNumStockGreaterThan(int minStock);
    /**
     * Recupera una página de productos pertenecientes a un vendedor.
     *
     * @param idVendedor identificador del vendedor
     * @param pageable configuración de página y orden
     * @return página de productos del vendedor
     */
    Page<Producto> findByIdVendedor(String idVendedor, Pageable pageable);
    /**
     * Busca un producto solo si pertenece al vendedor indicado.
     *
     * @param id identificador del producto
     * @param idVendedor identificador del propietario
     * @return producto coincidente, o vacío si no existe o no pertenece al vendedor
     */
    Optional<Producto> findByIdAndIdVendedor(String id, String idVendedor);

    /**
     * Indica si algún producto usa la categoría indicada (los productos guardan el nombre).
     *
     * @param categoria nombre de la categoría
     * @return true si hay al menos un producto con esa categoría
     */
    boolean existsByCategoria(String categoria);

}