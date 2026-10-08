package esi.grupo5.esiBuy.Dto;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        String rol,
        String tipoCliente,
        String mfaStatus,
        String email,
        String mfaSetupToken) {

    public LoginResponseDTO(String accessToken, String refreshToken, String rol, String tipoCliente) {
        this(accessToken, refreshToken, rol, tipoCliente, "SUCCESS", null, null);
    }

    public LoginResponseDTO(String accessToken, String refreshToken, String rol, String tipoCliente,
                            String mfaStatus, String email) {
        this(accessToken, refreshToken, rol, tipoCliente, mfaStatus, email, null);
    }
}
