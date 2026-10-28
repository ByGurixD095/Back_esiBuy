package esi.grupo5.esiBuy.Repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import esi.grupo5.esiBuy.Model.Carrito;

public interface CarritoRepository extends MongoRepository<Carrito, String> {

    Optional<Carrito> findByClienteId(String clienteId);
    
}
