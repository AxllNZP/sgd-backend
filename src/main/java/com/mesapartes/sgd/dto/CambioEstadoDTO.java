package com.mesapartes.sgd.dto;

import com.mesapartes.sgd.entity.EstadoDocumento;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambioEstadoDTO {

    @NotNull(message = "El estado es obligatorio")
    private EstadoDocumento estado;

    private String observacion;

    private String usuarioResponsable;
}