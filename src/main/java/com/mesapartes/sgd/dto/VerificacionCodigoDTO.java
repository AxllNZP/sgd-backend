package com.mesapartes.sgd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerificacionCodigoDTO {

    // El frontend sabe si fue natural o jurídica, lo manda aquí
    @NotBlank(message = "El tipo de persona es obligatorio")
    private String tipoPersna; // "NATURAL" o "JURIDICA"

    // Para NATURAL: número de documento. Para JURIDICA: RUC
    @NotBlank(message = "El identificador es obligatorio")
    private String identificador;

    @NotBlank(message = "El código es obligatorio")
    private String codigo;
}