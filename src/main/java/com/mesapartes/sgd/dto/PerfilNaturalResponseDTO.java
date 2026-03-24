package com.mesapartes.sgd.dto;

import com.mesapartes.sgd.entity.PreguntaSeguridad;
import com.mesapartes.sgd.entity.TipoDocumento;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PerfilNaturalResponseDTO {

    private UUID id;

    // Solo lectura
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String departamento;
    private String provincia;
    private String distrito;

    // Editables
    private String direccion;
    private String telefono;
    private String email;
    private PreguntaSeguridad preguntaSeguridad;
    private String descripcionPregunta;

    private boolean afiliadoBuzon;
    private LocalDateTime fechaCreacion;
}