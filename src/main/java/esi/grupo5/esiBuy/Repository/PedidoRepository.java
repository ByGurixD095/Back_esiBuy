package esi.grupo5.esiBuy.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.Collection;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import esi.grupo5.esiBuy.Model.Pedido;
import esi.grupo5.esiBuy.Model.enums.EstadoPedido;

/** Acceso a los pedidos persistidos. */
public interface PedidoRepository extends MongoRepository<Pedido, String> {

    List<Pedido> findByClienteIdOrderByFechaPedidoDesc(String clienteId);

    Optional<Pedido> findByIdAndClienteId(String id, String clienteId);

    List<Pedido> findByEstadoAndFechaExpiracionLessThanEqual(EstadoPedido estado, LocalDateTime fechaExpiracion);

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByLineas_IdVendedorAndEstadoIn(String idVendedor, Collection<EstadoPedido> estados);

    boolean existsByLineas_IdVendedorAndEstadoIn(String idVendedor, Collection<EstadoPedido> estados);

    boolean existsByLineas_ProductoId(String productoId);

    @Query("{ 'clienteId': ?0 }")
    List<Pedido> findAllByClienteId(String clienteId);
}
