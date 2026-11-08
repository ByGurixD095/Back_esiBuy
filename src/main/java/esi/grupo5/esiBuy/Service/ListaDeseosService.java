package esi.grupo5.esiBuy.Service;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Model.ListaDeseos;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.ListaDeseosRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;
import esi.grupo5.esiBuy.Exception.NotFoundException;

@Service
public class ListaDeseosService {

    private static final String LISTA_NO_ENCONTRADA = "Lista de deseos no encontrada";

    private final ListaDeseosRepository listaDeseosRepository;
    private final ProductoRepository productoRepository;

    public ListaDeseosService(ListaDeseosRepository listaDeseosRepository, ProductoRepository productoRepository) {
        this.listaDeseosRepository = listaDeseosRepository;
        this.productoRepository = productoRepository;
    }

    public ListaDeseos crearLista(String nombre, String idCliente) {
        ListaDeseos lista = new ListaDeseos();
        lista.setNombre(nombre);
        lista.setIdCliente(idCliente);
        lista.setProductosIds(new HashSet<>());
        return listaDeseosRepository.save(lista);
    }

    public List<ListaDeseos> obtenerListas (String idCliente) {
        return listaDeseosRepository.findByIdCliente(idCliente);
    }

    public List<Producto> obtenerProductosLista (String idLista, String idCliente) {
        ListaDeseos lista = listaDeseosRepository.findByIdAndIdCliente(idLista, idCliente).orElseThrow(() -> new NotFoundException(LISTA_NO_ENCONTRADA));
        List<Producto> productos = new ArrayList<>();

        productoRepository.findAllById(lista.getProductosIds()).forEach(productos::add);

        return productos;
    }

    public ListaDeseos agregarProductoLista (String idLista, String idProducto, String idCliente) {
        ListaDeseos lista = listaDeseosRepository.findByIdAndIdCliente(idLista, idCliente).orElseThrow(() -> new NotFoundException (LISTA_NO_ENCONTRADA));

        productoRepository.findById(idProducto).orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        lista.getProductosIds().add(idProducto);

        return listaDeseosRepository.save(lista);
    }

    public ListaDeseos eliminarProductoLista(String idLista, String idProducto, String idCliente) {
        ListaDeseos lista = listaDeseosRepository.findByIdAndIdCliente(idLista, idCliente)
                .orElseThrow(() -> new NotFoundException(LISTA_NO_ENCONTRADA));

        if (!lista.getProductosIds().contains(idProducto)) {
            throw new NotFoundException("El producto no se encuentra en la lista de deseos");
        }

        lista.getProductosIds().remove(idProducto);
        return listaDeseosRepository.save(lista);
    }

    public ListaDeseos modificarLista(String idLista, String nuevoNombre, String idCliente) {

        ListaDeseos lista = listaDeseosRepository.findByIdAndIdCliente(idLista, idCliente)
                .orElseThrow(() -> new NotFoundException(LISTA_NO_ENCONTRADA));

        lista.setNombre(nuevoNombre);
        return listaDeseosRepository.save(lista);
      
    }
    public void eliminarLista(String idLista, String idCliente) {
        ListaDeseos lista = listaDeseosRepository.findByIdAndIdCliente(idLista, idCliente)
                .orElseThrow(() -> new NotFoundException(LISTA_NO_ENCONTRADA));
        listaDeseosRepository.delete(lista);
    }

}
