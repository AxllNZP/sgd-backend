package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String mailFrom;

    // ===== CÓDIGO DE VERIFICACIÓN AL REGISTRARSE =====
    @Override
    public void enviarCodigoVerificacion(String destinatario, String nombreOEmpresa, String codigo) {
        String asuntoEmail = "Activación de cuenta – Sistema de Mesa de Partes Digital";
        String contenido = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2 style="color: #1a5276;">Mesa de Partes Virtual</h2>
                    <p>Estimado/a <b>%s</b>,</p>
                    <p>Su registro fue recibido. Para activar su cuenta ingrese el siguiente código:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <span style="font-size: 36px; font-weight: bold; letter-spacing: 10px;
                                     background: #eaf4fb; padding: 16px 32px; border-radius: 8px;
                                     color: #1a5276;">%s</span>
                    </div>
                    <p style="color: #888;">Este código tiene una validez de <b>10 minutos</b>.</p>
                    <p>Si no solicitó este registro, ignore este mensaje.</p>
                    <hr>
                    <p><i>Sistema de Gestión Documental – Mesa de Partes Digital</i></p>
                </div>
                """.formatted(nombreOEmpresa, codigo);

        enviarEmail(destinatario, asuntoEmail, contenido);
    }

    // ===== CÓDIGO DE RECUPERACIÓN DE CONTRASEÑA =====
    @Override
    public void enviarCodigoRecuperacion(String destinatario, String nombreOEmpresa, String codigo) {
        String asuntoEmail = "Recuperación de contraseña – Sistema de Mesa de Partes Digital";
        String contenido = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2 style="color: #922b21;">Mesa de Partes Virtual</h2>
                    <p>Estimado/a <b>%s</b>,</p>
                    <p>Recibimos una solicitud para restablecer la contraseña de su cuenta.</p>
                    <p>Ingrese el siguiente código para continuar:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <span style="font-size: 36px; font-weight: bold; letter-spacing: 10px;
                                     background: #fdedec; padding: 16px 32px; border-radius: 8px;
                                     color: #922b21;">%s</span>
                    </div>
                    <p style="color: #888;">Este código tiene una validez de <b>10 minutos</b>.</p>
                    <p>Si no solicitó este cambio, ignore este mensaje. Su contraseña no será modificada.</p>
                    <hr>
                    <p><i>Sistema de Gestión Documental – Mesa de Partes Digital</i></p>
                </div>
                """.formatted(nombreOEmpresa, codigo);

        enviarEmail(destinatario, asuntoEmail, contenido);
    }

    // ===== CONFIRMACIÓN DE REGISTRO DE DOCUMENTO =====
    @Override
    public void enviarConfirmacionRegistro(String destinatario, String numeroTramite,
                                           String asunto, String remitente) {
        String asuntoEmail = "Confirmación de registro - " + numeroTramite;
        String contenido = """
                <h2>Mesa de Partes Virtual</h2>
                <p>Estimado/a <b>%s</b>,</p>
                <p>Su documento ha sido registrado exitosamente.</p>
                <br>
                <table border="1" cellpadding="8">
                    <tr><td><b>Número de Trámite</b></td><td>%s</td></tr>
                    <tr><td><b>Asunto</b></td><td>%s</td></tr>
                    <tr><td><b>Estado</b></td><td>RECIBIDO</td></tr>
                </table>
                <br>
                <p>Puede usar su número de trámite para hacer seguimiento de su documento.</p>
                <p><i>Sistema de Gestión Documental</i></p>
                """.formatted(remitente, numeroTramite, asunto);

        enviarEmail(destinatario, asuntoEmail, contenido);
    }

    // ===== CAMBIO DE ESTADO =====
    @Override
    public void enviarCambioEstado(String destinatario, String numeroTramite,
                                   String nuevoEstado, String observacion) {
        String asuntoEmail = "Actualización de estado - " + numeroTramite;
        String contenido = """
                <h2>Mesa de Partes Virtual</h2>
                <p>Su documento ha sido actualizado.</p>
                <br>
                <table border="1" cellpadding="8">
                    <tr><td><b>Número de Trámite</b></td><td>%s</td></tr>
                    <tr><td><b>Nuevo Estado</b></td><td>%s</td></tr>
                    <tr><td><b>Observación</b></td><td>%s</td></tr>
                </table>
                <br>
                <p><i>Sistema de Gestión Documental</i></p>
                """.formatted(numeroTramite, nuevoEstado,
                observacion != null ? observacion : "Sin observación");

        enviarEmail(destinatario, asuntoEmail, contenido);
    }

    // ===== RESPUESTA FORMAL =====
    @Override
    public void enviarRespuestaFormal(String destinatario, String numeroTramite,
                                      String remitente, String contenido) {
        String asuntoEmail = "Respuesta oficial - " + numeroTramite;
        String cuerpo = """
            <h2>Mesa de Partes Virtual</h2>
            <p>Estimado/a <b>%s</b>,</p>
            <p>Le informamos que su trámite <b>%s</b> ha recibido una respuesta oficial:</p>
            <br>
            <div style="border-left: 4px solid #007bff; padding-left: 16px;">
                <p>%s</p>
            </div>
            <br>
            <p><i>Sistema de Gestión Documental</i></p>
            """.formatted(remitente, numeroTramite, contenido);

        enviarEmail(destinatario, asuntoEmail, cuerpo);
    }

    // ===== CONFIRMACIÓN DE EXPEDIENTE REGISTRADO =====
    @Override
    public void enviarConfirmacionExpediente(String destinatario, String nombreRemitente,
                                             String numeroTramite, String asunto,
                                             String tipoDocumento, Integer numeroFolios) {
        String asuntoEmail = "Confirmación de recepción de expediente – " + numeroTramite;
        String contenido = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2 style="color: #1a5276;">Mesa de Partes Virtual</h2>
                    <p>Estimado/a <b>%s</b>,</p>
                    <p>Su documento ha sido recibido y registrado exitosamente en nuestro sistema.</p>
                    <br>
                    <table border="1" cellpadding="10" style="border-collapse: collapse; width: 100%%;">
                        <tr style="background-color: #eaf4fb;">
                            <td><b>N° de Expediente</b></td>
                            <td><b>%s</b></td>
                        </tr>
                        <tr>
                            <td><b>Tipo de Documento</b></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><b>Asunto</b></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><b>N° de Folios</b></td>
                            <td>%d</td>
                        </tr>
                        <tr>
                            <td><b>Estado</b></td>
                            <td>RECIBIDO</td>
                        </tr>
                    </table>
                    <br>
                    <p>Guarde su número de expediente para realizar el seguimiento de su trámite.</p>
                    <hr>
                    <p><i>Sistema de Gestión Documental – Mesa de Partes Digital</i></p>
                </div>
                """.formatted(nombreRemitente, numeroTramite, tipoDocumento, asunto, numeroFolios);

        enviarEmail(destinatario, asuntoEmail, contenido);
    }

    // ===== MÉTODO INTERNO =====
    private void enviarEmail(String destinatario, String asunto, String contenido) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenido, true);
            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar email: " + e.getMessage());
        }
    }
}