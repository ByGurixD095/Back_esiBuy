package esi.grupo5.esiBuy.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import esi.grupo5.esiBuy.Model.ListaDeseos;

public interface ListaDeseosRepository extends MongoRepository<ListaDeseos, String> {

}