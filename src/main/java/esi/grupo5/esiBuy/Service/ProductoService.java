package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.FiltroCatalogoDTO;
import esi.grupo5.esiBuy.Dto.ProductoDTO;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    private static final String ACTIVO_FIELD = "activo";
    private static final String STOCK_FIELD = "numStock";
    private static final String PRECIO_CENT_FIELD = "precioCent";
    private static final String NOMBRE_FIELD = "nombre";
    private static final String REFERENCIA_FIELD = "referencia";
    private static final String DESCUENTO_FIELD = "descuento";

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
                valorPorDefecto(dto.numStock()),
                dto.precioCent(),
                dto.descripcion(),
                dto.categoria(),
                dto.urlImagen(),
                valorPorDefecto(dto.descuento()),
                valorPorDefecto(dto.descuentoPremium())
        );

        producto.setActivo(dto.activo() == null || dto.activo());
        return productoRepository.save(producto);
    }

    public List<Producto> obtenerProductosDisponibles(FiltroCatalogoDTO filtros) {
        return mongoTemplate.find(construirConsulta(filtros, Sort.unsorted()), Producto.class);
    }

    public Page<Producto> obtenerProductosDisponibles(FiltroCatalogoDTO filtros, Pageable pageable) {
        Query query = construirConsulta(filtros, pageable.getSort());
        long total = mongoTemplate.count(query, Producto.class);

        if (!pageable.isUnpaged()) {
            query.skip(pageable.getOffset());
            query.limit(pageable.getPageSize());
        }

        List<Producto> productos = mongoTemplate.find(query, Producto.class);
        return new PageImpl<>(productos, pageable, total);
    }

    private Query construirConsulta(FiltroCatalogoDTO filtros, Sort ordenAlternativo) {
        Query query = new Query();
        query.addCriteria(Criteria.where(ACTIVO_FIELD).is(true).and(STOCK_FIELD).gt(0));

        if (filtros != null) {
            aplicarBusqueda(query, filtros.busqueda());
            aplicarCategoria(query, filtros.categoria());
            aplicarRangoPrecio(query, filtros.precioMinCent(), filtros.precioMaxCent());
            aplicarOferta(query, filtros.soloOfertas());
        }

        String ordenSolicitado = filtros == null ? null : filtros.orden();
        aplicarOrden(query, ordenSolicitado, ordenAlternativo);
        return query;
    }

    private void aplicarBusqueda(Query query, String busqueda) {
        if (busqueda != null && !busqueda.isBlank()) {
            String textoBusqueda = Pattern.quote(busqueda.trim());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where(NOMBRE_FIELD).regex(textoBusqueda, "i"),
                    Criteria.where(REFERENCIA_FIELD).regex(textoBusqueda, "i")
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
            query.addCriteria(Criteria.where(DESCUENTO_FIELD).gt(0));
        }
    }

    private void aplicarOrden(Query query, String orden) {
        if ("Precio Ascendente".equalsIgnoreCase(orden)) {
            query.with(Sort.by(Sort.Direction.ASC, PRECIO_CENT_FIELD));
        } else if ("Precio Descendente".equalsIgnoreCase(orden)) {
            query.with(Sort.by(Sort.Direction.DESC, PRECIO_CENT_FIELD));
        }
    }

    private void aplicarOrden(Query query, String orden, Sort ordenAlternativo) {
        aplicarOrden(query, orden);
        if ((orden == null || orden.isBlank()) && ordenAlternativo != null && ordenAlternativo.isSorted()) {
            query.with(ordenAlternativo);
        }
    }

    public List<Producto> obtenerProductosDisponibles() {
        return productoRepository.findByActivoTrueAndNumStockGreaterThan(0);
    }

    public Page<Producto> obtenerCatalogoVendedor(String idVendedor, int numeroPagina, int tamanoPagina) {
        Pageable pageable = PageRequest.of(numeroPagina, tamanoPagina);
        return productoRepository.findByIdVendedor(idVendedor, pageable);
    }

    private int valorPorDefecto(Integer valor) {
        return valor == null ? 0 : valor;
    }
}
