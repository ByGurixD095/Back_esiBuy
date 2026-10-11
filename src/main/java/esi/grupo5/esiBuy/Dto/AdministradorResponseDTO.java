package esi.grupo5.esiBuy.Dto;

import java.time.LocalDate;

/** Respuesta de usuario que incluye los datos específicos de un administrador. */
public class AdministradorResponseDTO extends UsuarioResponseDTO {
    /** Sede de trabajo del administrador. */
    private final String sede;
    /** Fecha de incorporación del administrador. */
    private final LocalDate fechaIncorporacion;
    /** Mensaje asociado a la respuesta, si corresponde. */
    private final String mensaje;

    /** Construye la respuesta a partir de los valores recopilados por el builder. */
    private AdministradorResponseDTO(Builder builder) {
        super(builder.commonFields());
        this.sede = builder.sede;
        this.fechaIncorporacion = builder.fechaIncorporacion;
        this.mensaje = builder.mensaje;
    }

    /** @return un builder para una respuesta de administrador */
    public static Builder builder() {
        return new Builder();
    }

    /** @return sede de trabajo */
    public String getSede() { return sede; }
    /** @return fecha de incorporación */
    public LocalDate getFechaIncorporacion() { return fechaIncorporacion; }
    /** @return mensaje de la respuesta */
    public String getMensaje() { return mensaje; }

    /** Builder para los campos específicos de una respuesta de administrador. */
    public static class Builder extends UsuarioResponseDTO.Builder<Builder> {
        /** Sede de trabajo configurada para la respuesta. */
        private String sede;
        /** Fecha de incorporación configurada para la respuesta. */
        private LocalDate fechaIncorporacion;
        /** Mensaje configurado para la respuesta. */
        private String mensaje;

        /** @return este builder */
        @Override
        protected Builder self() {
            return this;
        }

        /**
         * @param sede sede de trabajo
         * @return este builder
         */
        public Builder sede(String sede) { this.sede = sede; return this; }
        /**
         * @param fechaIncorporacion fecha de incorporación
         * @return este builder
         */
        public Builder fechaIncorporacion(LocalDate fechaIncorporacion) {
            this.fechaIncorporacion = fechaIncorporacion;
            return this;
        }
        /**
         * @param mensaje mensaje de la respuesta
         * @return este builder
         */
        public Builder mensaje(String mensaje) { this.mensaje = mensaje; return this; }

        /** @return respuesta de administrador construida */
        @Override
        public AdministradorResponseDTO build() {
            return new AdministradorResponseDTO(this);
        }
    }
}
