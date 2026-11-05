package esi.grupo5.esiBuy.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import esi.grupo5.esiBuy.Model.Valoracion;

/** Acceso a las valoraciones de productos. */
public interface ValoracionRepository extends MongoRepository<Valoracion, String> {

    List<Valoracion> findByProductoIdOrderByFechaDesc(String productoId);

    boolean existsByProductoIdAndUsuarioId(String productoId, String usuarioId);
}
