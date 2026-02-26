package com.mesapartes.sgd.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RespuestaResponseDTO {

    private UUID id;
    private String numeroTramite;
    private String contenido;
    private String usuarioResponsable;
    private LocalDateTime fechaRespuesta;
    private boolean enviadoPorEmail;
}