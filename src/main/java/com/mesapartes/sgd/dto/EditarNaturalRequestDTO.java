package com.mesapartes.sgd.dto;

import com.mesapartes.sgd.entity.PreguntaSeguridad;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EditarNaturalRequestDTO {

    // ===== DATOS EDITABLES =====
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    // ===== NOTIFICACIÓN Y SEGURIDAD =====
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene formato válido")
    private String email;

    @NotNull(message = "La pregunta de seguridad es obligatoria")
    private PreguntaSeguridad preguntaSeguridad;

    @NotBlank(message = "La respuesta de seguridad es obligatoria")
    private String respuestaSeguridad;
}