package esi.grupo5.esiBuy.Service.strategy;

import esi.grupo5.esiBuy.Dto.UserPatchDTO;
import esi.grupo5.esiBuy.Model.Usuario;

/** Define cómo aplicar cambios específicos según el tipo concreto de usuario. */
public interface UsuarioUpdateStrategy {
    /**
     * Indica si la estrategia corresponde al usuario recibido.
     *
     * @param usuario usuario cuyo tipo se evalúa
     * @return {@code true} si esta estrategia puede actualizarlo
     */
    boolean supports(Usuario usuario);
    /**
     * Aplica al usuario los campos específicos presentes en el parche.
     *
     * @param usuario usuario que se modificará
     * @param dto cambios parciales solicitados
     */
    void actualizar(Usuario usuario, UserPatchDTO dto);
}