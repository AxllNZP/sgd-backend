package com.mesapartes.sgd.dto;

import com.mesapartes.sgd.entity.EstadoDocumento;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DocumentoResponseDTO {

    private UUID id;
    private String numeroTramite;
    private String tipoPersona;
    private String remitente;
    private String dniRuc;
    private String asunto;
    private String tipoDocumento;
    private String numeroDocumentoRemitente;
    private Integer numeroFolios;

    // Archivo principal
    private String nombreArchivoOriginal;
    private String rutaArchivo;

    // Anexo
    private String nombreAnexoOriginal;
    private String rutaAnexo;

    private LocalDateTime fechaHoraRegistro;
    private EstadoDocumento estado;

    // Notificaciones
    private String emailRemitente;
    private String emailNotificacionAdicional;
    private String contactosNotificacionIds;

    // Área asignada
    private UUID areaId;
    private String areaNombre;
}