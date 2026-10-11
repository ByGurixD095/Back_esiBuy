package esi.grupo5.esiBuy.Model.enums;

/** Roles de cuenta utilizados para identificar usuarios y autorizar operaciones. */
public enum Rol {
    /** Cuenta con permisos de administración de la plataforma. */
    ADMINISTRADOR,
    /** Cuenta que administra y publica productos. */
    VENDEDOR,
    /** Cuenta que compra productos en la plataforma. */
    CLIENTE
}