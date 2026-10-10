package esi.grupo5.esiBuy.Dto;

import java.time.LocalDate;

public class AdministradorResponseDTO extends UsuarioResponseDTO {
    private final String sede;
    private final LocalDate fechaIncorporacion;
    private final String mensaje;

    private AdministradorResponseDTO(Builder builder) {
        super(builder.commonFields());
        this.sede = builder.sede;
        this.fechaIncorporacion = builder.fechaIncorporacion;
        this.mensaje = builder.mensaje;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getSede() { return sede; }
    public LocalDate getFechaIncorporacion() { return fechaIncorporacion; }
    public String getMensaje() { return mensaje; }

    public static class Builder extends UsuarioResponseDTO.Builder<Builder> {
        private String sede;
        private LocalDate fechaIncorporacion;
        private String mensaje;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder sede(String sede) { this.sede = sede; return this; }
        public Builder fechaIncorporacion(LocalDate fechaIncorporacion) {
            this.fechaIncorporacion = fechaIncorporacion;
            return this;
        }
        public Builder mensaje(String mensaje) { this.mensaje = mensaje; return this; }

        @Override
        public AdministradorResponseDTO build() {
            return new AdministradorResponseDTO(this);
        }
    }
}
