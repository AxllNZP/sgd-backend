package com.mesapartes.sgd.dto;

import com.mesapartes.sgd.entity.PreguntaSeguridad;
import com.mesapartes.sgd.entity.TipoDocumento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RegistroJuridicaRequestDTO {

    // ===== DATOS DE LA EMPRESA =====
    @NotBlank(message = "El RUC es obligatorio")
    @Size(min = 11, max = 11, message = "El RUC debe tener exactamente 11 dígitos")
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    private String razonSocial;

    // ===== CREDENCIALES =====
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    // ===== SEGURIDAD =====
    @NotNull(message = "La pregunta de seguridad es obligatoria")
    private PreguntaSeguridad preguntaSeguridad;

    @NotBlank(message = "La respuesta de seguridad es obligatoria")
    private String respuestaSeguridad;

    // ===== REPRESENTANTE LEGAL =====
    @NotNull(message = "El tipo de documento del representante es obligatorio")
    private TipoDocumento tipoDocRepresentante;

    @NotBlank(message = "El número de documento del representante es obligatorio")
    private String numDocRepresentante;

    @NotBlank(message = "Los nombres del representante son obligatorios")
    private String nombresRepresentante;

    @NotBlank(message = "El apellido paterno del representante es obligatorio")
    private String apellidoPaternoRepresentante;

    @NotBlank(message = "El apellido materno del representante es obligatorio")
    private String apellidoMaternoRepresentante;

    @NotBlank(message = "El email del representante es obligatorio")
    @Email(message = "El email del representante no tiene formato válido")
    private String emailRepresentante;

    // ===== UBIGEO DE LA EMPRESA =====
    @NotBlank(message = "El departamento es obligatorio")
    private String departamento;

    @NotBlank(message = "La provincia es obligatoria")
    private String provincia;

    @NotBlank(message = "El distrito es obligatorio")
    private String distrito;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    // ===== CONTACTOS DE NOTIFICACIÓN =====
    @Valid
    private List<ContactoNotificacionDTO> contactosNotificacion = new ArrayList<>();

    // ===== OPCIONES =====
    private boolean afiliadoBuzon = false;
}