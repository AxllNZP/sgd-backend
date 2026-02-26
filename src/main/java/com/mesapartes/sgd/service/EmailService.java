package com.mesapartes.sgd.service;

public interface EmailService {

    void enviarConfirmacionRegistro(String destinatario, String numeroTramite, String asunto, String remitente);

    void enviarCambioEstado(String destinatario, String numeroTramite, String nuevoEstado, String observacion);

    void enviarRespuestaFormal(String destinatario, String numeroTramite,
                               String remitente, String contenido);
}