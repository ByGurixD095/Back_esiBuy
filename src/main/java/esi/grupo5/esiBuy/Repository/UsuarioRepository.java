package esi.grupo5.esiBuy.Repository;

import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.Rol;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    boolean existsByEmail(String email);
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findAllByEliminadoFalse();
    Optional<Usuario> findByIdAndEliminadoFalse(String id);
    long countByRolAndEliminadoFalse(Rol rol);
    Optional<Usuario> findByTokenRecuperacionContrasena(String hashToken);
}