package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.RespuestaRequestDTO;
import com.mesapartes.sgd.dto.RespuestaResponseDTO;
import com.mesapartes.sgd.entity.Documento;
import com.mesapartes.sgd.entity.EstadoDocumento;
import com.mesapartes.sgd.entity.HistorialEstado;
import com.mesapartes.sgd.entity.RespuestaDocumento;
import com.mesapartes.sgd.exception.BusinessException;
import com.mesapartes.sgd.repository.DocumentoRepository;
import com.mesapartes.sgd.repository.HistorialEstadoRepository;
import com.mesapartes.sgd.repository.RespuestaDocumentoRepository;
import com.mesapartes.sgd.service.EmailService;
import com.mesapartes.sgd.service.RespuestaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// =============================================================
// RespuestaServiceImpl — CORRECCIONES:
//
//   1. @Transactional en emitirRespuesta:
//      Sin esto, si el email fallaba a mitad del método,
//      la respuesta quedaba guardada en BD pero el historial no,
//      dejando el sistema en estado inconsistente.
//      Con @Transactional, cualquier excepción hace rollback de
//      TODO lo que se escribió en BD en ese método.
//
//   2. Email enviado DESPUÉS de todos los saves:
//      Antes: crearRespuesta() llamaba enviarEmail() ANTES del save.
//      Si el SMTP fallaba → BusinessException → rollback → 422.
//      Ahora: guardamos todo primero, luego intentamos el email.
//      Si el SMTP falla, capturamos la excepción con try-catch,
//      marcamos enviadoPorEmail=false y guardamos ese estado.
//      La respuesta SIEMPRE se guarda. El email es best-effort.
//
//   3. ¿Por qué best-effort para el email?
//      El email es una notificación, no parte del negocio core.
//      El ciudadano puede ver la respuesta desde el portal aunque
//      el email falle. Es mejor guardar la respuesta sin email
//      que no guardar nada.
// =============================================================
@Service
@RequiredArgsConstructor
public class RespuestaServiceImpl implements RespuestaService {

    private static final Logger log = LoggerFactory.getLogger(RespuestaServiceImpl.class);

    private final RespuestaDocumentoRepository respuestaRepository;
    private final DocumentoRepository documentoRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final EmailService emailService;

    /**
     * Emite una respuesta, archiva el documento y opcionalmente envía email.
     * @Transactional garantiza que los 3 saves (respuesta, documento, historial)
     * sean atómicos. El email es best-effort: si falla, no revierte los saves.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RespuestaResponseDTO emitirRespuesta(String numeroTramite, RespuestaRequestDTO request) {

        Documento documento = obtenerDocumento(numeroTramite);

        // ── 1. Construir la entidad SIN enviar email aún ──────
        RespuestaDocumento respuesta = new RespuestaDocumento();
        respuesta.setDocumento(documento);
        respuesta.setContenido(request.getContenido());
        respuesta.setUsuarioResponsable(request.getUsuarioResponsable());
        respuesta.setEnviadoPorEmail(false); // se actualiza si el email tiene éxito

        // ── 2. Guardar respuesta ──────────────────────────────
        RespuestaDocumento guardada = respuestaRepository.save(respuesta);

        // ── 3. Archivar el documento ──────────────────────────
        documento.setEstado(EstadoDocumento.ARCHIVADO);
        documentoRepository.save(documento);

        // ── 4. Registrar en historial ─────────────────────────
        registrarHistorial(documento, request.getUsuarioResponsable());

        // ── 5. Intentar enviar email (best-effort) ────────────
        // El email se envía DESPUÉS de todos los saves.
        // Si falla, loguea el error pero NO revierte la transacción.
        // La respuesta ya está guardada — el ciudadano puede verla en el portal.
        if (request.isEnviarEmail()
                && documento.getEmailRemitente() != null
                && !documento.getEmailRemitente().isBlank()) {
            try {
                emailService.enviarRespuestaFormal(
                        documento.getEmailRemitente(),
                        numeroTramite,
                        documento.getRemitente(),
                        request.getContenido()
                );
                // Actualizar flag solo si el email tuvo éxito
                guardada.setEnviadoPorEmail(true);
                respuestaRepository.save(guardada);
            } catch (RuntimeException e) {
                // Email falló — log de advertencia, respuesta ya guardada
                log.warn("[RESPUESTA] Email no enviado para trámite {} ({}): {}",
                        numeroTramite, documento.getEmailRemitente(), e.getMessage());
                // No relanzamos — la transacción NO hace rollback por esto
            }
        }

        return mapearRespuesta(guardada, numeroTramite);
    }

    @Override
    public List<RespuestaResponseDTO> obtenerRespuestasPorTramite(String numeroTramite) {
        Documento documento = obtenerDocumento(numeroTramite);
        return respuestaRepository
                .findByDocumentoOrderByFechaRespuestaAsc(documento)
                .stream()
                .map(r -> mapearRespuesta(r, numeroTramite))
                .collect(Collectors.toList());
    }

    // ── PRIVADOS ─────────────────────────────────────────────

    private Documento obtenerDocumento(String numeroTramite) {
        return documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() ->
                        new RuntimeException("Documento no encontrado: " + numeroTramite));
    }

    private void registrarHistorial(Documento documento, String usuario) {
        HistorialEstado historial = new HistorialEstado();
        historial.setDocumento(documento);
        historial.setEstado(EstadoDocumento.ARCHIVADO);
        historial.setObservacion("Respuesta emitida al ciudadano por: " + usuario);
        historial.setUsuarioResponsable(usuario);
        historialEstadoRepository.save(historial);
    }

    private RespuestaResponseDTO mapearRespuesta(RespuestaDocumento respuesta, String numeroTramite) {
        RespuestaResponseDTO response = new RespuestaResponseDTO();
        response.setId(respuesta.getId());
        response.setNumeroTramite(numeroTramite);
        response.setContenido(respuesta.getContenido());
        response.setUsuarioResponsable(respuesta.getUsuarioResponsable());
        response.setFechaRespuesta(respuesta.getFechaRespuesta());
        response.setEnviadoPorEmail(respuesta.isEnviadoPorEmail());
        return response;
    }
}