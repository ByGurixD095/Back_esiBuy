package esi.grupo5.esiBuy.Model;

import java.time.LocalDateTime;


import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/** Registro persistente que vincula un JWT de refresco con su cuenta y expiración. */
@Document(collection = "refresh_tokens")
public class RefreshToken {
    /** Versión persistida usada para control optimista de concurrencia. */
    @Version
    Long version;

    /** Identificador del registro. */
    @Id
    private String id;
    /** JWT utilizado para renovar el token de acceso. */
    private String token;
    
    /** Cuenta propietaria del token de refresco. */
    @DBRef
    private Usuario uusuario;
    
    /** Fecha y hora de expiración del token. */
    private LocalDateTime fechaExpiracion;

    /** Constructor requerido por MongoDB para materializar el documento. */
    public RefreshToken() {
    }
    /** @return identificador persistido */
    public String getId() { return id; }
    /** @param id identificador persistido */
    public void setId(String id) { this.id = id; }
    /** @return JWT de refresco */
    public String getToken() { return token; }
    /** @param token JWT de refresco */
    public void setToken(String token) { this.token = token; }
    /** @return cuenta propietaria */
    public Usuario getUsuario() { return uusuario; }
    /** @param uusuario cuenta propietaria */
    public void setUsuario(Usuario uusuario) { this.uusuario = uusuario; }
    /** @return fecha y hora de expiración */
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    /** @param fechaExpiracion fecha y hora de expiración */
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
}