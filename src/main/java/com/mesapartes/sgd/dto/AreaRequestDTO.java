package com.mesapartes.sgd.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AreaRequestDTO {

    @NotBlank(message = "El nombre del área es obligatorio")
    private String nombre;

    private String descripcion;
}