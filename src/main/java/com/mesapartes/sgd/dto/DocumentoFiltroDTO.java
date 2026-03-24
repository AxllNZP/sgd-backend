package com.mesapartes.sgd.dto;

import com.mesapartes.sgd.entity.EstadoDocumento;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentoFiltroDTO {

    private String remitente;
    private String asunto;
    private EstadoDocumento estado;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
}