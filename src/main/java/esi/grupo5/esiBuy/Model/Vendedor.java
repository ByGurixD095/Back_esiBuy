package esi.grupo5.esiBuy.Model;

import esi.grupo5.esiBuy.Model.enums.Rol;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.core.index.Indexed;

public class Vendedor extends Usuario {

    @NotBlank
    @Indexed(unique = true)
    private String nombreComercial;

    @NotBlank
    private String cifNif;

    @NotBlank
    private String categoriaPrincipalId;

    public Vendedor() {
        super();
        this.setRol(Rol.VENDEDOR);
    }

    public Vendedor(String nombre, String apellidos, String email, 
                    String contrasena, String nombreComercial, String cifNif) {
        super(nombre, apellidos, email, contrasena, null, null);
        this.setRol(Rol.VENDEDOR);
        this.nombreComercial = nombreComercial;
        this.cifNif = cifNif;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Vendedor vendedor = new Vendedor();

        public Builder nombre(String nombre) { vendedor.setNombre(nombre); return this; }
        public Builder apellidos(String apellidos) { vendedor.setApellidos(apellidos); return this; }
        public Builder email(String email) { vendedor.setEmail(email); return this; }
        public Builder contrasena(String contrasena) { vendedor.setContrasena(contrasena); return this; }
        public Builder telefono(String telefono) { vendedor.setTelefono(telefono); return this; }
        public Builder imagenPerfil(String imagenPerfil) { vendedor.setImagenPerfil(imagenPerfil); return this; }
        public Builder nombreComercial(String nombreComercial) { vendedor.setNombreComercial(nombreComercial); return this; }
        public Builder cifNif(String cifNif) { vendedor.setCifNif(cifNif); return this; }
        public Builder categoriaPrincipalId(String categoriaPrincipalId) { 
            vendedor.setCategoriaPrincipalId(categoriaPrincipalId); 
            return this; 
        }

        public Vendedor build() {
            return vendedor;
        }
    }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public String getCifNif() { return cifNif; }
    public void setCifNif(String cifNif) { this.cifNif = cifNif; }

    public String getCategoriaPrincipalId() { return categoriaPrincipalId; }
    public void setCategoriaPrincipalId(String categoriaPrincipalId) { this.categoriaPrincipalId = categoriaPrincipalId; }
}