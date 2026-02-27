package com.mesapartes.sgd.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DocumentoRequestDTO {

    // ===== DATOS DEL REMITENTE =====
    // Tipo de persona: "NATURAL" o "JURIDICA"
    @NotBlank(message = "El tipo de persona es obligatorio")
    private String tipoPersona;

    @NotBlank(message = "El nombre del remitente es obligatorio")
    private String remitente;

    @NotBlank(message = "El DNI o RUC es obligatorio")
    @Size(min = 8, max = 11, message = "El DNI o RUC debe tener entre 8 y 11 caracteres")
    private String dniRuc;

    // ===== DATOS DEL DOCUMENTO =====
    // Tipo de escrito: CARTA, OFICIO, SOLICITUD, etc.
    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento;

    // Número identificador del documento
    private String numeroDocumentoRemitente;

    // Número de folios (páginas)
    @NotNull(message = "El número de folios es obligatorio")
    @Min(value = 1, message = "El número de folios debe ser al menos 1")
    private Integer numeroFolios;

    // Asunto (máx 900 caracteres según el manual)
    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 900, message = "El asunto no puede superar los 900 caracteres")
    private String asunto;

    // ===== NOTIFICACIONES =====
    @Email(message = "El email del remitente no tiene formato válido")
    private String emailRemitente;

    // Email adicional para copia (solo Persona Natural)
    @Email(message = "El email adicional no tiene formato válido")
    private String emailNotificacionAdicional;

    // IDs de contactos activos seleccionados (solo Persona Jurídica)
    private List<UUID> contactosNotificacionIds;
}