package com.mesapartes.sgd.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class AreaResponseDTO {

    private UUID id;
    private String nombre;
    private String descripcion;
    private boolean activa;
}