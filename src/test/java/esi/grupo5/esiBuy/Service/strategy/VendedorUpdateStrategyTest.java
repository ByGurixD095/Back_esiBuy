package esi.grupo5.esiBuy.Service.strategy;

import esi.grupo5.esiBuy.Dto.UserPatchDTO;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Vendedor;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VendedorUpdateStrategyTest {

    private final VendedorUpdateStrategy strategy = new VendedorUpdateStrategy();

    @Test
    void supports_onlyVendedores() {
        assertTrue(strategy.supports(new Vendedor()));
        assertFalse(strategy.supports(new Cliente()));
    }

    @Test
    void actualizar_aplicaLosCamposNoNulosYConservaLosDemases() {
        Vendedor vendedor = Vendedor.builder()
                .nombreComercial("Comercio")
                .cifNif("B12345678")
                .categoriaPrincipalId("ropa")
                .build();
        UserPatchDTO dto = new UserPatchDTO(
                null, null, null, null, null, null, null,
                "Nuevo comercio", "B87654321", "tecnologia", null);

        strategy.actualizar(vendedor, dto);

        assertEquals("Nuevo comercio", vendedor.getNombreComercial());
        assertEquals("B87654321", vendedor.getCifNif());
        assertEquals("tecnologia", vendedor.getCategoriaPrincipalId());
    }

    @Test
    void actualizar_ignoraCamposNulos() {
        Vendedor vendedor = Vendedor.builder()
                .nombreComercial("Comercio")
                .cifNif("B12345678")
                .categoriaPrincipalId("ropa")
                .build();

        strategy.actualizar(vendedor, new UserPatchDTO(
                null, null, null, null, null, null, null,
                null, null, null, null));

        assertEquals("Comercio", vendedor.getNombreComercial());
        assertEquals("B12345678", vendedor.getCifNif());
        assertEquals("ropa", vendedor.getCategoriaPrincipalId());
    }

    @Test
    void actualizar_rechazaCadaCampoDeTextoEnBlanco() {
        Vendedor vendedor = new Vendedor();

        assertBlankFieldRejected(vendedor, " ");
        assertBlankFieldRejected(vendedor, null, " ");
        assertBlankFieldRejected(vendedor, null, null, " ");
    }

    private void assertBlankFieldRejected(Vendedor vendedor, String nombreComercial) {
        assertBadRequest(() -> strategy.actualizar(vendedor, new UserPatchDTO(
                null, null, null, null, null, null, null,
                nombreComercial, null, null, null)));
    }

    private void assertBlankFieldRejected(Vendedor vendedor, String nombreComercial, String cifNif) {
        assertBadRequest(() -> strategy.actualizar(vendedor, new UserPatchDTO(
                null, null, null, null, null, null, null,
                nombreComercial, cifNif, null, null)));
    }

    private void assertBlankFieldRejected(
            Vendedor vendedor, String nombreComercial, String cifNif, String categoriaPrincipalId) {
        assertBadRequest(() -> strategy.actualizar(vendedor, new UserPatchDTO(
                null, null, null, null, null, null, null,
                nombreComercial, cifNif, categoriaPrincipalId, null)));
    }

    private void assertBadRequest(Runnable action) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
