package com.mesapartes.sgd.service;

import org.springframework.security.access.prepost.PreAuthorize;

public interface EmailService {

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    void enviarConfirmacionRegistro(String destinatario, String numeroTramite,
                                    String asunto, String remitente);

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    void enviarCambioEstado(String destinatario, String numeroTramite,
                            String nuevoEstado, String observacion);

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    void enviarRespuestaFormal(String destinatario, String numeroTramite,
                               String remitente, String contenido);

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    void enviarCodigoVerificacion(String destinatario, String nombreOEmpresa, String codigo);

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    void enviarCodigoRecuperacion(String destinatario, String nombreOEmpresa, String codigo);

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    void enviarConfirmacionExpediente(String destinatario, String nombreRemitente,
                                      String numeroTramite, String asunto,
                                      String tipoDocumento, Integer numeroFolios);
}