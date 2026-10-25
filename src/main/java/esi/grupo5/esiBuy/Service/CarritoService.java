package esi.grupo5.esiBuy.Service;

import java.util.List;
import java.util.Collections;

import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Repository.CarritoRepository;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;

    public CarritoService(CarritoRepository carritoRepository) {
        this.carritoRepository = carritoRepository;
    }

    public List<?> listarCarrito(String clienteId) {
        return carritoRepository.findByClienteId(clienteId).map(carrito -> Collections.emptyList()).orElse(Collections.emptyList());
    }


    
}
