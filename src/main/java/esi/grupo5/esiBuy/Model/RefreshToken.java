package esi.grupo5.esiBuy.Model;

import java.time.LocalDateTime;


import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "refresh_tokens")
public class RefreshToken {
    @Version
    Long version;

    @Id
    private String id;
    private String token;
    
    @DBRef
    private Usuario uusuario;
    
    private LocalDateTime fechaExpiracion;

    public RefreshToken() {
        //Empty constructor for manipulation
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Usuario getUsuario() { return uusuario; }
    public void setUsuario(Usuario uusuario) { this.uusuario = uusuario; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
}