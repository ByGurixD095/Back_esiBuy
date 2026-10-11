package esi.grupo5.esiBuy.Repository;

import esi.grupo5.esiBuy.Model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/** Acceso persistente a los tokens de refresco de sesión. */
@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    /**
     * Busca el registro que contiene el token indicado.
     *
     * @param token JWT de refresco
     * @return registro encontrado, o vacío si no existe
     */
    Optional<RefreshToken> findByToken(String token);
    /**
     * Elimina el registro que contiene el token indicado.
     *
     * @param token JWT de refresco que se eliminará
     */
    void deleteByToken(String token);
}