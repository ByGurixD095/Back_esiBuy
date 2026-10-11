package esi.grupo5.esiBuy.Dto;


/**
 * Preferencias de segundo y tercer factor solicitadas para una cuenta de cliente.
 *
 * @param enable2fa indica si se activa el segundo factor
 * @param enable3fa indica si se activa el factor adicional por correo
 */
public record MfaConfigRequestDTO(
    boolean enable2fa,
    boolean enable3fa
) {

}
