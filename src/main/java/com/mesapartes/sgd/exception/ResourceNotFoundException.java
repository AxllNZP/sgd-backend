package com.mesapartes.sgd.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String recurso, Object id) {
        super(String.format("%s no encontrado con identificador: %s", recurso, id));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}