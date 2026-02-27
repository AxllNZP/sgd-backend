package com.mesapartes.sgd.dto;

import com.mesapartes.sgd.entity.PreguntaSeguridad;
import com.mesapartes.sgd.entity.TipoDocumento;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistroNaturalRequestDTO {

    // ===== DOCUMENTO =====
    @NotNull(message = "El tipo de documento es obligatorio")
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 8, max = 12, message = "El número de documento debe tener entre 8 y 12 caracteres")
    private String numeroDocumento;

    // ===== DATOS PERSONALES =====
    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    private String apellidoMaterno;

    // ===== UBIGEO =====
    @NotBlank(message = "El departamento es obligatorio")
    private String departamento;

    @NotBlank(message = "La provincia es obligatoria")
    private String provincia;

    @NotBlank(message = "El distrito es obligatorio")
    private String distrito;

    // ===== CONTACTO =====
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    // ===== CREDENCIALES =====
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    // ===== SEGURIDAD =====
    @NotNull(message = "La pregunta de seguridad es obligatoria")
    private PreguntaSeguridad preguntaSeguridad;

    @NotBlank(message = "La respuesta de seguridad es obligatoria")
    private String respuestaSeguridad;

    // ===== OPCIONES =====
    private boolean afiliadoBuzon = false;
}