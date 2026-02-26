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
                """.formatted(numeroTramite, nuevoEstado, observacion != null ? observacion : "Sin observación");

        enviarEmail(destinatario, asuntoEmail, contenido);
    }

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
}