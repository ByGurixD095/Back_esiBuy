package esi.grupo5.esiBuy.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import esi.grupo5.esiBuy.Model.Categoria;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.CategoriaRepository;

/** Migra una sola vez las categorías antiguas guardadas como nombres en productos. */
@Component
@ConditionalOnProperty(name = "app.migrations.product-category.enabled", havingValue = "true", matchIfMissing = true)
public class ProductoCategoriaMigration implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;
    private final CategoriaRepository categoriaRepository;

    public ProductoCategoriaMigration(MongoTemplate mongoTemplate, CategoriaRepository categoriaRepository) {
        this.mongoTemplate = mongoTemplate;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, Categoria> categoriasPorNombre = new HashMap<>();
        categoriaRepository.findAll().forEach(categoria ->
                categoriasPorNombre.put(clave(categoria.getNombre()), categoria));

        mongoTemplate.getCollection("productos")
                .distinct("categoria", String.class)
                .into(new ArrayList<>())
                .stream()
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .forEach(nombre -> migrarNombre(nombre, categoriasPorNombre));

        long productosSinCategoria = mongoTemplate.count(
                Query.query(Criteria.where("categoriaId").is(null)), Producto.class);
        if (productosSinCategoria > 0) {
            throw new IllegalStateException(
                    "No se pudieron migrar " + productosSinCategoria
                            + " productos: tienen una categoría vacía o sin nombre antiguo.");
        }
    }

    private void migrarNombre(String nombreAntiguo, Map<String, Categoria> categoriasPorNombre) {
        String nombreNormalizado = nombreAntiguo.trim();
        String clave = clave(nombreNormalizado);
        Categoria categoria = categoriasPorNombre.get(clave);
        if (categoria == null) {
            categoria = categoriaRepository.save(new Categoria(null, nombreNormalizado));
            categoriasPorNombre.put(clave, categoria);
        }

        mongoTemplate.getCollection("productos").updateMany(
                Filters.eq("categoria", nombreAntiguo),
                Updates.combine(
                        Updates.set("categoriaId", categoria.getId()),
                        Updates.unset("categoria")));
    }

    private String clave(String nombre) {
        return nombre == null ? "" : nombre.trim().toLowerCase(Locale.ROOT);
    }
}
