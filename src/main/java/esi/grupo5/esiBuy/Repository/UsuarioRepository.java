package esi.grupo5.esiBuy.Repository;

import esi.grupo5.esiBuy.Model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    // Spring Data resuelve polimórficamente las clases hijas guardadas en "usuarios"
    Optional<Usuario> findByEmail(String username);
}