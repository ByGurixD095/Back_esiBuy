package esi.grupo5.esiBuy.Dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Resultado de autenticación, que puede contener tokens o indicar un paso MFA pendiente.
 *
 * @param accessToken token de acceso, nulo mientras quede una verificación pendiente
 * @param refreshToken token de renovación, nulo mientras quede una verificación pendiente
 * @param rol rol asociado a la cuenta
 * @param tipoCliente tipo de cliente cuando la cuenta corresponde a un cliente
 * @param mfaStatus estado del flujo de autenticación
 * @param email correo asociado al desafío MFA cuando corresponde
 * @param mfaSetupToken token temporal para configurar MFA cuando corresponde
 */
public record LoginResponseDTO(
        @JsonIgnore
        String accessToken,
        @JsonIgnore
        String refreshToken,
        String rol,
        String tipoCliente,
        String mfaStatus,
        String email,
        String mfaSetupToken) {

    /**
     * Crea una respuesta de autenticación satisfactoria sin datos adicionales de MFA.
     *
     * @param accessToken token de acceso
     * @param refreshToken token de renovación
     * @param rol rol de la cuenta
     * @param tipoCliente tipo de cliente, si corresponde
     */
    public LoginResponseDTO(String accessToken, String refreshToken, String rol, String tipoCliente) {
        this(accessToken, refreshToken, rol, tipoCliente, "SUCCESS", null, null);
    }

    /**
     * Crea una respuesta de autenticación con estado MFA y correo, sin token de configuración.
     *
     * @param accessToken token de acceso
     * @param refreshToken token de renovación
     * @param rol rol de la cuenta
     * @param tipoCliente tipo de cliente, si corresponde
     * @param mfaStatus estado del flujo MFA
     * @param email correo asociado al flujo
     */
    public LoginResponseDTO(String accessToken, String refreshToken, String rol, String tipoCliente,
                            String mfaStatus, String email) {
        this(accessToken, refreshToken, rol, tipoCliente, mfaStatus, email, null);
    }
}
