package com.mesapartes.sgd.dto;

import lombok.Data;

@Data
public class EstadisticasCiudadanosDTO {
    private long totalNaturales;
    private long naturalesActivos;
    private long naturalesInactivos;
    private long naturalesVerificados;
    private long totalJuridicas;
    private long juridicasActivas;
    private long juridicasInactivas;
    private long juridicasVerificadas;
    private long totalCiudadanos;       // suma de ambos
}