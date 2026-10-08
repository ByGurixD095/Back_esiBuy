package esi.grupo5.esiBuy.Service.strategy;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.ZoneId;

import esi.grupo5.esiBuy.Dto.UserPatchDTO;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Usuario;

@Component
public class ClienteUpdateStrategy implements UsuarioUpdateStrategy {
    
    @Override
    public boolean supports(Usuario usuario) {
        return usuario instanceof Cliente;
    }

    @Override
    public void actualizar(Usuario usuario, UserPatchDTO dto) {
        Cliente cliente = (Cliente) usuario;
        
        if (dto.dni() != null) {
            cliente.setDni(dto.dni());
        }
        if (dto.tipoCliente() != null) {
            cliente.setTipoCliente(dto.tipoCliente());
        }
        if (dto.fechaNacimiento() != null) {
            if (dto.fechaNacimiento().isAfter(LocalDate.now(ZoneId.systemDefault()).minusYears(18))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cliente debe ser mayor de edad");
            }
            cliente.setFechaNacimiento(dto.fechaNacimiento());
        }
    }
}