package esi.grupo5.esiBuy.Exception;

/** Indica que una cuenta está bloqueada por intentos de acceso fallidos. */
public class AccountLockedException extends BusinessException {

    /**
     * Crea el error de cuenta bloqueada.
     *
     * @param message motivo del bloqueo
     */
    public AccountLockedException(String message) {
        super(message, 403, "ACCOUNT_LOCKED");
    }

    /**
     * Crea el error de cuenta bloqueada conservando la causa.
     *
     * @param message motivo del bloqueo
     * @param cause causa original del error
     */
    public AccountLockedException(String message, Throwable cause) {
        super(message, 403, "ACCOUNT_LOCKED", cause);
    }
}
