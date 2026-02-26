package com.mesapartes.sgd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DocumentoRequestDTO {

    @NotBlank(message = "El nombre del remitente es obligatorio")
    private String remitente;

    @NotBlank(message = "El DNI o RUC es obligatorio")
    @Size(min = 8, max = 11, message = "El DNI o RUC debe tener entre 8 y 11 caracteres")
    private String dniRuc;

    @NotBlank(message = "El asunto es obligatorio")
    private String asunto;

    private String tipoDocumento;

    @Email(message = "El email no tiene formato válido")
    private String emailRemitente;
}