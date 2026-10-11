package esi.grupo5.esiBuy.Dto;

/**
 * Credenciales recibidas para iniciar sesión.
 *
 * @param username identificador de acceso, usado como correo electrónico
 * @param password contraseña proporcionada
 */
public record LoginRequestDTO(String username, String password) {

}
