package com.mesapartes.sgd.dto;

import com.mesapartes.sgd.entity.EstadoDocumento;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class HistorialResponseDTO {

    private UUID id;
    private String numeroTramite;
    private EstadoDocumento estado;
    private String observacion;
    private String usuarioResponsable;
    private LocalDateTime fechaCambio;
}