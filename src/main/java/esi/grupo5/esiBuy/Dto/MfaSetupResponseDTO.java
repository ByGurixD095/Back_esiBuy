package esi.grupo5.esiBuy.Dto;

/**
 * Material necesario para registrar MFA en una aplicación autenticadora.
 *
 * @param qrCodeUri imagen QR codificada como URI de datos
 * @param manualSecretKey secreto que puede introducirse manualmente en la aplicación
 */
public record MfaSetupResponseDTO(String qrCodeUri, String manualSecretKey) {

}
