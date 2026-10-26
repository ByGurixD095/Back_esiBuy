package esi.grupo5.esiBuy.Model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import esi.grupo5.esiBuy.Model.enums.Rol;

/**
 * Datos comunes de las cuentas de usuario y de sus mecanismos de autenticación.
 */
@Document(collection = "usuarios")
public abstract class Usuario {

    /** Versión del documento para control optimista de concurrencia. */
    @Version
    Long version;

    /** Identificador persistente de la cuenta. */
    @Id
    private String id;

    /** Nombre de la persona titular. */
    @NotBlank
    private String nombre;

    /** Apellidos de la persona titular. */
    @NotBlank
    private String apellidos;

    /** Dirección de correo única usada para identificar la cuenta. */
    @NotBlank
    @Email
    @Indexed(unique = true)
    private String email;

    /** Contraseña almacenada por la aplicación. */
    @NotBlank
    private String contrasena;

    /** Número de teléfono de contacto. */
    private String telefono;

    /** URL o referencia de la imagen de perfil. */
    private String imagenPerfil;

    /** Rol asignado a la cuenta. */
    @NotNull
    private Rol rol;

    /** Indica si la cuenta está activa. */
    private boolean activo;
    /** Indica si la cuenta está bloqueada. */
    private boolean bloqueado;

    /** Fecha de creación registrada automáticamente. */
    @CreatedDate
    private LocalDateTime fechaAlta;

    /** Fecha de última modificación registrada automáticamente. */
    @LastModifiedDate
    private LocalDateTime fechaModificacion;

    /** Token temporal para recuperar la contraseña. */
    @Indexed
    private String tokenRecuperacionContrasena;
    /** Fecha y hora en que deja de ser válido el token de recuperación. */
    private LocalDateTime fechaExpiracionTokenRecuperacion;

    /** Fecha en que debe renovarse la contraseña. */
    private LocalDateTime fechaCambioContrasena = LocalDateTime.now().plusDays(30);
    /** Historial de contraseñas anteriores almacenadas por la aplicación. */
    private List<String> historialContrasenas = new ArrayList<>();

    /** Indica si la configuración MFA de la cuenta está completa. */
    private boolean mfaConfigurado = false;
    /** Indica si está activo el segundo factor del cliente. */
    private boolean is2faActivoCliente = false;
    /** Indica si está activo el tercer factor del cliente. */
    private boolean is3faActivoCliente = false;

    /** Secreto TOTP cifrado, cuando la cuenta tiene ese factor configurado. */
    private String totpSecretCifrado;
    /** Códigos de respaldo almacenados como hashes. */
    private List<String> codigosRespaldoHasheados = new ArrayList<>();

    /** Hash del código OTP enviado por correo. */
    private String emailOtpHash;
    /** Fecha y hora de expiración del código OTP de correo. */
    private LocalDateTime emailOtpExpiracion;
    /** Hash del token temporal para configurar MFA. */
    private String mfaSetupTokenHash;
    /** Fecha y hora de expiración del token de configuración MFA. */
    private LocalDateTime mfaSetupTokenExpiracion;


    /** Constructor para que las subclases creen cuentas con valores iniciales comunes. */
    protected Usuario() {
        this(null, false);
    }

    /** Inicializa el rol y el estado propios del tipo concreto de cuenta. */
    protected Usuario(Rol rol, boolean activo) {
        this.rol = rol;
        this.activo = activo;
        this.bloqueado = false;
        this.fechaCambioContrasena = LocalDateTime.now().plusDays(30);
        this.historialContrasenas = new ArrayList<>();
    }
    
    /**
     * Inicializa los datos personales comunes de una cuenta.
     *
     * @param nombre nombre de la persona titular
     * @param apellidos apellidos de la persona titular
     * @param email dirección de correo
     * @param contrasena contraseña
     * @param telefono teléfono de contacto
     * @param imagenPerfil URL o referencia de la imagen de perfil
     */
    protected Usuario(String nombre, String apellidos, String email, 
                      String contrasena, String telefono, String imagenPerfil) {
        this(nombre, apellidos, email, contrasena, telefono, imagenPerfil, null, false);
    }

    /**
     * Inicializa los datos personales y el rol de una cuenta.
     *
     * @param nombre nombre de la persona titular
     * @param apellidos apellidos de la persona titular
     * @param email dirección de correo
     * @param contrasena contraseña
     * @param telefono teléfono de contacto
     * @param imagenPerfil URL o referencia de la imagen de perfil
     * @param rol rol asignado a la cuenta
     * @param activo estado inicial de la cuenta
     */
    protected Usuario(String nombre, String apellidos, String email,
                      String contrasena, String telefono, String imagenPerfil,
                      Rol rol, boolean activo) {
        this(rol, activo);
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.contrasena = contrasena;
        this.telefono = telefono;
        this.imagenPerfil = imagenPerfil;
    }

    /** @return identificador persistente */
    public String getId() { return id; }
    /** @param id identificador persistente */
    public void setId(String id) { this.id = id; }

    /** @return nombre de la persona titular */
    public String getNombre() { return nombre; }
    /** @param nombre nombre de la persona titular */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** @return apellidos de la persona titular */
    public String getApellidos() { return apellidos; }
    /** @param apellidos apellidos de la persona titular */
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    /** @return dirección de correo */
    public String getEmail() { return email; }
    /** @param email dirección de correo */
    public void setEmail(String email) { this.email = email; }

    /** @return contraseña almacenada */
    public String getContrasena() { return contrasena; }
    /** @param contrasena contraseña almacenada */
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    /** @return teléfono de contacto */
    public String getTelefono() { return telefono; }
    /** @param telefono teléfono de contacto */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /** @return URL o referencia de la imagen de perfil */
    public String getImagenPerfil() { return imagenPerfil; }
    /** @param imagenPerfil URL o referencia de la imagen de perfil */
    public void setImagenPerfil(String imagenPerfil) { this.imagenPerfil = imagenPerfil; }

