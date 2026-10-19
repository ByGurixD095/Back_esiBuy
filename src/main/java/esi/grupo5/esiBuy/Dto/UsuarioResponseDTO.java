package esi.grupo5.esiBuy.Dto;

import java.time.LocalDateTime;

import esi.grupo5.esiBuy.Model.enums.Rol;

/** Datos comunes de una cuenta que se devuelven en las respuestas de la API. */
public class UsuarioResponseDTO {
    /** Identificador de la cuenta. */
    private final String id;
    /** Nombre de la persona titular. */
    private final String name;
    /** Apellidos de la persona titular. */
    private final String apellidos;
    /** Dirección de correo asociada a la cuenta. */
    private final String email;
    /** Teléfono de contacto. */
    private final String telefono;
    /** URL o referencia de la imagen de perfil. */
    private final String imagenPerfil;
    /** Rol asignado a la cuenta. */
    private final Rol rol;
    /** Indica si la cuenta está activa. */
    private final boolean activo;
    /** Indica si la cuenta está bloqueada. */
    private final boolean bloqueado;
    /** Fecha de alta de la cuenta. */
    private final LocalDateTime fechaAlta;
    /** Fecha de la última modificación de la cuenta. */
    private final LocalDateTime fechaModificacion;
    /** Fecha límite para cambiar la contraseña. */
    private final LocalDateTime fechaCambioContrasena;
    /** Indica si la configuración de MFA está completada. */
    private final boolean mfaConfigurado;
    /** Indica si está activo el segundo factor del cliente. */
    private final boolean dosFactorActivoCliente;
    /** Indica si está activo el tercer factor del cliente. */
    private final boolean tresFactorActivoCliente;

