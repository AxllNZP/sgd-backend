package com.mesapartes.sgd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecuperacionSolicitarDTO {

    // "NATURAL" o "JURIDICA"
    @NotBlank(message = "El tipo de persona es obligatorio")
    private String tipoPersna;

    // Para NATURAL: número de documento. Para JURIDICA: RUC
    @NotBlank(message = "El identificador es obligatorio")
    private String identificador;

    // Debe coincidir con el email registrado
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene formato válido")
    private String email;
}