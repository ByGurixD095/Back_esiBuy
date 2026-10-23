package esi.grupo5.esiBuy.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.Favorito;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.FavoritoRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

/** Gestiona los productos favoritos de los clientes. */
@Service
public class FavoritoService {

    /** Mensaje común cuando el producto no existe o no está publicado. */
    private static final String PRODUCTO_NO_ENCONTRADO = "Producto no encontrado";

    /** Persistencia de favoritos. */
    private final FavoritoRepository favoritoRepository;
    /** Persistencia de productos, usada para validar y recuperar los favoritos. */
    private final ProductoRepository productoRepository;

    /**
     * Crea el servicio de favoritos.
     *
     * @param favoritoRepository repositorio de favoritos
     * @param productoRepository repositorio de productos
     */
    public FavoritoService(FavoritoRepository favoritoRepository, ProductoRepository productoRepository) {
        this.favoritoRepository = favoritoRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Marca un producto como favorito del cliente. Es idempotente: si ya estaba marcado no se
     * duplica y, si lo había quitado antes, se reactiva.
     *
     * @param clienteId identificador del cliente autenticado
     * @param productoId identificador del producto
     * @throws NotFoundException si el producto no existe o no está activo
     */
    public void marcarFavorito(String clienteId, String productoId) {
        productoRepository.findById(productoId)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new NotFoundException(PRODUCTO_NO_ENCONTRADO));

        Optional<Favorito> existente = favoritoRepository.findByClienteIdAndProductoId(clienteId, productoId);
        if (existente.isPresent()) {
            Favorito favorito = existente.get();
            if (!favorito.isActivo()) {
                favorito.setActivo(true);
                favoritoRepository.save(favorito);
            }
            return;
        }

        try {
            favoritoRepository.save(new Favorito(clienteId, productoId));
        } catch (DuplicateKeyException e) {
            // Otra petición simultánea ya lo marcó: el resultado es el mismo.
        }
    }

    /**
     * Quita un producto de los favoritos del cliente con borrado lógico. Es idempotente.
     *
     * @param clienteId identificador del cliente autenticado
     * @param productoId identificador del producto
     */
    public void quitarFavorito(String clienteId, String productoId) {
        favoritoRepository.findByClienteIdAndProductoId(clienteId, productoId)
                .filter(Favorito::isActivo)
                .ifPresent(favorito -> {
                    favorito.setActivo(false);
                    favoritoRepository.save(favorito);
                });
    }

    /**
     * Lista los productos favoritos vigentes del cliente, del más reciente al más antiguo.
     * Se omiten los productos que ya no existen o no están activos.
     *
     * @param clienteId identificador del cliente autenticado
     * @return productos favoritos activos
     */
    public List<Producto> listarFavoritos(String clienteId) {
        List<Favorito> favoritos = favoritoRepository.findByClienteIdAndActivoTrue(clienteId);

        Map<String, Producto> productos = productoRepository
                .findAllById(favoritos.stream().map(Favorito::getProductoId).toList())
                .stream()
                .filter(Producto::isActivo)
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        return favoritos.stream()
                .sorted(Comparator.comparing(Favorito::getFechaAlta,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(favorito -> productos.get(favorito.getProductoId()))
                .filter(Objects::nonNull)
                .toList();
    }
}