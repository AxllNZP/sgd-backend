package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.HistorialResponseDTO;
import com.mesapartes.sgd.entity.Documento;
import com.mesapartes.sgd.entity.HistorialEstado;
import com.mesapartes.sgd.repository.DocumentoRepository;
import com.mesapartes.sgd.repository.HistorialEstadoRepository;
import com.mesapartes.sgd.service.HistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistorialServiceImpl implements HistorialService {

    private final HistorialEstadoRepository historialEstadoRepository;
    private final DocumentoRepository documentoRepository;

    /**
     * Obtiene el historial completo de estados de un documento
     * a partir de su número de trámite.
     */
    @Override
    public List<HistorialResponseDTO> obtenerHistorialPorNumeroTramite(String numeroTramite) {

        Documento documento = obtenerDocumentoPorNumeroTramite(numeroTramite);

        return historialEstadoRepository
                .findByDocumentoOrderByFechaCambioAsc(documento)
                .stream()
                .map(historial -> mapearRespuesta(historial, numeroTramite))
                .collect(Collectors.toList());
    }

    @Override
    public Page<HistorialResponseDTO> obtenerHistorialPorNumeroTramite(String numeroTramite, Pageable pageable) {
        Documento documento = obtenerDocumentoPorNumeroTramite(numeroTramite);

        // Requiere que HistorialEstadoRepository tenga:
        // Page<HistorialEstado> findByDocumentoOrderByFechaCambioAsc(Documento documento, Pageable pageable);
        return historialEstadoRepository
                .findByDocumentoOrderByFechaCambioAsc(documento, pageable)
                .map(historial -> mapearRespuesta(historial, numeroTramite));
    }

    /**
     * Busca el documento asociado al número de trámite.
     */
    private Documento obtenerDocumentoPorNumeroTramite(String numeroTramite) {
        return documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new RuntimeException(
                        "Documento no encontrado con número de trámite: " + numeroTramite
                ));
    }

    /**
     * Convierte la entidad HistorialEstado a DTO.
     */
    private HistorialResponseDTO mapearRespuesta(HistorialEstado historial, String numeroTramite) {

        HistorialResponseDTO response = new HistorialResponseDTO();

        response.setId(historial.getId());
        response.setNumeroTramite(numeroTramite);
        response.setEstado(historial.getEstado());
        response.setObservacion(historial.getObservacion());
        response.setUsuarioResponsable(historial.getUsuarioResponsable());
        response.setFechaCambio(historial.getFechaCambio());

        return response;
    }
}