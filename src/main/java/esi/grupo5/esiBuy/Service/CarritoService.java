package esi.grupo5.esiBuy.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Repository.CarritoRepository;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;

    public CarritoService(CarritoRepository carritoRepository) {
        this.carritoRepository = carritoRepository;
    }

    public List<?> listarCarrito(String clienteId) {
        return null; //Temporal para provocar el fallo en el test, luego se implementa correctamente
    }


    
}
