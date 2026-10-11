package esi.grupo5.esiBuy.Dto;

import java.util.List;

/**
 * Códigos de respaldo entregados al finalizar la configuración MFA.
 *
 * @param backupCodes códigos de un solo uso que debe guardar el usuario
 * @param mensaje texto informativo asociado a la operación
 */
public record MfaBackupCodesDTO(
        List<String> backupCodes, 
        String mensaje
) {}
