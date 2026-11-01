package esi.grupo5.esiBuy.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import esi.grupo5.esiBuy.Model.Pedido;

/** Acceso a los pedidos persistidos. */
public interface PedidoRepository extends MongoRepository<Pedido, String> {

    List<Pedido> findByClienteIdOrderByFechaPedidoDesc(String clienteId);

    Optional<Pedido> findByIdAndClienteId(String id, String clienteId);
}
