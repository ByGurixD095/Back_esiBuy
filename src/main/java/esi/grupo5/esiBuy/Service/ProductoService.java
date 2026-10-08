package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.FiltroCatalogoDTO;
import esi.grupo5.esiBuy.Dto.ProductoDTO;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    private static final String PRECIO_CENT_FIELD = "precioCent";

    private final ProductoRepository productoRepository;
    private final MongoTemplate mongoTemplate;

    public ProductoService(ProductoRepository productoRepository, MongoTemplate mongoTemplate) {
        this.productoRepository = productoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Producto crearProducto(ProductoDTO dto) {
        Producto producto = new Producto(
                dto.nombre(),
                dto.referencia(),
                dto.numStock() != null ? dto.numStock() : 0,
                dto.precioCent(),
                dto.descripcion(),
                dto.categoria(),
                dto.urlImagen(),
                dto.descuento(),
                dto.descuentoPremium()
        );

        producto.setActivo(dto.activo() == null || dto.activo());
        return productoRepository.save(producto);
    }

    public List<Producto> obtenerProductosDisponibles(FiltroCatalogoDTO filtros) {
        Query query = new Query();
        query.addCriteria(Criteria.where("activo").is(true).and("numStock").gt(0));

        if (filtros != null) {
            aplicarBusqueda(query, filtros.busqueda());
            aplicarCategoria(query, filtros.categoria());
            aplicarRangoPrecio(query, filtros.precioMinCent(), filtros.precioMaxCent());
            aplicarOferta(query, filtros.soloOfertas());
            aplicarOrden(query, filtros.orden());
        }

        return mongoTemplate.find(query, Producto.class);
    }

    private void aplicarBusqueda(Query query, String busqueda) {
        if (busqueda != null && !busqueda.isBlank()) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("nombre").regex(busqueda.trim(), "i"),
                    Criteria.where("referencia").regex(busqueda.trim(), "i")
            ));
        }
    }

    private void aplicarCategoria(Query query, String categoria) {
        if (categoria != null && !categoria.isBlank() && !categoria.equalsIgnoreCase("Todas")) {
            query.addCriteria(Criteria.where("categoria").is(categoria));
        }
    }

    private void aplicarRangoPrecio(Query query, Integer minimo, Integer maximo) {
        if (minimo != null || maximo != null) {
            Criteria precio = Criteria.where(PRECIO_CENT_FIELD);
            if (minimo != null) {
                precio.gte(minimo);
            }
            if (maximo != null) {
                precio.lte(maximo);
            }
            query.addCriteria(precio);
        }
    }

    private void aplicarOferta(Query query, Boolean soloOfertas) {
        if (Boolean.TRUE.equals(soloOfertas)) {
            query.addCriteria(Criteria.where("descuento").gt(0));
        }
    }

    private void aplicarOrden(Query query, String orden) {
        if ("Precio Ascendente".equalsIgnoreCase(orden)) {
            query.with(Sort.by(Sort.Direction.ASC, PRECIO_CENT_FIELD));
        } else if ("Precio Descendente".equalsIgnoreCase(orden)) {
            query.with(Sort.by(Sort.Direction.DESC, PRECIO_CENT_FIELD));
        }
    }

    public List<Producto> obtenerProductosDisponibles() {
        return productoRepository.findByActivoTrueAndNumStockGreaterThan(0);
    }
}
