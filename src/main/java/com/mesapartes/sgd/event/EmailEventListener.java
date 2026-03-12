package com.mesapartes.sgd.event;

import com.mesapartes.sgd.dto.DocumentoRequestDTO;
import com.mesapartes.sgd.entity.ContactoNotificacion;
import com.mesapartes.sgd.entity.PersonaJuridica;
import com.mesapartes.sgd.entity.PersonaNatural;
import com.mesapartes.sgd.repository.ContactoNotificacionRepository;
import com.mesapartes.sgd.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final EmailService emailService;
    private final ContactoNotificacionRepository contactoRepo;

    @EventListener
    @Async("taskExecutor")
    public void onDocumentoRegistrado(DocumentoRegistradoEvent event) {
        DocumentoRequestDTO req = event.getRequest();
        var doc = event.getDocumento();
        String tipo = req.getTipoPersona() != null ? req.getTipoPersona().toUpperCase() : "NATURAL";

        if ("NATURAL".equals(tipo)) {
            if (req.getEmailRemitente() != null && !req.getEmailRemitente().isEmpty()) {
                emailService.enviarConfirmacionExpediente(
                        req.getEmailRemitente(),
                        req.getRemitente(),
                        doc.getNumeroTramite(),
                        req.getAsunto(),
                        req.getTipoDocumento(),
                        req.getNumeroFolios()
                );
            }
            if (req.getEmailNotificacionAdicional() != null && !req.getEmailNotificacionAdicional().isEmpty()) {
                emailService.enviarConfirmacionExpediente(
                        req.getEmailNotificacionAdicional(),
                        req.getRemitente(),
                        doc.getNumeroTramite(),
                        req.getAsunto(),
                        req.getTipoDocumento(),
                        req.getNumeroFolios()
                );
            }

        } else if ("JURIDICA".equals(tipo)) {
            if (req.getEmailRemitente() != null && !req.getEmailRemitente().isEmpty()) {
                emailService.enviarConfirmacionExpediente(
                        req.getEmailRemitente(),
                        req.getRemitente(),
                        doc.getNumeroTramite(),
                        req.getAsunto(),
                        req.getTipoDocumento(),
                        req.getNumeroFolios()
                );
            }
            if (req.getContactosNotificacionIds() != null) {
                for (UUID contactoId : req.getContactosNotificacionIds()) {
                    contactoRepo.findById(contactoId).ifPresent(contacto -> {
                        if (contacto.isActivo()) {
                            emailService.enviarConfirmacionExpediente(
                                    contacto.getEmail(),
                                    req.getRemitente(),
                                    doc.getNumeroTramite(),
                                    req.getAsunto(),
                                    req.getTipoDocumento(),
                                    req.getNumeroFolios()
                            );
                        }
                    });
                }
            }
        }
    }

    @EventListener
    @Async("taskExecutor")
    public void onCambioEstado(CambioEstadoDocumentoEvent event) {
        var doc = event.getDocumento();
        var cambio = event.getCambio();
        if (doc.getEmailRemitente() != null && !doc.getEmailRemitente().isEmpty()) {
            emailService.enviarCambioEstado(
                    doc.getEmailRemitente(),
                    doc.getNumeroTramite(),
                    cambio.getEstado().name(),
                    cambio.getObservacion()
            );
        }
    }

    @EventListener
    @Async("taskExecutor")
    public void onCodigoVerificacion(CodigoVerificacionEvent event) {
        if (event.getNatural() != null) {
            PersonaNatural p = event.getNatural();
            emailService.enviarCodigoVerificacion(
                    p.getEmail(),
                    p.getNombres() + " " + p.getApellidoPaterno(),
                    p.getCodigoVerificacion()
            );
        } else if (event.getJuridica() != null) {
            PersonaJuridica j = event.getJuridica();
            emailService.enviarCodigoVerificacion(
                    j.getEmailRepresentante(),
                    j.getRazonSocial(),
                    j.getCodigoVerificacion()
            );
        }
    }
}