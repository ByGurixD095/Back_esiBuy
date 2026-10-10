package esi.grupo5.esiBuy.Dto;

import java.time.LocalDate;

import esi.grupo5.esiBuy.Model.enums.TipoCliente;

public class ClienteResponseDTO extends UsuarioResponseDTO {
    private final String dni;
    private final LocalDate fechaNacimiento;
    private final TipoCliente tipoCliente;

    private ClienteResponseDTO(Builder builder) {
        super(builder.commonFields());
        this.dni = builder.dni;
        this.fechaNacimiento = builder.fechaNacimiento;
        this.tipoCliente = builder.tipoCliente;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getDni() { return dni; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public TipoCliente getTipoCliente() { return tipoCliente; }

    public static class Builder extends UsuarioResponseDTO.Builder<Builder> {
        private String dni;
        private LocalDate fechaNacimiento;
        private TipoCliente tipoCliente;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder dni(String dni) { this.dni = dni; return this; }
        public Builder fechaNacimiento(LocalDate fechaNacimiento) {
            this.fechaNacimiento = fechaNacimiento;
            return this;
        }
        public Builder tipoCliente(TipoCliente tipoCliente) {
            this.tipoCliente = tipoCliente;
            return this;
        }

        @Override
        public ClienteResponseDTO build() {
            return new ClienteResponseDTO(this);
        }
    }
}
