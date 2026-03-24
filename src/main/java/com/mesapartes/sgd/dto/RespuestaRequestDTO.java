package com.mesapartes.sgd.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RespuestaRequestDTO {

    @NotBlank(message = "El contenido de la respuesta es obligatorio")
    private String contenido;

    @NotBlank(message = "El usuario responsable es obligatorio")
    private String usuarioResponsable;

    private boolean enviarEmail = true;
}