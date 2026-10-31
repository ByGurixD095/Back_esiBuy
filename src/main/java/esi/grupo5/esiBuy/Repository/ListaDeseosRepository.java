package esi.grupo5.esiBuy.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import esi.grupo5.esiBuy.Model.ListaDeseos;

import java.util.List;
import java.util.Optional;

public interface ListaDeseosRepository extends MongoRepository<ListaDeseos, String> {

    List<ListaDeseos> findByIdCliente (String idCliente);
    Optional<ListaDeseos> findByIdAndIdCliente (String id, String idCliente);
    
}