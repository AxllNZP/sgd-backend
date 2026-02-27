package com.mesapartes.sgd.service;

public interface EmailService {

    void enviarConfirmacionRegistro(String destinatario, String numeroTramite,
                                    String asunto, String remitente);

    void enviarCambioEstado(String destinatario, String numeroTramite,
                            String nuevoEstado, String observacion);

    void enviarRespuestaFormal(String destinatario, String numeroTramite,
                               String remitente, String contenido);

    // Envía código al registrarse (activación de cuenta)
    void enviarCodigoVerificacion(String destinatario, String nombreOEmpresa, String codigo);

    // Envía código para recuperar contraseña
    void enviarCodigoRecuperacion(String destinatario, String nombreOEmpresa, String codigo);

    // Envía confirmación de expediente registrado con número de trámite
    void enviarConfirmacionExpediente(String destinatario, String nombreRemitente,
                                      String numeroTramite, String asunto,
                                      String tipoDocumento, Integer numeroFolios);
}