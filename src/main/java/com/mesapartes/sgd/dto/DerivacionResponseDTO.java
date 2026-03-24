package com.mesapartes.sgd.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DerivacionResponseDTO {

    private UUID id;
    private String numeroTramite;
    private String areaOrigen;
    private String areaDestino;
    private String motivo;
    private String usuarioResponsable;
    private LocalDateTime fechaDerivacion;
}