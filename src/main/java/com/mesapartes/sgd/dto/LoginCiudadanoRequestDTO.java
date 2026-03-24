package com.mesapartes.sgd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginCiudadanoRequestDTO {

    // "NATURAL" o "JURIDICA"
    @NotBlank(message = "El tipo de persona es obligatorio")
    private String tipoPersna;

    // Para NATURAL: DNI o carné. Para JURIDICA: RUC
    @NotBlank(message = "El número de documento es obligatorio")
    private String identificador;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}