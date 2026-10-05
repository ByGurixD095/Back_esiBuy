package esi.grupo5.esiBuy.Dto;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        String rol,
        String tipoCliente) {

}
