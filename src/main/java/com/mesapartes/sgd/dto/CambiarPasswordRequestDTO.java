package com.mesapartes.sgd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CambiarPasswordRequestDTO {

    // Tipo de persona: "NATURAL" o "JURIDICA"
    @NotBlank(message = "El tipo de persona es obligatorio")
    private String tipoPersna;

    // Para NATURAL: número de documento. Para JURIDICA: RUC
    @NotBlank(message = "El identificador es obligatorio")
    private String identificador;

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String passwordActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    private String nuevaPassword;

    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmarPassword;
}