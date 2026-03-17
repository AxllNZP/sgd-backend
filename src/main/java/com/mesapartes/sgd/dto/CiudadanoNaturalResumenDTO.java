package com.mesapartes.sgd.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CiudadanoNaturalResumenDTO {
    private UUID id;
    private String tipoDocumento;       // "DNI" | "CARNET_EXTRANJERIA"
    private String numeroDocumento;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String email;
    private String telefono;
    private String departamento;
    private String provincia;
    private String distrito;
    private boolean activo;
    private boolean verificado;
    private boolean afiliadoBuzon;
    private LocalDateTime fechaCreacion;
}