package esi.grupo5.esiBuy.Service.strategy;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.util.function.Consumer;

import esi.grupo5.esiBuy.Dto.UserPatchDTO;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.Vendedor;

@Component
public class VendedorUpdateStrategy implements UsuarioUpdateStrategy {

    @Override
    public boolean supports(Usuario usuario) {
        return usuario instanceof Vendedor;
    }

    @Override
    public void actualizar(Usuario usuario, UserPatchDTO dto) {
        Vendedor vendedor = (Vendedor) usuario;
        
        actualizarTexto(dto.nombreComercial(), vendedor::setNombreComercial, "El nombre comercial no puede estar vacío");
        actualizarTexto(dto.cifNif(), vendedor::setCifNif, "El CIF/NIF no puede estar vacío");
        actualizarTexto(dto.categoriaPrincipalId(), vendedor::setCategoriaPrincipalId, "La categoría principal no puede estar vacía");
    }

    private void actualizarTexto(String valor, Consumer<String> setter, String mensajeError) {
        if (valor != null) {
            if (valor.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensajeError);
            }
            setter.accept(valor);
        }
    }
}