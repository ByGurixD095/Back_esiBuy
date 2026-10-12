package esi.grupo5.esiBuy.Repository;

import esi.grupo5.esiBuy.Model.LoginAttemptState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/** Acceso persistente a estados de intentos de inicio de sesión. */
@Repository
public interface LoginAttemptStateRepository extends MongoRepository<LoginAttemptState, String> {
    /**
     * Busca el estado de intentos asociado a una dirección IP.
     *
     * @param ipAddress dirección IP que se consulta
     * @return estado encontrado, o vacío si no existe
     */
    Optional<LoginAttemptState> findByIpAddress(String ipAddress);
}