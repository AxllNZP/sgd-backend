package com.mesapartes.sgd.dto;

import com.mesapartes.sgd.entity.EstadoDocumento;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DocumentoResponseDTO {

    private UUID id;
    private String numeroTramite;
    private String remitente;
    private String dniRuc;
    private String asunto;
    private String tipoDocumento;
    private String rutaArchivo;
    private LocalDateTime fechaHoraRegistro;
    private EstadoDocumento estado;
    private String emailRemitente;
    private UUID areaId;
    private String areaNombre;
}