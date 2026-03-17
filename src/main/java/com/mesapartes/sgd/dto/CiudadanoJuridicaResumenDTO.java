package com.mesapartes.sgd.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CiudadanoJuridicaResumenDTO {
    private UUID id;
    private String ruc;
    private String razonSocial;
    private String emailRepresentante;
    private String nombresRepresentante;
    private String apellidoPaternoRepresentante;
    private String apellidoMaternoRepresentante;
    private String telefono;
    private String departamento;
    private String provincia;
    private String distrito;
    private boolean activo;
    private boolean verificado;
    private boolean afiliadoBuzon;
    private int totalContactos;         // útil para la tabla del admin
    private LocalDateTime fechaCreacion;
}