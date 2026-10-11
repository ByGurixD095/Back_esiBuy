package esi.grupo5.esiBuy.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/** Estado persistente de intentos fallidos y bloqueos temporales por dirección IP. */
@Document(collection = "login_attempts")
public class LoginAttemptState {

    /** Identificador del documento. */
    @Id
    private String id;

    /** Dirección IP única a la que corresponde el estado de intentos. */
    @Indexed(unique = true)
    private String ipAddress;

    /** Fallos acumulados desde el último bloqueo. */
    private int failedAttempts;
    /** Nivel usado para calcular la duración progresiva de los bloqueos. */
    private int banLevel;
    /** Instante hasta el que se bloquean nuevos intentos, si existe. */
    private LocalDateTime bannedUntil;
    /** Instante del último fallo de autenticación. */
    private LocalDateTime lastFailureAt;
    /** Instante de la última actualización del estado. */
    private LocalDateTime updatedAt;

    /** Constructor requerido por MongoDB para materializar el documento. */
    public LoginAttemptState() {
    }

    /**
     * Crea el estado inicial asociado a una dirección IP.
     *
     * @param ipAddress dirección IP que se va a controlar
     */
    public LoginAttemptState(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /** @return identificador persistido */
    public String getId() {
        return id;
    }

    /** @param id identificador persistido */
    public void setId(String id) {
        this.id = id;
    }

    /** @return dirección IP controlada */
    public String getIpAddress() {
        return ipAddress;
    }

    /** @param ipAddress dirección IP controlada */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /** @return número de fallos desde el último bloqueo */
    public int getFailedAttempts() {
        return failedAttempts;
    }

    /** @param failedAttempts nuevo número de fallos */
    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    /** @return nivel actual de bloqueo progresivo */
    public int getBanLevel() {
        return banLevel;
    }

    /** @param banLevel nuevo nivel de bloqueo */
    public void setBanLevel(int banLevel) {
        this.banLevel = banLevel;
    }

    /** @return instante de fin del bloqueo o {@code null} si no está bloqueada */
    public LocalDateTime getBannedUntil() {
        return bannedUntil;
    }

    /** @param bannedUntil instante de fin del bloqueo */
    public void setBannedUntil(LocalDateTime bannedUntil) {
        this.bannedUntil = bannedUntil;
    }

    /** @return instante del último fallo */
    public LocalDateTime getLastFailureAt() {
        return lastFailureAt;
    }

    /** @param lastFailureAt instante del último fallo */
    public void setLastFailureAt(LocalDateTime lastFailureAt) {
        this.lastFailureAt = lastFailureAt;
    }

    /** @return instante de actualización del documento */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** @param updatedAt instante de actualización */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}