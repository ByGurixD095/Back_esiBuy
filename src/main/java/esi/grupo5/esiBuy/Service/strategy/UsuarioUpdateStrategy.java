package esi.grupo5.esiBuy.Service.strategy;

import esi.grupo5.esiBuy.Dto.UserPatchDTO;
import esi.grupo5.esiBuy.Model.Usuario;

public interface UsuarioUpdateStrategy {
    boolean supports(Usuario usuario);
    void actualizar(Usuario usuario, UserPatchDTO dto);
}