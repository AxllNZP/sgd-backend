package com.mesapartes.sgd.service;

import org.springframework.security.access.prepost.PreAuthorize;

public interface EmailService {

    void enviarConfirmacionRegistro(String destinatario, String numeroTramite,
                                    String asunto, String remitente);

    void enviarCambioEstado(String destinatario, String numeroTramite,
                            String nuevoEstado, String observacion);

    void enviarRespuestaFormal(String destinatario, String numeroTramite,
                               String remitente, String contenido);

    void enviarCodigoVerificacion(String destinatario, String nombreOEmpresa, String codigo);

    void enviarCodigoRecuperacion(String destinatario, String nombreOEmpresa, String codigo);

     void enviarConfirmacionExpediente(String destinatario, String nombreRemitente,
                                      String numeroTramite, String asunto,
                                      String tipoDocumento, Integer numeroFolios);
}