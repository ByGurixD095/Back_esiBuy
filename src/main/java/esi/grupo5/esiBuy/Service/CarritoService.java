package esi.grupo5.esiBuy.Service;

import java.util.List;
import java.util.Collections;

import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Model.ItemCarrito;
import esi.grupo5.esiBuy.Model.Carrito;
import esi.grupo5.esiBuy.Repository.CarritoRepository;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;

    public CarritoService(CarritoRepository carritoRepository) {
        this.carritoRepository = carritoRepository;
    }

    public List<ItemCarrito> listarCarrito(String clienteId) {
        return carritoRepository.findByClienteId(clienteId).map(carrito -> Collections.<ItemCarrito>emptyList()).orElse(Collections.emptyList());
}


    
}
