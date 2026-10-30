package esi.grupo5.esiBuy.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import esi.grupo5.esiBuy.Model.ListaDeseos;

import java.util.List;

public interface ListaDeseosRepository extends MongoRepository<ListaDeseos, String> {

    List<ListaDeseos> findByIdCliente (String idCliente);
    
}