    /**
     * Construye la respuesta común a partir de los campos recopilados por un builder.
     *
     * @param fields valores comunes que se copiarán a la respuesta
     */
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
        this.fechaAlta = fields.fechaAlta;
        this.fechaModificacion = fields.fechaModificacion;
        this.fechaCambioContrasena = fields.fechaCambioContrasena;
        this.mfaConfigurado = fields.mfaConfigurado;
        this.dosFactorActivoCliente = fields.dosFactorActivoCliente;
        this.tresFactorActivoCliente = fields.tresFactorActivoCliente;
    }

    /** @return un builder para una respuesta de usuario */
    public static UsuarioBuilder usuarioBuilder() {
        return new UsuarioBuilder();
    }

    /** @return identificador de la cuenta */
    public String getId() { return id; }
    /** @return nombre de la persona titular */
    public String getName() { return name; }
    /** @return apellidos de la persona titular */
    public String getApellidos() { return apellidos; }
    /** @return dirección de correo */
    public String getEmail() { return email; }
    /** @return teléfono de contacto */
    public String getTelefono() { return telefono; }
    /** @return URL o referencia de la imagen de perfil */
    public String getImagenPerfil() { return imagenPerfil; }
    /** @return rol asignado */
    public Rol getRol() { return rol; }
    /** @return {@code true} si la cuenta está activa */
    public boolean isActivo() { return activo; }
    /** @return {@code true} si la cuenta está bloqueada */
    public boolean isBloqueado() { return bloqueado; }
    /** @return fecha de alta */
    public LocalDateTime getFechaAlta() { return fechaAlta; }
    /** @return fecha de última modificación */
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    /** @return fecha límite para cambiar la contraseña */
    public LocalDateTime getFechaCambioContrasena() { return fechaCambioContrasena; }
    /** @return {@code true} si MFA está configurado */
    public boolean isMfaConfigurado() { return mfaConfigurado; }
    /** @return {@code true} si el segundo factor del cliente está activo */
    public boolean isDosFactorActivoCliente() { return dosFactorActivoCliente; }
    /** @return {@code true} si el tercer factor del cliente está activo */
    public boolean isTresFactorActivoCliente() { return tresFactorActivoCliente; }

    /** Contenedor inmutable de los campos comunes de la respuesta. */
    protected static final class CommonFields {
        /** Identificador de la cuenta. */
        private final String id;
        /** Nombre de la persona titular. */
        private final String name;
        /** Apellidos de la persona titular. */
        private final String apellidos;
        /** Dirección de correo asociada a la cuenta. */
        private final String email;
        /** Teléfono de contacto. */
        private final String telefono;
        /** URL o referencia de la imagen de perfil. */
        private final String imagenPerfil;
        /** Rol asignado a la cuenta. */
        private final Rol rol;
        /** Estado de actividad de la cuenta. */
        private final boolean activo;
        /** Estado de bloqueo de la cuenta. */
        private final boolean bloqueado;
        /** Fecha de alta de la cuenta. */
        private final LocalDateTime fechaAlta;
        /** Fecha de última modificación de la cuenta. */
        private final LocalDateTime fechaModificacion;
        /** Fecha límite para cambiar la contraseña. */
        private final LocalDateTime fechaCambioContrasena;
        /** Estado de configuración MFA. */
        private final boolean mfaConfigurado;
        /** Estado del segundo factor del cliente. */
        private final boolean dosFactorActivoCliente;
        /** Estado del tercer factor del cliente. */
        private final boolean tresFactorActivoCliente;

        /** Copia a un contenedor los valores actuales del builder correspondiente. */
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
            this.fechaAlta = builder.fechaAlta;
            this.fechaModificacion = builder.fechaModificacion;
            this.fechaCambioContrasena = builder.fechaCambioContrasena;
            this.mfaConfigurado = builder.mfaConfigurado;
            this.dosFactorActivoCliente = builder.dosFactorActivoCliente;
            this.tresFactorActivoCliente = builder.tresFactorActivoCliente;
        }
    }

    /** Builder genérico y extensible para respuestas con los campos comunes de usuario. */
    public abstract static class Builder<T extends Builder<T>> {
        /** Identificador de la cuenta que se incluirá en la respuesta. */
        protected String id;
        /** Nombre de la persona titular que se incluirá en la respuesta. */
        protected String name;
        /** Apellidos que se incluirán en la respuesta. */
        protected String apellidos;
        /** Dirección de correo que se incluirá en la respuesta. */
        protected String email;
        /** Teléfono que se incluirá en la respuesta. */
        protected String telefono;
        /** Imagen de perfil que se incluirá en la respuesta. */
        protected String imagenPerfil;
        /** Rol que se incluirá en la respuesta. */
        protected Rol rol;
        /** Estado de actividad que se incluirá en la respuesta. */
        protected boolean activo;
        /** Estado de bloqueo que se incluirá en la respuesta. */
        protected boolean bloqueado;
        /** Fecha de alta que se incluirá en la respuesta. */
        protected LocalDateTime fechaAlta;
        /** Fecha de última modificación que se incluirá en la respuesta. */
        protected LocalDateTime fechaModificacion;
        /** Fecha límite de cambio de contraseña que se incluirá en la respuesta. */
        protected LocalDateTime fechaCambioContrasena;
        /** Estado de configuración MFA que se incluirá en la respuesta. */
        protected boolean mfaConfigurado;
        /** Estado del segundo factor que se incluirá en la respuesta. */
        protected boolean dosFactorActivoCliente;
        /** Estado del tercer factor que se incluirá en la respuesta. */
        protected boolean tresFactorActivoCliente;

        /**
         * Devuelve la instancia concreta para mantener el encadenamiento tipado.
         *
         * @return instancia concreta del builder
         */
        protected abstract T self();

        /**
         * @param id identificador de la cuenta
         * @return este builder
         */
        public T id(String id) { this.id = id; return self(); }
        /**
         * @param name nombre de la persona titular
         * @return este builder
         */
        public T name(String name) { this.name = name; return self(); }
        /**
         * @param apellidos apellidos de la persona titular
         * @return este builder
         */
        public T apellidos(String apellidos) { this.apellidos = apellidos; return self(); }
        /**
         * @param email dirección de correo
         * @return este builder
         */
        public T email(String email) { this.email = email; return self(); }
        /**
         * @param telefono teléfono de contacto
         * @return este builder
         */
        public T telefono(String telefono) { this.telefono = telefono; return self(); }
        /**
         * @param imagenPerfil URL o referencia de la imagen
         * @return este builder
         */
        public T imagenPerfil(String imagenPerfil) { this.imagenPerfil = imagenPerfil; return self(); }
        /**
         * @param rol rol asignado
         * @return este builder
         */
        public T rol(Rol rol) { this.rol = rol; return self(); }
        /**
         * @param activo estado de actividad
         * @return este builder
         */
        public T activo(boolean activo) { this.activo = activo; return self(); }
        /**
         * @param bloqueado estado de bloqueo
         * @return este builder
         */
        public T bloqueado(boolean bloqueado) { this.bloqueado = bloqueado; return self(); }
        /**
         * @param fechaAlta fecha de alta
         * @return este builder
         */
        public T fechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; return self(); }
        /**
         * @param fechaModificacion fecha de última modificación
         * @return este builder
         */
        public T fechaModificacion(LocalDateTime fechaModificacion) {
            this.fechaModificacion = fechaModificacion;
            return self();
        }
        /**
         * @param fechaCambioContrasena fecha límite para cambiar la contraseña
         * @return este builder
         */
        public T fechaCambioContrasena(LocalDateTime fechaCambioContrasena) {
            this.fechaCambioContrasena = fechaCambioContrasena;
            return self();
        }
        /**
         * @param mfaConfigurado estado de configuración MFA
         * @return este builder
         */
        public T mfaConfigurado(boolean mfaConfigurado) {
            this.mfaConfigurado = mfaConfigurado;
            return self();
        }
        /**
         * @param activo estado del segundo factor
         * @return este builder
         */
        public T dosFactorActivoCliente(boolean activo) {
            this.dosFactorActivoCliente = activo;
            return self();
        }
        /**
         * @param activo estado del tercer factor
         * @return este builder
         */
        public T tresFactorActivoCliente(boolean activo) {
            this.tresFactorActivoCliente = activo;
            return self();
        }

        /** @return contenedor inmutable de los valores comunes */
        protected CommonFields commonFields() {
            return new CommonFields(this);
        }

        /** @return respuesta con los campos comunes configurados */
        public UsuarioResponseDTO build() {
            return new UsuarioResponseDTO(commonFields());
        }
    }

    /** Builder concreto para crear una respuesta genérica de usuario. */
    public static final class UsuarioBuilder extends Builder<UsuarioBuilder> {
        /** @return este builder */
        @Override
        protected UsuarioBuilder self() {
            return this;
        }

        /** @return respuesta genérica construida */
        @Override
        public UsuarioResponseDTO build() {
            return new UsuarioResponseDTO(commonFields());
        }
    }
}
