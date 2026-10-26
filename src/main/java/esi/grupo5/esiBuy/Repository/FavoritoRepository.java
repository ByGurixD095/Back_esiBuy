package esi.grupo5.esiBuy.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import esi.grupo5.esiBuy.Model.Favorito;

/** Acceso persistente a los productos favoritos de los clientes. */
@Repository
public interface FavoritoRepository extends MongoRepository<Favorito, String> {

    /**
     * Busca el favorito de un cliente para un producto, esté o no vigente.
     *
     * @param clienteId identificador del cliente
     * @param productoId identificador del producto
     * @return favorito encontrado, o vacío si nunca se marcó
     */
    Optional<Favorito> findByClienteIdAndProductoId(String clienteId, String productoId);

    /**
     * Lista los favoritos vigentes de un cliente.
     *
     * @param clienteId identificador del cliente
     * @return favoritos activos del cliente
     */
    List<Favorito> findByClienteIdAndActivoTrue(String clienteId);
}