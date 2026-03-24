package com.mesapartes.sgd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistroResponseDTO {

    private String mensaje;

    // Identificador: para Natural = numeroDocumento, para Jurídica = ruc
    private String identificador;

    private String tipoPersna;

    // Indica al frontend que debe mostrar la pantalla de ingreso de código
    private boolean requiereVerificacion;
}