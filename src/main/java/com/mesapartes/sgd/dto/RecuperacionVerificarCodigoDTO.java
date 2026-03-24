package com.mesapartes.sgd.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecuperacionVerificarCodigoDTO {

    @NotBlank(message = "El tipo de persona es obligatorio")
    private String tipoPersna;

    @NotBlank(message = "El identificador es obligatorio")
    private String identificador;

    @NotBlank(message = "El código es obligatorio")
    private String codigo;
}