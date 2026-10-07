package esi.grupo5.esiBuy.Repository;

import esi.grupo5.esiBuy.Model.Producto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductoRepository extends MongoRepository<Producto, String> {
}