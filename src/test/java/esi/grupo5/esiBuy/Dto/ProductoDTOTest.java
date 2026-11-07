package esi.grupo5.esiBuy.Dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void datosValidos_yCamposOpcionalesNulos_noTienenErrores() {
        assertTrue(validator.validate(producto(
                "Camiseta ESI", "REF-092026a", 1999, null, "categoria-1", null,
                10, 5, 10)).isEmpty());
    }

    @Test
    void nombreReferenciaPrecioYCategoriaSonObligatorios() {
        assertInvalid(producto(null, "REF-092026a", 1999, null, "categoria-1", null, 10, 5, 10));
        assertInvalid(producto(" ", "REF-092026a", 1999, null, "categoria-1", null, 10, 5, 10));
        assertInvalid(producto("Camiseta ESI", "", 1999, null, "categoria-1", null, 10, 5, 10));
        assertInvalid(producto("Camiseta ESI", "REF-092026a", null, null, "categoria-1", null, 10, 5, 10));
        assertInvalid(producto("Camiseta ESI", "REF-092026a", 1999, null, "", null, 10, 5, 10));
    }

    @Test
    void stockYPrecioNoPuedenSerNegativos() {
        assertInvalid(producto("Camiseta ESI", "REF-092026a", -1, null, "categoria-1", null, 10, 5, 10));
        assertInvalid(producto("Camiseta ESI", "REF-092026a", 1999, null, "categoria-1", null, -1, 5, 10));
    }

    @Test
    void descuentosDebenEstarEntreCeroYCien() {
        assertInvalid(producto("Camiseta ESI", "REF-092026a", 1999, null, "categoria-1", null, 10, -1, 10));
        assertInvalid(producto("Camiseta ESI", "REF-092026a", 1999, null, "categoria-1", null, 10, 101, 10));
        assertInvalid(producto("Camiseta ESI", "REF-092026a", 1999, null, "categoria-1", null, 10, 5, -1));
        assertInvalid(producto("Camiseta ESI", "REF-092026a", 1999, null, "categoria-1", null, 10, 5, 101));
    }

    @Test
    void descripcionEImagenPuedenSerNulasPeroNoVacias() {
        assertInvalid(producto("Camiseta ESI", "REF-092026a", 1999, "", "categoria-1", null, 10, 5, 10));
        assertInvalid(producto("Camiseta ESI", "REF-092026a", 1999, null, "categoria-1", "", 10, 5, 10));
        assertInvalid(producto("Camiseta ESI", "REF-092026a", 1999, "   ", "categoria-1", null, 10, 5, 10));
        assertInvalid(producto("Camiseta ESI", "REF-092026a", 1999, null, "categoria-1", "   ", 10, 5, 10));
    }

    @Test
    void permiteCeroValoresNumericosYDescuentoMaximo() {
        assertTrue(validator.validate(producto(
                "Camiseta ESI", "REF-092026a", 0, null, "categoria-1", null, 0, 100, 100)).isEmpty());
    }

    private ProductoDTO producto(String nombre, String referencia, Integer precio, String descripcion,
                                 String categoriaId, String urlImagen, Integer stock, Integer descuento,
                                 Integer descuentoPremium) {
        return new ProductoDTO(nombre, referencia, precio, descripcion, categoriaId, urlImagen,
                stock, descuento, descuentoPremium, null);
    }

    private void assertInvalid(ProductoDTO dto) {
        assertFalse(validator.validate(dto).isEmpty());
    }
}