    /** @return rol asignado */
    public Rol getRol() { return rol; }

    /** @return {@code true} si la cuenta está activa */
    public boolean isActivo() { return activo; }
    /** @param activo nuevo estado de actividad */
    public void setActivo(boolean activo) { this.activo = activo; }

    /** @return {@code true} si la cuenta está bloqueada */
    public boolean isBloqueado() { return bloqueado; }
    /** @param bloqueado nuevo estado de bloqueo */
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    /** @return fecha de alta */
    public LocalDateTime getFechaAlta() { return fechaAlta; }
    /** @param fechaAlta fecha de alta */
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }

    /** @return fecha de última modificación */
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    /** @param fechaModificacion fecha de última modificación */
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    /** @return fecha en que debe cambiarse la contraseña */
    public LocalDateTime getFechaCambioContrasena() { return fechaCambioContrasena; }
    /** @param fechaCambioContrasena fecha límite para cambiar la contraseña */
    public void setFechaCambioContrasena(LocalDateTime fechaCambioContrasena) { this.fechaCambioContrasena = fechaCambioContrasena;}

    /** @return token de recuperación de contraseña */
    public String getTokenRecuperacionContrasena() { return tokenRecuperacionContrasena; }
    /** @param tokenRecuperacionContrasena token de recuperación */
    public void setTokenRecuperacionContrasena(String tokenRecuperacionContrasena) { this.tokenRecuperacionContrasena = tokenRecuperacionContrasena; }

    /** @return fecha de expiración del token de recuperación */
    public LocalDateTime getFechaExpiracionTokenRecuperacion() { return fechaExpiracionTokenRecuperacion; }
    /** @param fechaExpiracionTokenRecuperacion fecha de expiración del token */
    public void setFechaExpiracionTokenRecuperacion(LocalDateTime fechaExpiracionTokenRecuperacion) { this.fechaExpiracionTokenRecuperacion = fechaExpiracionTokenRecuperacion; }

    /** @return historial de contraseñas */
    public List<String> getHistorialContrasenas() { return historialContrasenas; }
    /** @param historialContrasenas historial de contraseñas */
    public void setHistorialContrasenas(List<String> historialContrasenas) { this.historialContrasenas = historialContrasenas; }

    /** @return {@code true} si MFA está configurado */
    public boolean isMfaConfigurado() { return mfaConfigurado; }
    /** @param mfaConfigurado nuevo estado de configuración MFA */
    public void setMfaConfigurado(boolean mfaConfigurado) { this.mfaConfigurado = mfaConfigurado; }

    /** @return {@code true} si el segundo factor del cliente está activo */
    public boolean is2faActivoCliente() { return is2faActivoCliente; }
    /** @param is2faActivoCliente nuevo estado del segundo factor */
    public void setIs2faActivoCliente(boolean is2faActivoCliente) { this.is2faActivoCliente = is2faActivoCliente; }

    /** @return {@code true} si el tercer factor del cliente está activo */
    public boolean is3faActivoCliente() { return is3faActivoCliente; }
    /** @param is3faActivoCliente nuevo estado del tercer factor */
    public void setIs3faActivoCliente(boolean is3faActivoCliente) {this.is3faActivoCliente = is3faActivoCliente; }

    /** @return secreto TOTP cifrado */
    public String getTotpSecretCifrado() { return totpSecretCifrado; }
    /** @param totpSecretCifrado secreto TOTP cifrado */
    public void setTotpSecretCifrado(String totpSecretCifrado) { this.totpSecretCifrado = totpSecretCifrado; }

    /** @return códigos de respaldo almacenados como hashes */
    public List<String> getCodigosRespaldoHasheados() { return codigosRespaldoHasheados; }
    /** @param codigosRespaldoHasheados códigos de respaldo almacenados como hashes */
    public void setCodigosRespaldoHasheados(List<String> codigosRespaldoHasheados) { this.codigosRespaldoHasheados = codigosRespaldoHasheados; }

    /** @return hash del OTP enviado por correo */
    public String getEmailOtpHash() { return emailOtpHash; }
    /** @param emailOtpHash hash del OTP enviado por correo */
    public void setEmailOtpHash(String emailOtpHash) { this.emailOtpHash = emailOtpHash; }

    /** @return fecha de expiración del OTP de correo */
    public LocalDateTime getEmailOtpExpiracion() { return emailOtpExpiracion; }
    /** @param emailOtpExpiracion fecha de expiración del OTP */
    public void setEmailOtpExpiracion(LocalDateTime emailOtpExpiracion) { this.emailOtpExpiracion = emailOtpExpiracion; }

    /** @return hash del token de configuración MFA */
    public String getMfaSetupTokenHash() { return mfaSetupTokenHash; }
    /** @param mfaSetupTokenHash hash del token de configuración MFA */
    public void setMfaSetupTokenHash(String mfaSetupTokenHash) { this.mfaSetupTokenHash = mfaSetupTokenHash; }

    /** @return fecha de expiración del token de configuración MFA */
    public LocalDateTime getMfaSetupTokenExpiracion() { return mfaSetupTokenExpiracion; }
    /** @param mfaSetupTokenExpiracion fecha de expiración del token */
    public void setMfaSetupTokenExpiracion(LocalDateTime mfaSetupTokenExpiracion) {
        this.mfaSetupTokenExpiracion = mfaSetupTokenExpiracion;
    }
}
    /** Fecha de última modificación registrada automáticamente. */
    /** Token temporal para recuperar la contraseña. */
