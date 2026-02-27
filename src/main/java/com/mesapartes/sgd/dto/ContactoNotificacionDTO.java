package com.mesapartes.sgd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactoNotificacionDTO {

    @NotBlank(message = "El nombre del contacto es obligatorio")
    private String nombres;

    @NotBlank(message = "El email del contacto es obligatorio")
    @Email(message = "El email del contacto no tiene formato válido")
    private String email;

    private boolean activo = true;
}