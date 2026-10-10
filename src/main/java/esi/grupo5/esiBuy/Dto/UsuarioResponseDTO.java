package esi.grupo5.esiBuy.Dto;

import java.time.LocalDateTime;

import esi.grupo5.esiBuy.Model.enums.Rol;

public class UsuarioResponseDTO {
    private final String id;
    private final String name;
    private final String apellidos;
    private final String email;
    private final String telefono;
    private final String imagenPerfil;
    private final Rol rol;
    private final boolean activo;
    private final boolean bloqueado;
    private final boolean eliminado;
    private final LocalDateTime fechaAlta;
    private final LocalDateTime fechaModificacion;
    private final LocalDateTime fechaCambioContrasena;
    private final boolean mfaConfigurado;
    private final boolean dosFactorActivoCliente;
    private final boolean tresFactorActivoCliente;

    protected UsuarioResponseDTO(CommonFields fields) {
        this.id = fields.id;
        this.name = fields.name;
        this.apellidos = fields.apellidos;
        this.email = fields.email;
        this.telefono = fields.telefono;
        this.imagenPerfil = fields.imagenPerfil;
        this.rol = fields.rol;
        this.activo = fields.activo;
        this.bloqueado = fields.bloqueado;
        this.eliminado = fields.eliminado;
        this.fechaAlta = fields.fechaAlta;
        this.fechaModificacion = fields.fechaModificacion;
        this.fechaCambioContrasena = fields.fechaCambioContrasena;
        this.mfaConfigurado = fields.mfaConfigurado;
        this.dosFactorActivoCliente = fields.dosFactorActivoCliente;
        this.tresFactorActivoCliente = fields.tresFactorActivoCliente;
    }

    public static UsuarioBuilder usuarioBuilder() {
        return new UsuarioBuilder();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getApellidos() { return apellidos; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getImagenPerfil() { return imagenPerfil; }
    public Rol getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public boolean isBloqueado() { return bloqueado; }
    public boolean isEliminado() { return eliminado; }
    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public LocalDateTime getFechaCambioContrasena() { return fechaCambioContrasena; }
    public boolean isMfaConfigurado() { return mfaConfigurado; }
    public boolean isDosFactorActivoCliente() { return dosFactorActivoCliente; }
    public boolean isTresFactorActivoCliente() { return tresFactorActivoCliente; }

    protected static final class CommonFields {
        private final String id;
        private final String name;
        private final String apellidos;
        private final String email;
        private final String telefono;
        private final String imagenPerfil;
        private final Rol rol;
        private final boolean activo;
        private final boolean bloqueado;
        private final boolean eliminado;
        private final LocalDateTime fechaAlta;
        private final LocalDateTime fechaModificacion;
        private final LocalDateTime fechaCambioContrasena;
        private final boolean mfaConfigurado;
        private final boolean dosFactorActivoCliente;
        private final boolean tresFactorActivoCliente;

        private <T extends Builder<T>> CommonFields(Builder<T> builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.apellidos = builder.apellidos;
            this.email = builder.email;
            this.telefono = builder.telefono;
            this.imagenPerfil = builder.imagenPerfil;
            this.rol = builder.rol;
            this.activo = builder.activo;
            this.bloqueado = builder.bloqueado;
            this.eliminado = builder.eliminado;
            this.fechaAlta = builder.fechaAlta;
            this.fechaModificacion = builder.fechaModificacion;
            this.fechaCambioContrasena = builder.fechaCambioContrasena;
            this.mfaConfigurado = builder.mfaConfigurado;
            this.dosFactorActivoCliente = builder.dosFactorActivoCliente;
            this.tresFactorActivoCliente = builder.tresFactorActivoCliente;
        }
    }

    public abstract static class Builder<T extends Builder<T>> {
        protected String id;
        protected String name;
        protected String apellidos;
        protected String email;
        protected String telefono;
        protected String imagenPerfil;
        protected Rol rol;
        protected boolean activo;
        protected boolean bloqueado;
        protected boolean eliminado;
        protected LocalDateTime fechaAlta;
        protected LocalDateTime fechaModificacion;
        protected LocalDateTime fechaCambioContrasena;
        protected boolean mfaConfigurado;
        protected boolean dosFactorActivoCliente;
        protected boolean tresFactorActivoCliente;

        protected abstract T self();

        public T id(String id) { this.id = id; return self(); }
        public T name(String name) { this.name = name; return self(); }
        public T apellidos(String apellidos) { this.apellidos = apellidos; return self(); }
        public T email(String email) { this.email = email; return self(); }
        public T telefono(String telefono) { this.telefono = telefono; return self(); }
        public T imagenPerfil(String imagenPerfil) { this.imagenPerfil = imagenPerfil; return self(); }
        public T rol(Rol rol) { this.rol = rol; return self(); }
        public T activo(boolean activo) { this.activo = activo; return self(); }
        public T bloqueado(boolean bloqueado) { this.bloqueado = bloqueado; return self(); }
        public T eliminado(boolean eliminado) { this.eliminado = eliminado; return self(); }
        public T fechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; return self(); }
        public T fechaModificacion(LocalDateTime fechaModificacion) {
            this.fechaModificacion = fechaModificacion;
            return self();
        }
        public T fechaCambioContrasena(LocalDateTime fechaCambioContrasena) {
            this.fechaCambioContrasena = fechaCambioContrasena;
            return self();
        }
        public T mfaConfigurado(boolean mfaConfigurado) {
            this.mfaConfigurado = mfaConfigurado;
            return self();
        }
        public T dosFactorActivoCliente(boolean activo) {
            this.dosFactorActivoCliente = activo;
            return self();
        }
        public T tresFactorActivoCliente(boolean activo) {
            this.tresFactorActivoCliente = activo;
            return self();
        }

        protected CommonFields commonFields() {
            return new CommonFields(this);
        }

        public UsuarioResponseDTO build() {
            return new UsuarioResponseDTO(commonFields());
        }
    }

    public static final class UsuarioBuilder extends Builder<UsuarioBuilder> {
        @Override
        protected UsuarioBuilder self() {
            return this;
        }

        @Override
        public UsuarioResponseDTO build() {
            return new UsuarioResponseDTO(commonFields());
        }
    }
}
