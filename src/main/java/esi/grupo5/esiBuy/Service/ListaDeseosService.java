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
        ListaDeseos lista = listaDeseosRepository.findByIdAndIdCliente(idLista, idCliente).orElseThrow(() -> new NotFoundException("Lista de deseos no encontrada"));
        List<Producto> productos = new ArrayList<>();

        productoRepository.findAllById(lista.getProductosIds()).forEach(productos::add);

        return productos;
    }
}
