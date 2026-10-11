package esi.grupo5.esiBuy.Service.strategy;

import org.springframework.stereotype.Component;

import esi.grupo5.esiBuy.Dto.UserPatchDTO;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Usuario;

/** Actualiza los campos propios del perfil de administrador. */
@Component
public class AdministradorUpdateStrategy implements UsuarioUpdateStrategy {

    /** {@inheritDoc} */
    @Override
    public boolean supports(Usuario usuario) {
        return usuario instanceof Administrador;
    }

    /** {@inheritDoc} */
    @Override
    public void actualizar(Usuario usuario, UserPatchDTO dto) {
        Administrador administrador = (Administrador) usuario;
        if (dto.sede() != null) {
            administrador.setSede(dto.sede());
        }
    }
}