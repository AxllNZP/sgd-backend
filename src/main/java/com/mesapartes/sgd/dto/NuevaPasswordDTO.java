package com.mesapartes.sgd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NuevaPasswordDTO {

    @NotBlank(message = "El tipo de persona es obligatorio")
    private String tipoPersna;

    @NotBlank(message = "El identificador es obligatorio")
    private String identificador;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String nuevaPassword;

    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmarPassword;
}