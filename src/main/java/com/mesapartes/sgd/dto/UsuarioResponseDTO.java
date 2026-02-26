package com.mesapartes.sgd.dto;

import com.mesapartes.sgd.entity.RolUsuario;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UsuarioResponseDTO {

    private UUID id;
    private String nombre;
    private String email;
    private RolUsuario rol;
    private boolean activo;
    private LocalDateTime fechaCreacion;
}