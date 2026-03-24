package com.mesapartes.sgd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class DerivacionRequestDTO {

    @NotNull(message = "El área destino es obligatoria")
    private UUID areaDestinoId;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    @NotBlank(message = "El usuario responsable es obligatorio")
    private String usuarioResponsable;
}