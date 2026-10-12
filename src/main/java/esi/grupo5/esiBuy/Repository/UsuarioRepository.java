package esi.grupo5.esiBuy.Repository;

import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.Rol;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Acceso persistente a usuarios y consultas por atributos de cuenta. */
@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    /**
     * Comprueba si ya existe una cuenta con el correo indicado.
     *
     * @param email correo que se consulta
     * @return {@code true} si existe
     */
    boolean existsByEmail(String email);
    /**
     * Comprueba si otro vendedor ya utiliza el nombre comercial indicado.
     *
     * @param nombreComercial nombre comercial que se consulta
     * @param id identificador del usuario que se excluye
     * @return {@code true} si otro vendedor utiliza el nombre
     */
    @Query(value = "{ 'nombreComercial': ?0, '_id': { '$ne': ?1 } }", exists = true)
    boolean existsByNombreComercialAndIdNot(String nombreComercial, String id);
    /**
     * Comprueba si un vendedor ya utiliza el nombre comercial indicado.
     *
     * @param nombreComercial nombre comercial que se consulta
     * @return {@code true} si existe
     */
    @Query(value = "{ 'nombreComercial': ?0 }", exists = true)
    boolean existsByNombreComercial(String nombreComercial);
    /**
     * Busca una cuenta por correo.
     *
     * @param email correo de la cuenta
     * @return usuario encontrado, o vacío si no existe
     */
    Optional<Usuario> findByEmail(String email);
    /** @return usuarios que no han sido eliminados lógicamente */
    List<Usuario> findAllByEliminadoFalse();
    /**
     * Busca una cuenta no eliminada por identificador.
     *
     * @param id identificador de usuario
     * @return usuario encontrado, o vacío si no existe o fue eliminado
     */
    Optional<Usuario> findByIdAndEliminadoFalse(String id);
    /**
     * Comprueba que una cuenta siga activa, desbloqueada y no eliminada.
     *
     * @param id identificador de la cuenta
     * @return {@code true} si la cuenta puede autenticarse
     */
    boolean existsByIdAndActivoTrueAndBloqueadoFalseAndEliminadoFalse(String id);
    /**
     * Cuenta las cuentas no bloqueadas, activas y no eliminadas con el rol indicado.
     *
     * @param rol rol de las cuentas que se contarán
     * @return número de cuentas que cumplen todos los criterios
     */
    long countByRolAndActivoTrueAndBloqueadoFalseAndEliminadoFalse(Rol rol);
    /**
     * Busca la cuenta asociada al hash de un token de recuperación.
     *
     * @param hashToken hash del token
     * @return usuario asociado, o vacío si no existe
     */
    Optional<Usuario> findByTokenRecuperacionContrasena(String hashToken);
}