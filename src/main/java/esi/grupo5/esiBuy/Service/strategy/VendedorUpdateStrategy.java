package esi.grupo5.esiBuy.Service.strategy;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.util.function.Consumer;

import esi.grupo5.esiBuy.Dto.UserPatchDTO;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.Vendedor;

/** Actualiza los campos específicos del perfil de vendedor. */
@Component
public class VendedorUpdateStrategy implements UsuarioUpdateStrategy {

    /** {@inheritDoc} */
    @Override
    public boolean supports(Usuario usuario) {
        return usuario instanceof Vendedor;
    }

    /** {@inheritDoc} */
    @Override
    public void actualizar(Usuario usuario, UserPatchDTO dto) {
        Vendedor vendedor = (Vendedor) usuario;
        
        actualizarTexto(dto.nombreComercial(), vendedor::setNombreComercial, "El nombre comercial no puede estar vacío");
        actualizarTexto(dto.cifNif(), vendedor::setCifNif, "El CIF/NIF no puede estar vacío");
        actualizarTexto(dto.categoriaPrincipalId(), vendedor::setCategoriaPrincipalId, "La categoría principal no puede estar vacía");
    }

    /**
     * Actualiza el texto cuando está presente y rechaza valores en blanco.
     *
     * @param valor texto opcional recibido
     * @param setter operación que asigna el texto
     * @param mensajeError mensaje asociado a un valor vacío
     * @throws ResponseStatusException si el valor no es nulo pero está en blanco
     */
    private void actualizarTexto(String valor, Consumer<String> setter, String mensajeError) {
        if (valor != null) {
            if (valor.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensajeError);
            }
            setter.accept(valor);
        }
    }
}