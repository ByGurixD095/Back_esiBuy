package esi.grupo5.esiBuy.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import esi.grupo5.esiBuy.Model.Categoria;

/** Acceso persistente a las categorías. */
@Repository
public interface CategoriaRepository extends MongoRepository<Categoria, String> {

    boolean existsByNombreRegexIgnoreCase(String nombreRegex);

    boolean existsByIdNotAndNombreRegexIgnoreCase(String id, String nombreRegex);
}
