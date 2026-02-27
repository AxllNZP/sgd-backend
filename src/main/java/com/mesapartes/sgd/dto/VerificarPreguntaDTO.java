package com.mesapartes.sgd.dto;

import com.mesapartes.sgd.entity.PreguntaSeguridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerificarPreguntaDTO {

    @NotBlank(message = "El tipo de persona es obligatorio")
    private String tipoPersna;

    @NotBlank(message = "El identificador es obligatorio")
    private String identificador;

    @NotNull(message = "La pregunta de seguridad es obligatoria")
    private PreguntaSeguridad preguntaSeguridad;

    @NotBlank(message = "La respuesta es obligatoria")
    private String respuesta;
}