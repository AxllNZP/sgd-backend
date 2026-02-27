package com.mesapartes.sgd.entity;

public enum PreguntaSeguridad {
    NOMBRE_MASCOTA("¿Cuál es el nombre de tu primera mascota?"),
    CIUDAD_NACIMIENTO("¿En qué ciudad naciste?"),
    NOMBRE_COLEGIO("¿Cuál es el nombre de tu colegio de primaria?"),
    NOMBRE_MADRE("¿Cuál es el primer nombre de tu madre?"),
    PELICULA_FAVORITA("¿Cuál es tu película favorita?"),
    APODO_INFANCIA("¿Cuál era tu apodo de infancia?");

    private final String descripcion;

    PreguntaSeguridad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}