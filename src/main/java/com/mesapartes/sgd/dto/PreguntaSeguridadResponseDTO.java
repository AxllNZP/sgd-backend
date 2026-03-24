package com.mesapartes.sgd.dto;

import com.mesapartes.sgd.entity.PreguntaSeguridad;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PreguntaSeguridadResponseDTO {

    private PreguntaSeguridad pregunta;

    // La descripción legible para mostrar en pantalla
    // Ej: "¿Cuál es el nombre de tu primera mascota?"
    private String descripcion;
}