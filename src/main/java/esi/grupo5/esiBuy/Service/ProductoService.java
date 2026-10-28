package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.FiltroCatalogoDTO;
import esi.grupo5.esiBuy.Dto.ProductInfoDTO;
import esi.grupo5.esiBuy.Dto.ProductoDTO;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.Categoria;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.CategoriaRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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

/** Implementa la consulta del catálogo y la gestión de productos de vendedores. */
@Service
public class ProductoService {

    /** Nombre persistido del campo de estado activo. */
    private static final String ACTIVO_FIELD = "activo";
    /** Nombre persistido del campo de existencias. */
    private static final String STOCK_FIELD = "numStock";
    /** Nombre persistido del campo de precio en céntimos. */
    private static final String PRECIO_CENT_FIELD = "precioCent";
    /** Nombre persistido del campo nombre del producto. */
    private static final String NOMBRE_FIELD = "nombre";
    /** Nombre persistido del campo de referencia. */
    private static final String REFERENCIA_FIELD = "referencia";
    /** Nombre persistido del campo de descuento. */
    private static final String DESCUENTO_FIELD = "descuento";

    /** Repositorio CRUD de productos. */
    private final ProductoRepository productoRepository;
    /** Repositorio CRUD de categorías. */
    private final CategoriaRepository categoriaRepository;
    /** Motor de consultas MongoDB para filtros del catálogo. */
    private final MongoTemplate mongoTemplate;

    private final UserService usuarioService;

