package esi.grupo5.esiBuy.Dto;


public record MfaConfigRequestDTO(
    boolean enable2fa,
    boolean enable3fa
) {

}
