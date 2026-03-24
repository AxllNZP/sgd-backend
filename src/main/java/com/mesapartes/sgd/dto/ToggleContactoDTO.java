package com.mesapartes.sgd.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ToggleContactoDTO {

    @NotNull(message = "El estado es obligatorio")
    private Boolean activo;
}