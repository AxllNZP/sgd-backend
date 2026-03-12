package com.mesapartes.sgd.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Credenciales incorrectas");
    }
}