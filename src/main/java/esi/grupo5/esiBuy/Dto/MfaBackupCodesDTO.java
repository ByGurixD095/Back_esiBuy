package esi.grupo5.esiBuy.Dto;

import java.util.List;

public record MfaBackupCodesDTO(
        List<String> backupCodes, 
        String mensaje
) {}
