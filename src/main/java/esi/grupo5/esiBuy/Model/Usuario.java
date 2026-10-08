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

@Document(collection = "usuarios")
public abstract class Usuario {

    @Version
    Long version;

    @Id
    private String id;

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellidos;

    @NotBlank
    @Email
    @Indexed(unique = true)
    private String email;

    @NotBlank
    private String contrasena;

    private String telefono;

    private String imagenPerfil;

    @NotNull
    private Rol rol;

    private boolean activo;
    private boolean bloqueado;
    private boolean eliminado;

    @CreatedDate
    private LocalDateTime fechaAlta;

    @LastModifiedDate
    private LocalDateTime fechaModificacion;

    @Indexed
    private String tokenRecuperacionContrasena;
    private LocalDateTime fechaExpiracionTokenRecuperacion;

    private LocalDateTime fechaCambioContrasena = LocalDateTime.now().plusDays(30);
    private List<String> historialContrasenas = new ArrayList<>();

    private boolean mfaConfigurado = false;
    private boolean is2faActivoCliente = false;
    private boolean is3faActivoCliente = false;

    private String totpSecretCifrado;
    private List<String> codigosRespaldoHasheados = new ArrayList<>();

    private String emailOtpHash;
    private LocalDateTime emailOtpExpiracion;
    private String mfaSetupTokenHash;
    private LocalDateTime mfaSetupTokenExpiracion;


    protected Usuario() {
        this.activo = false;
        this.bloqueado = false;
        this.fechaCambioContrasena = LocalDateTime.now().plusDays(30); //Sirve para llevar el conteo de los dias hasta 30(cambio de contraseña obligatorio)
        this.historialContrasenas = new ArrayList<>();
    }
    
    protected Usuario(String nombre, String apellidos, String email, 
                      String contrasena, String telefono, String imagenPerfil) {
        this();
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.contrasena = contrasena;
        this.telefono = telefono;
        this.imagenPerfil = imagenPerfil;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getImagenPerfil() { return imagenPerfil; }
    public void setImagenPerfil(String imagenPerfil) { this.imagenPerfil = imagenPerfil; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public LocalDateTime getFechaCambioContrasena() { return fechaCambioContrasena; }
    public void setFechaCambioContrasena(LocalDateTime fechaCambioContrasena) { this.fechaCambioContrasena = fechaCambioContrasena;}

    public String getTokenRecuperacionContrasena() { return tokenRecuperacionContrasena; }
    public void setTokenRecuperacionContrasena(String tokenRecuperacionContrasena) { this.tokenRecuperacionContrasena = tokenRecuperacionContrasena; }

    public LocalDateTime getFechaExpiracionTokenRecuperacion() { return fechaExpiracionTokenRecuperacion; }
    public void setFechaExpiracionTokenRecuperacion(LocalDateTime fechaExpiracionTokenRecuperacion) { this.fechaExpiracionTokenRecuperacion = fechaExpiracionTokenRecuperacion; }

    public List<String> getHistorialContrasenas() { return historialContrasenas; }
    public void setHistorialContrasenas(List<String> historialContrasenas) { this.historialContrasenas = historialContrasenas; }

    public boolean isMfaConfigurado() { return mfaConfigurado; }
    public void setMfaConfigurado(boolean mfaConfigurado) { this.mfaConfigurado = mfaConfigurado; }

    public boolean is2faActivoCliente() { return is2faActivoCliente; }
    public void setIs2faActivoCliente(boolean is2faActivoCliente) { this.is2faActivoCliente = is2faActivoCliente; }

    public boolean is3faActivoCliente() { return is3faActivoCliente; }
    public void setIs3faActivoCliente(boolean is3faActivoCliente) {this.is3faActivoCliente = is3faActivoCliente; }

    public String getTotpSecretCifrado() { return totpSecretCifrado; }
    public void setTotpSecretCifrado(String totpSecretCifrado) { this.totpSecretCifrado = totpSecretCifrado; }

    public List<String> getCodigosRespaldoHasheados() { return codigosRespaldoHasheados; }
    public void setCodigosRespaldoHasheados(List<String> codigosRespaldoHasheados) { this.codigosRespaldoHasheados = codigosRespaldoHasheados; }

    public String getEmailOtpHash() { return emailOtpHash; }
    public void setEmailOtpHash(String emailOtpHash) { this.emailOtpHash = emailOtpHash; }

    public LocalDateTime getEmailOtpExpiracion() { return emailOtpExpiracion; }
    public void setEmailOtpExpiracion(LocalDateTime emailOtpExpiracion) { this.emailOtpExpiracion = emailOtpExpiracion; }

    public String getMfaSetupTokenHash() { return mfaSetupTokenHash; }
    public void setMfaSetupTokenHash(String mfaSetupTokenHash) { this.mfaSetupTokenHash = mfaSetupTokenHash; }

    public LocalDateTime getMfaSetupTokenExpiracion() { return mfaSetupTokenExpiracion; }
    public void setMfaSetupTokenExpiracion(LocalDateTime mfaSetupTokenExpiracion) {
        this.mfaSetupTokenExpiracion = mfaSetupTokenExpiracion;
    }

    
}
