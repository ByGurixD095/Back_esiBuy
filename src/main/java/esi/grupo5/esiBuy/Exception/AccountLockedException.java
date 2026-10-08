package esi.grupo5.esiBuy.Exception;

/**
 * Excepción para cuando una cuenta está bloqueada por demasiados intentos fallidos
 */
public class AccountLockedException extends BusinessException {

    public AccountLockedException(String message) {
        super(message, 403, "ACCOUNT_LOCKED");
    }

    public AccountLockedException(String message, Throwable cause) {
        super(message, 403, "ACCOUNT_LOCKED", cause);
    }
}
