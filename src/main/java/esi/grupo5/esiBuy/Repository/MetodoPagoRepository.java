
package esi.grupo5.esiBuy.Repository;

import esi.grupo5.esiBuy.Model.MetodoPago;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MetodoPagoRepository extends MongoRepository<MetodoPago, String> {

    List<MetodoPago> findByClienteId(String clienteId);
    Optional<MetodoPago> findByIdAndClienteId(String id, String clienteId);
}
