package esi.grupo5.esiBuy.Dto;

public class VendedorResponseDTO extends UsuarioResponseDTO {
    private final String nombreComercial;
    private final String cifNif;
    private final String categoriaPrincipalId;

    private VendedorResponseDTO(Builder builder) {
        super(builder.commonFields());
        this.nombreComercial = builder.nombreComercial;
        this.cifNif = builder.cifNif;
        this.categoriaPrincipalId = builder.categoriaPrincipalId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getNombreComercial() { return nombreComercial; }
    public String getCifNif() { return cifNif; }
    public String getCategoriaPrincipalId() { return categoriaPrincipalId; }

    public static class Builder extends UsuarioResponseDTO.Builder<Builder> {
        private String nombreComercial;
        private String cifNif;
        private String categoriaPrincipalId;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder nombreComercial(String nombreComercial) {
            this.nombreComercial = nombreComercial;
            return this;
        }
        public Builder cifNif(String cifNif) { this.cifNif = cifNif; return this; }
        public Builder categoriaPrincipalId(String categoriaPrincipalId) {
            this.categoriaPrincipalId = categoriaPrincipalId;
            return this;
        }

        @Override
        public VendedorResponseDTO build() {
            return new VendedorResponseDTO(this);
        }
    }
}
