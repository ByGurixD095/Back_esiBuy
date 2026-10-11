package esi.grupo5.esiBuy.Dto;

/** Respuesta de usuario que incluye los datos específicos de un vendedor. */
public class VendedorResponseDTO extends UsuarioResponseDTO {
    /** Nombre comercial del vendedor. */
    private final String nombreComercial;
    /** CIF o NIF asociado al vendedor. */
    private final String cifNif;
    /** Identificador de la categoría principal del vendedor. */
    private final String categoriaPrincipalId;

    /** Construye la respuesta a partir de los valores recopilados por el builder. */
    private VendedorResponseDTO(Builder builder) {
        super(builder.commonFields());
        this.nombreComercial = builder.nombreComercial;
        this.cifNif = builder.cifNif;
        this.categoriaPrincipalId = builder.categoriaPrincipalId;
    }

    /** @return un builder para una respuesta de vendedor */
    public static Builder builder() {
        return new Builder();
    }

    /** @return nombre comercial */
    public String getNombreComercial() { return nombreComercial; }
    /** @return CIF o NIF */
    public String getCifNif() { return cifNif; }
    /** @return identificador de la categoría principal */
    public String getCategoriaPrincipalId() { return categoriaPrincipalId; }

    /** Builder para los campos específicos de una respuesta de vendedor. */
    public static class Builder extends UsuarioResponseDTO.Builder<Builder> {
        /** Nombre comercial configurado para la respuesta. */
        private String nombreComercial;
        /** CIF o NIF configurado para la respuesta. */
        private String cifNif;
        /** Identificador de la categoría principal configurado para la respuesta. */
        private String categoriaPrincipalId;

        /** @return este builder */
        @Override
        protected Builder self() {
            return this;
        }

        /**
         * @param nombreComercial nombre comercial
         * @return este builder
         */
        public Builder nombreComercial(String nombreComercial) {
            this.nombreComercial = nombreComercial;
            return this;
        }
        /**
         * @param cifNif CIF o NIF
         * @return este builder
         */
        public Builder cifNif(String cifNif) { this.cifNif = cifNif; return this; }
        /**
         * @param categoriaPrincipalId identificador de la categoría principal
         * @return este builder
         */
        public Builder categoriaPrincipalId(String categoriaPrincipalId) {
            this.categoriaPrincipalId = categoriaPrincipalId;
            return this;
        }

        /** @return respuesta de vendedor construida */
        @Override
        public VendedorResponseDTO build() {
            return new VendedorResponseDTO(this);
        }
    }
}
