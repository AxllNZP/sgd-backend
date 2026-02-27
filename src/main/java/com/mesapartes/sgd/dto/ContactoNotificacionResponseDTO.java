package com.mesapartes.sgd.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ContactoNotificacionResponseDTO {

    private UUID id;
    private String nombres;
    private String email;
    private boolean activo;
}