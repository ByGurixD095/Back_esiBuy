package esi.grupo5.esiBuy.Repository;

import esi.grupo5.esiBuy.Model.LoginAttemptState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LoginAttemptStateRepository extends MongoRepository<LoginAttemptState, String> {
    Optional<LoginAttemptState> findByIpAddress(String ipAddress);
}