    /**
     * Crea el servicio de productos.
     *
     * @param productoRepository repositorio de productos
     * @param mongoTemplate motor de consultas MongoDB
     */
    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
            MongoTemplate mongoTemplate, UserService usuarioService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.mongoTemplate = mongoTemplate;
        this.usuarioService = usuarioService;
    }

    /**
     * Crea un producto para el vendedor indicado y aplica cero a los enteros opcionales nulos.
     *
     * @param dto datos del producto
     * @param idVendedor identificador del vendedor propietario
     * @return producto persistido
     */
    public Producto crearProducto(ProductoDTO dto, String idVendedor) {
        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
        Producto producto = new Producto(
                dto.nombre(),
                dto.referencia(),
                valorPorDefecto(dto.numStock()),
                dto.precioCent(),
                dto.descripcion(),
                dto.categoriaId(),
                dto.urlImagen(),
                valorPorDefecto(dto.descuento()),
                valorPorDefecto(dto.descuentoPremium())
        );

        producto.setActivo(dto.activo() == null || dto.activo());
        producto.setIdVendedor(idVendedor);
        
        producto = productoRepository.save(producto);
        producto.setCategoria(categoria.getNombre());
        return producto;
    }

    /**
     * Busca los productos disponibles que cumplen los filtros, sin paginar.
     *
     * @param filtros filtros opcionales del catálogo
     * @return productos coincidentes
     */
    public List<Producto> obtenerProductosDisponibles(FiltroCatalogoDTO filtros) {
        List<Producto> productos = mongoTemplate.find(construirConsulta(filtros, Sort.unsorted()), Producto.class);
        completarNombresCategorias(productos);
        return productos;
    }

    /**
     * Busca los productos disponibles que cumplen los filtros y los devuelve paginados.
     *
     * @param filtros filtros opcionales del catálogo
     * @param pageable página, tamaño y orden solicitados
     * @return página de productos y total de resultados coincidentes
     */
    public Page<Producto> obtenerProductosDisponibles(FiltroCatalogoDTO filtros, Pageable pageable) {
        Query query = construirConsulta(filtros, pageable.getSort());
        long total = mongoTemplate.count(query, Producto.class);

        if (!pageable.isUnpaged()) {
            query.skip(pageable.getOffset());
            query.limit(pageable.getPageSize());
        }

        List<Producto> productos = mongoTemplate.find(query, Producto.class);
        completarNombresCategorias(productos);
        return new PageImpl<>(productos, pageable, total);
    }

    /**
     * Construye una consulta para productos activos y con stock, incorporando filtros
     * y orden aplicables.
     *
     * @param filtros criterios del catálogo; puede ser nulo
     * @param ordenAlternativo orden de la página cuando el filtro no especifica orden
     * @return consulta MongoDB construida
     */
    private Query construirConsulta(FiltroCatalogoDTO filtros, Sort ordenAlternativo) {
        Query query = new Query();
        query.addCriteria(Criteria.where(ACTIVO_FIELD).is(true).and(STOCK_FIELD).gt(0));

        if (filtros != null) {
            aplicarBusqueda(query, filtros.busqueda());
            aplicarCategoria(query, filtros.categoriaId());
            aplicarRangoPrecio(query, filtros.precioMinCent(), filtros.precioMaxCent());
            aplicarOferta(query, filtros.soloOfertas());
        }

        String ordenSolicitado = filtros == null ? null : filtros.orden();
        aplicarOrden(query, ordenSolicitado, ordenAlternativo);
        return query;
    }

    /**
     * Añade una búsqueda literal e insensible a mayúsculas en nombre o referencia.
     *
     * @param query consulta que se modifica
     * @param busqueda texto que se buscará
     */
    private void aplicarBusqueda(Query query, String busqueda) {
        if (busqueda != null && !busqueda.isBlank()) {
            String textoBusqueda = Pattern.quote(busqueda.trim());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where(NOMBRE_FIELD).regex(textoBusqueda, "i"),
                    Criteria.where(REFERENCIA_FIELD).regex(textoBusqueda, "i")
            ));
        }
    }

    /**
     * Añade un filtro de categoría salvo que no se indique o se seleccione «Todas».
     *
     * @param query consulta que se modifica
     * @param categoriaId identificador de categoría seleccionada
     */
    private void aplicarCategoria(Query query, String categoriaId) {
        if (categoriaId != null && !categoriaId.isBlank()) {
            query.addCriteria(Criteria.where("categoriaId").is(categoriaId));
        }
    }

    /**
     * Añade los límites inclusivos de precio que estén definidos.
     *
     * @param query consulta que se modifica
     * @param minimo precio mínimo en céntimos
     * @param maximo precio máximo en céntimos
     */
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

    /**
     * Restringe la búsqueda a descuentos positivos cuando se solicitan solo ofertas.
     *
     * @param query consulta que se modifica
     * @param soloOfertas indicador de filtro exclusivo de ofertas
     */
    private void aplicarOferta(Query query, Boolean soloOfertas) {
        if (Boolean.TRUE.equals(soloOfertas)) {
            query.addCriteria(Criteria.where(DESCUENTO_FIELD).gt(0));
        }
    }

    /**
     * Aplica el orden de precio reconocido por el filtro de catálogo.
     *
     * @param query consulta que se modifica
     * @param orden nombre del orden solicitado
     */
    private void aplicarOrden(Query query, String orden) {
        if ("Precio Ascendente".equalsIgnoreCase(orden)) {
            query.with(Sort.by(Sort.Direction.ASC, PRECIO_CENT_FIELD));
        } else if ("Precio Descendente".equalsIgnoreCase(orden)) {
            query.with(Sort.by(Sort.Direction.DESC, PRECIO_CENT_FIELD));
        } else {
            query.with(Sort.unsorted());
        }
    }

    /**
     * Aplica el orden del filtro o, si no se indicó, el orden solicitado en la página.
     *
     * @param query consulta que se modifica
     * @param orden orden indicado en el filtro
     * @param ordenAlternativo orden de reserva de la página
     */
    private void aplicarOrden(Query query, String orden, Sort ordenAlternativo) {
        aplicarOrden(query, orden);
        if ((orden == null || orden.isBlank()) && ordenAlternativo != null && ordenAlternativo.isSorted()) {
            query.with(ordenAlternativo);
        }
    }

    /**
     * Obtiene todos los productos activos con existencias.
     *
     * @return productos disponibles
     */
    public List<Producto> obtenerProductosDisponibles() {
        List<Producto> productos = productoRepository.findByActivoTrueAndNumStockGreaterThan(0);
        completarNombresCategorias(productos);
        return productos;
    }

    /**
     * Obtiene una página del catálogo de un vendedor.
     *
     * @param idVendedor identificador del vendedor
     * @param numeroPagina índice de página, empezando en cero
     * @param tamanoPagina número de elementos por página
     * @return página del catálogo
     */
    public Page<Producto> obtenerCatalogoVendedor(String idVendedor, int numeroPagina, int tamanoPagina) {
        Pageable pageable = PageRequest.of(numeroPagina, tamanoPagina);
        Page<Producto> pagina = productoRepository.findByIdVendedor(idVendedor, pageable);
        completarNombresCategorias(pagina.getContent());
        return pagina;
    }

    /**
     * Recupera un producto que pertenezca al vendedor indicado.
     *
     * @param id identificador del producto
     * @param idVendedor identificador del propietario
     * @return producto encontrado
     * @throws NotFoundException si no existe o no pertenece al vendedor
     */
    public Producto obtenerProducto(String id, String idVendedor) {
        Producto producto = buscarProductoPropio(id, idVendedor);
        completarNombresCategorias(List.of(producto));
        return producto;
    }

    /**
     * Actualiza y persiste los datos de un producto propio.
     *
     * @param id identificador del producto
     * @param dto datos nuevos
     * @param idVendedor identificador del propietario
     * @return producto actualizado
     * @throws NotFoundException si no existe o no pertenece al vendedor
     */
    public Producto actualizarProducto(String id, ProductoDTO dto, String idVendedor) {
        Producto producto = buscarProductoPropio(id, idVendedor);

        producto.setNombre(dto.nombre());
        producto.setReferencia(dto.referencia());
        producto.setNumStock(dto.numStock() != null ? dto.numStock() : 0);
        producto.setPrecioCent(dto.precioCent());
        producto.setDescripcion(dto.descripcion());
        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
        producto.setCategoriaId(categoria.getId());
        producto.setUrlImagen(dto.urlImagen());
        producto.setDescuento(valorPorDefecto(dto.descuento()));
        producto.setDescuentoPremium(valorPorDefecto(dto.descuentoPremium()));
        if (dto.activo() != null) {
            producto.setActivo(dto.activo());
        }

        producto = productoRepository.save(producto);
        producto.setCategoria(categoria.getNombre());
        return producto;
    }

    /**
     * Elimina un producto propio.
     *
     * @param id identificador del producto
     * @param idVendedor identificador del propietario
     * @throws NotFoundException si no existe o no pertenece al vendedor
     */
    public void eliminarProducto(String id, String idVendedor) {
        Producto producto = buscarProductoPropio(id, idVendedor);
        productoRepository.delete(producto);
    }

    /**
     * Obtiene los datos públicos de un producto activo y el nombre comercial de su vendedor.
     *
     * @param id identificador del producto
     * @return información pública del producto
     * @throws NotFoundException si el producto no existe, está inactivo o no tiene vendedor
     */
    public ProductInfoDTO infoProducto(String id) {
        Producto producto = productoRepository.findById(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        String nombreComercial = usuarioService.getNombreComercialById(producto.getIdVendedor());
        Categoria categoria = categoriaRepository.findById(producto.getCategoriaId())
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));

        return new ProductInfoDTO(
                producto.getNombre(),
                producto.getNumStock(),
                producto.getPrecioCent(),
                producto.getDescripcion(),
                categoria.getNombre(),
                producto.getUrlImagen(),
                producto.getDescuento(),
                producto.getDescuentoPremium(),
                nombreComercial);
    }

    /**
     * Busca un producto verificando su identificador y propietario.
     *
     * @param id identificador del producto
     * @param idVendedor identificador del vendedor
     * @return producto perteneciente al vendedor
     * @throws NotFoundException si no hay coincidencia
     */
    private Producto buscarProductoPropio(String id, String idVendedor) {
        return productoRepository.findByIdAndIdVendedor(id, idVendedor)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
    }

    /**
     * Sustituye un entero nulo por cero.
     *
     * @param valor valor opcional
     * @return valor recibido o cero
     */
    private int valorPorDefecto(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private void completarNombresCategorias(List<Producto> productos) {
        Set<String> ids = productos.stream()
                .map(Producto::getCategoriaId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }

        Map<String, Categoria> categorias = StreamSupport.stream(
                        categoriaRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toMap(Categoria::getId, categoria -> categoria));
        for (Producto producto : productos) {
            if (producto.getCategoriaId() != null) {
                Categoria categoria = categorias.get(producto.getCategoriaId());
                if (categoria == null) {
                    throw new NotFoundException("Categoría no encontrada para el producto " + producto.getId());
                }
                producto.setCategoria(categoria.getNombre());
            }
        }
    }
}
