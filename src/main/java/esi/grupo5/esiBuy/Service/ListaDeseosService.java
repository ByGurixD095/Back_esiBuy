package esi.grupo5.esiBuy.Service;

import java.util.HashSet;

import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Model.ListaDeseos;
import esi.grupo5.esiBuy.Repository.ListaDeseosRepository;

@Service
public class ListaDeseosService {

    private final ListaDeseosRepository listaDeseosRepository;

    public ListaDeseosService(ListaDeseosRepository listaDeseosRepository) {
        this.listaDeseosRepository = listaDeseosRepository;
    }

    public ListaDeseos crearLista(String nombre, String idCliente) {
        ListaDeseos lista = new ListaDeseos();
        lista.setNombre(nombre);
        lista.setIdCliente(idCliente);
        lista.setProductosIds(new HashSet<>());
        return listaDeseosRepository.save(lista);
    }
}
