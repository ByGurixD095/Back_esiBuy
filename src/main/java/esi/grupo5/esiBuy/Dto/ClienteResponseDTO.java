package esi.grupo5.esiBuy.Dto;

import java.time.LocalDate;

import esi.grupo5.esiBuy.Model.enums.TipoCliente;

/** Respuesta de usuario que incluye los datos específicos de un cliente. */
public class ClienteResponseDTO extends UsuarioResponseDTO {
    /** Documento nacional de identidad del cliente. */
    private final String dni;
    /** Fecha de nacimiento del cliente. */
    private final LocalDate fechaNacimiento;
    /** Tipo de cliente asignado. */
    private final TipoCliente tipoCliente;

    /** Construye la respuesta a partir de los valores recopilados por el builder. */
    private ClienteResponseDTO(Builder builder) {
        super(builder.commonFields());
        this.dni = builder.dni;
        this.fechaNacimiento = builder.fechaNacimiento;
        this.tipoCliente = builder.tipoCliente;
    }

    /** @return un builder para una respuesta de cliente */
    public static Builder builder() {
        return new Builder();
    }

    /** @return DNI del cliente */
    public String getDni() { return dni; }
    /** @return fecha de nacimiento */
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    /** @return tipo de cliente */
    public TipoCliente getTipoCliente() { return tipoCliente; }

    /** Builder para los campos específicos de una respuesta de cliente. */
    public static class Builder extends UsuarioResponseDTO.Builder<Builder> {
        /** DNI configurado para la respuesta. */
        private String dni;
        /** Fecha de nacimiento configurada para la respuesta. */
        private LocalDate fechaNacimiento;
        /** Tipo de cliente configurado para la respuesta. */
        private TipoCliente tipoCliente;

        /** @return este builder */
        @Override
        protected Builder self() {
            return this;
        }

        /**
         * @param dni documento nacional de identidad
         * @return este builder
         */
        public Builder dni(String dni) { this.dni = dni; return this; }
        /**
         * @param fechaNacimiento fecha de nacimiento
         * @return este builder
         */
        public Builder fechaNacimiento(LocalDate fechaNacimiento) {
            this.fechaNacimiento = fechaNacimiento;
            return this;
        }
        /**
         * @param tipoCliente tipo de cliente
         * @return este builder
         */
        public Builder tipoCliente(TipoCliente tipoCliente) {
            this.tipoCliente = tipoCliente;
            return this;
        }

        /** @return respuesta de cliente construida */
        @Override
        public ClienteResponseDTO build() {
            return new ClienteResponseDTO(this);
        }
    }
}
