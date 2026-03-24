package com.mesapartes.sgd.dto;

import lombok.Data;

@Data
public class CiudadanoFiltroDTO {
    private String busqueda;    // nombre, doc, email, ruc, razón social
    private Boolean activo;     // null = todos | true = activos | false = inactivos
    private Boolean verificado; // null = todos
}