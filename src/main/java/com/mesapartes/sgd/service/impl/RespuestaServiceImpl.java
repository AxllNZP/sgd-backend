package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.RespuestaRequestDTO;
import com.mesapartes.sgd.dto.RespuestaResponseDTO;
import com.mesapartes.sgd.entity.Documento;
import com.mesapartes.sgd.entity.EstadoDocumento;
import com.mesapartes.sgd.entity.HistorialEstado;
import com.mesapartes.sgd.entity.RespuestaDocumento;
import com.mesapartes.sgd.repository.DocumentoRepository;
import com.mesapartes.sgd.repository.HistorialEstadoRepository;
import com.mesapartes.sgd.repository.RespuestaDocumentoRepository;
import com.mesapartes.sgd.service.EmailService;
import com.mesapartes.sgd.service.RespuestaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RespuestaServiceImpl implements RespuestaService {

    private final RespuestaDocumentoRepository respuestaRepository;
    private final DocumentoRepository documentoRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final EmailService emailService;

    /**
     * Emite una respuesta a un documento y opcionalmente la envía por correo.
     */
    @Override
    public RespuestaResponseDTO emitirRespuesta(String numeroTramite, RespuestaRequestDTO request) {

        Documento documento = obtenerDocumento(numeroTramite);

        RespuestaDocumento respuesta = crearRespuesta(documento, request, numeroTramite);

        RespuestaDocumento guardada = respuestaRepository.save(respuesta);

        archivarDocumento(documento, request.getUsuarioResponsable());

        registrarHistorial(documento, request.getUsuarioResponsable());

        return mapearRespuesta(guardada, numeroTramite);
    }

    /**
     * Obtiene todas las respuestas emitidas para un trámite.
     */
    @Override
    public List<RespuestaResponseDTO> obtenerRespuestasPorTramite(String numeroTramite) {

        Documento documento = obtenerDocumento(numeroTramite);

        return respuestaRepository
                .findByDocumentoOrderByFechaRespuestaAsc(documento)
                .stream()
                .map(respuesta -> mapearRespuesta(respuesta, numeroTramite))
                .collect(Collectors.toList());
    }

    /**
     * Busca el documento por número de trámite.
     */
    private Documento obtenerDocumento(String numeroTramite) {
        return documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() ->
                        new RuntimeException("Documento no encontrado: " + numeroTramite));
    }

    /**
     * Construye la entidad RespuestaDocumento.
     */
    private RespuestaDocumento crearRespuesta(
            Documento documento,
            RespuestaRequestDTO request,
            String numeroTramite) {

        RespuestaDocumento respuesta = new RespuestaDocumento();

        respuesta.setDocumento(documento);
        respuesta.setContenido(request.getContenido());
        respuesta.setUsuarioResponsable(request.getUsuarioResponsable());
        respuesta.setEnviadoPorEmail(false);

        enviarEmailSiCorresponde(respuesta, documento, request, numeroTramite);

        return respuesta;
    }

    /**
     * Envía la respuesta por correo si el request lo solicita.
     */
    private void enviarEmailSiCorresponde(
            RespuestaDocumento respuesta,
            Documento documento,
            RespuestaRequestDTO request,
            String numeroTramite) {

        if (request.isEnviarEmail()
                && documento.getEmailRemitente() != null
                && !documento.getEmailRemitente().isEmpty()) {

            emailService.enviarRespuestaFormal(
                    documento.getEmailRemitente(),
                    numeroTramite,
                    documento.getRemitente(),
                    request.getContenido()
            );

            respuesta.setEnviadoPorEmail(true);
        }
    }

    /**
     * Cambia el estado del documento a ARCHIVADO.
     */
    private void archivarDocumento(Documento documento, String usuario) {

        documento.setEstado(EstadoDocumento.ARCHIVADO);

        documentoRepository.save(documento);
    }

    /**
     * Registra el cambio de estado en el historial.
     */
    private void registrarHistorial(Documento documento, String usuario) {

        HistorialEstado historial = new HistorialEstado();

        historial.setDocumento(documento);
        historial.setEstado(EstadoDocumento.ARCHIVADO);
        historial.setObservacion("Respuesta emitida al ciudadano por: " + usuario);
        historial.setUsuarioResponsable(usuario);

        historialEstadoRepository.save(historial);
    }

    /**
     * Convierte entidad a DTO de respuesta.
     */
    private RespuestaResponseDTO mapearRespuesta(
            RespuestaDocumento respuesta,
            String numeroTramite) {

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