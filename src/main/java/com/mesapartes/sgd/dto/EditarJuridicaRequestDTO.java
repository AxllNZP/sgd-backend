package com.mesapartes.sgd.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EditarJuridicaRequestDTO {

    // ===== DATOS EDITABLES =====
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotBlank(message = "El departamento es obligatorio")
    private String departamento;

    @NotBlank(message = "La provincia es obligatoria")
    private String provincia;

    @NotBlank(message = "El distrito es obligatorio")
    private String distrito;
}