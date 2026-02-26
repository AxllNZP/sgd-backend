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

    @Override
    public RespuestaResponseDTO emitirRespuesta(String numeroTramite, RespuestaRequestDTO request) {
        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + numeroTramite));

        // Crear respuesta
        RespuestaDocumento respuesta = new RespuestaDocumento();
        respuesta.setDocumento(documento);
        respuesta.setContenido(request.getContenido());
        respuesta.setUsuarioResponsable(request.getUsuarioResponsable());
        respuesta.setEnviadoPorEmail(false);

        // Enviar email si corresponde
        if (request.isEnviarEmail() && documento.getEmailRemitente() != null
                && !documento.getEmailRemitente().isEmpty()) {
            emailService.enviarRespuestaFormal(
                    documento.getEmailRemitente(),
                    numeroTramite,
                    documento.getRemitente(),
                    request.getContenido()
            );
            respuesta.setEnviadoPorEmail(true);
        }

        RespuestaDocumento guardada = respuestaRepository.save(respuesta);

        // Archivar documento y registrar historial
        documento.setEstado(EstadoDocumento.ARCHIVADO);
        documentoRepository.save(documento);

        HistorialEstado historial = new HistorialEstado();
        historial.setDocumento(documento);
        historial.setEstado(EstadoDocumento.ARCHIVADO);
        historial.setObservacion("Respuesta emitida al ciudadano por: " + request.getUsuarioResponsable());
        historial.setUsuarioResponsable(request.getUsuarioResponsable());
        historialEstadoRepository.save(historial);

        return mapearRespuesta(guardada, numeroTramite);
    }

    @Override
    public List<RespuestaResponseDTO> obtenerRespuestasPorTramite(String numeroTramite) {
        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + numeroTramite));

        return respuestaRepository.findByDocumentoOrderByFechaRespuestaAsc(documento)
                .stream()
                .map(r -> mapearRespuesta(r, numeroTramite))
                .collect(Collectors.toList());
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